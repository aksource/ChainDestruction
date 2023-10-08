package ak.mcmod.chaindestruction.util;

import ak.mcmod.ak_lib.block.BlockUtils;
import ak.mcmod.ak_lib.item.ItemUtils;
import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.ak_lib.util.TailCall;
import ak.mcmod.ak_lib.util.TailCallUtils;
import ak.mcmod.chaindestruction.ChainDestruction;
import ak.mcmod.chaindestruction.capability.AdditionalItemStackStatus;
import ak.mcmod.chaindestruction.capability.CapabilityAdditionalItemStackStatus;
import ak.mcmod.chaindestruction.capability.CapabilityAdditionalPlayerStatus;
import ak.mcmod.chaindestruction.capability.IAdditionalPlayerStatus;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static ak.mcmod.chaindestruction.util.ConfigUtils.COMMON;

/**
 * Created by A.K. on 2021/09/17.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChainDestructionLogic {

  private ChainDestructionLogic() {}

  /**
   * 指定座標のブロックを破壊する処理
   *
   * @param world    World
   * @param player   プレイヤー
   * @param blockPos 指定座標
   * @param item     手持ちアイテム
   * @return 判定アイテムがnullの時やアイテムが壊れた時はtrueを返す。falseで続行。
   */
  public static boolean destroyBlockAtPosition(ServerLevel world, Player player,
                                               BlockPos blockPos, ItemStack item) {
    var state = world.getBlockState(blockPos);
    if (!COMMON.breakBedrock &&
            state.getDestroySpeed(world, blockPos) < 0) {
      return true;
    }
    var startBreakingBlock = item.getItem().onBlockStartBreak(item, blockPos, player);
    var blockDestroyed = item.getItem()
            .mineBlock(item, world, state, blockPos, player);
    if (!startBreakingBlock && blockDestroyed) {
      if (world.removeBlock(blockPos, false)) {
        state.getBlock().playerWillDestroy(world, blockPos, state, player);
        state.getBlock().destroy(world, blockPos, state);
        state.getBlock()
                .playerDestroy(world, player, new BlockPos(player.position().x, player.position().y, player.position().z), state,
                        null, item);
        if (COMMON.destroyingSequentially) {
          dropItemNearPlayer(world, player, blockPos);
        }
        if (item.getEnchantmentLevel(Enchantments.SILK_TOUCH) == 0) {
          var exp = state.getBlock().getExpDrop(state, world, world.random, blockPos,
                  item.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE),
                  item.getEnchantmentLevel(Enchantments.SILK_TOUCH));
          state.getBlock()
                  .popExperience(world, new BlockPos(player.position().x, player.position().y, player.position().z), exp);
        }
        if (item.getCount() == 0) {
          ItemUtils.destroyItem(player, item);
          return true;
        }
        return isItemBreakingSoon(item);
      }
    }
    return true;
  }

  /**
   * プレイヤーの足元にドロップを集める処理
   *
   * @param world    Worldクラス
   * @param player   プレイヤー
   * @param blockPos 破壊したブロックの座標
   */
  private static void dropItemNearPlayer(Level world, Player player,
                                         BlockPos blockPos) {
    var entityItemList = world
            .getEntitiesOfClass(ItemEntity.class, new AABB(
                    blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                    blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1)
                    .inflate(1, 1, 1));
    var f1 = player.getYRot() * (float) (2 * Math.PI / 360);
    for (var eItem : entityItemList) {
      eItem.setNoPickUpDelay();
      var posX = player.position().x - Mth.sin(f1) * 0.5D;
      var posY = player.position().y + 0.5D;
      var posZ = player.position().z + Mth.cos(f1) * 0.5D;
      eItem.setPos(posX, posY, posZ);
    }
  }

  /**
   * ツールの耐久値が１以下の時を判定
   *
   * @param itemStack 手持ちアイテム
   * @return 壊れない設定でかつ耐久が1以下の場合true
   */
  private static boolean isItemBreakingSoon(ItemStack itemStack) {
    return COMMON.notToDestroyItem && (itemStack.getMaxDamage() - itemStack.getDamageValue() <= 1);
  }

  /**
   * BlockStateが破壊対象かどうかを判定
   *
   * @param player          プレイヤー
   * @param state           判定されるBlockState
   * @param heldItem        手持ちアイテム
   * @param canHarvestBlock 手持ちアイテムで対象ブロックを採掘可能か
   * @return 破壊対象かどうか
   */
  public static boolean checkBlockValidate(Player player, BlockState state, ItemStack heldItem, boolean canHarvestBlock) {
    return player.getCapability(CapabilityAdditionalPlayerStatus.CAPABILITY).map(status -> {
      if (heldItem.isEmpty()) {
        return false;
      }
      if (status.getModeType() == ModeType.BRANCH_MINING || status.getModeType() == ModeType.WALL_MINING) {
        return canHarvestBlock;
      } else if (status.isPrivateRegisterMode() && heldItem
              .getCapability(CapabilityAdditionalItemStackStatus.CAPABILITY, null).isPresent()) {
        var itemStatus = heldItem.getCapability(CapabilityAdditionalItemStackStatus.CAPABILITY)
                .orElse(new AdditionalItemStackStatus(heldItem));
        return StringUtils.match(
                status.isTreeMode() ? itemStatus.getEnableLogBlocks() : itemStatus.getEnableBlocks(),
                state) && canHarvestBlock;
      }
      return StringUtils
              .match(status.isTreeMode() ? status.getEnableLogBlocks() : status.getEnableBlocks(),
                      state) && canHarvestBlock;
    }).orElse(false);
  }

  /**
   * ブロックの登録／削除
   *
   * @param set    登録／削除する対象集合
   * @param player プレイヤー
   * @param state  対象ブロックのIBlockState
   */
  public static void addAndRemoveBlocks(Set<String> set, Set<String> forbiddenTags, Player player, BlockState state) {
    var block = state.getBlock();
    //ブロックの固有文字列
    var uidStr = StringUtils.getUniqueString(ForgeRegistries.BLOCKS.getKey(block));
    //Meta値付き固有文字列
    var uidMetaStr = state.toString();
    //鉱石辞書名かMeta値付き固有文字列のリスト
    var tags = StringUtils.makeStringDataFromBlockState(state).stream().filter(tag -> forbiddenTags.stream().noneMatch(tag::contains)).toList();
    var chat = "";
    if (player.isShiftKeyDown()) {
      //鉱石辞書名かBlockState付き固有文字列があって、登録されて無ければ、そちらを登録。
      if (!StringUtils.matchTagNames(set, tags)) {
        set.addAll(tags);
        chat = String.format("Add Block : %s", tags);
        player.sendSystemMessage(Component.literal(chat));
      }
      if (!tags.contains(uidStr)) {
        set.remove(uidStr);
      }
    } else {
      //文字列がマッチした場合のみ、チャット出力。
      if (StringUtils.match(set, state)) {
        chat = String.format("Remove Block and its tag Names: %s", uidMetaStr);
        player.sendSystemMessage(Component.literal(chat));
      }
      set.remove(uidStr);
      tags.forEach(set::remove);
    }
  }

  /**
   * ２つのBlockMetaPairが鉱石辞書名経由で等しいかどうか
   *
   * @param pair1 判定元のIBlockState
   * @param pair2 判定されるIBlockState
   * @return 同じ鉱石辞書名を含む場合はtrue
   */
  private static boolean matchTwoBlocks(BlockState pair1, BlockState pair2) {
    var targetTagNames = StringUtils.makeStringDataFromBlockState(pair1).stream().filter(tag -> !tag.contains("mineable")).toList();
    var checkTagNames = StringUtils.makeStringDataFromBlockState(pair2).stream().filter(tag -> !tag.contains("mineable")).toList();
    return checkTagNames.stream().anyMatch(targetTagNames::contains);
  }

  /**
   * 連鎖破壊処理が動くかどうか
   *
   * @param world    ワールド
   * @param player   プレイヤー
   * @param state    破壊した最初のブロックのBlockState
   * @param blockPos 破壊した最初のブロックのBlockPos
   * @param heldItem 手持ちアイテム
   * @return 連鎖破壊処理が動くならtrue
   */
  public static boolean isChainDestructionActionable(LevelAccessor world, Player player, BlockState state, BlockPos blockPos, ItemStack heldItem) {
    return player.getCapability(CapabilityAdditionalPlayerStatus.CAPABILITY).map(
            status -> checkBlockValidate(player, state, heldItem, ForgeHooks.isCorrectToolForDrops(state, player))
                    && status.getEnableItems()
                    .contains(StringUtils.getUniqueString(ForgeRegistries.ITEMS.getKey(heldItem.getItem())))).orElse(false);
  }

  public static void setup(BlockState firstBrokenBlockState, Player player, ServerLevel world,
                           BlockPos blockPos) {
    player.getCapability(CapabilityAdditionalPlayerStatus.CAPABILITY).ifPresent(status -> {
      var searchedBlockSet = searchBlock(world, status, player, firstBrokenBlockState,
              blockPos);
      if (!COMMON.destroyingSequentially) {
        destroyBlock(world, player, player.getMainHandItem(), searchedBlockSet);
      } else {
        ChainDestruction.digTaskEvent.digTaskSet
                .add(new DigTask(player, player.getMainHandItem(), searchedBlockSet));
      }
    });
  }

  /**
   * 最初に破壊したブロックと同種ブロックを範囲内から取得
   *
   * @param world     Worldクラス
   * @param status    連鎖破壊ステータスクラス
   * @param target    最初に破壊したブロックのIBlockState
   * @param targetPos 最初に破壊したブロックの座標
   * @return 最初に破壊したブロックと同種ブロックの座標集合
   */
  private static LinkedHashSet<BlockPos> searchBlock(Level world, IAdditionalPlayerStatus status,
                                                     Player player, BlockState target, BlockPos targetPos) {
    var minPos = status.getMinPos(player, targetPos);
    var maxPos = status.getMaxPos(targetPos);
    var distance = status.isTreeMode() ? 3 : 2;
    Queue<BlockPos> blockPosQueue = Queues.newArrayDeque();
    blockPosQueue.add(targetPos);
    Predicate<BlockPos> checkPredicate = (blockPos) -> checkBlock(status, target, world.getBlockState(blockPos),
            player.getMainHandItem(), ForgeHooks.isCorrectToolForDrops(world.getBlockState(blockPos), player));
    Predicate<BlockPos> rangePredicate = (blockPos) -> {
      var ret = blockPos.getX() >= minPos.getX() && blockPos.getY() >= minPos.getY() && blockPos.getZ() >= minPos.getZ();
      ret &= blockPos.getX() <= maxPos.getX() && blockPos.getY() <= maxPos.getY() && blockPos.getZ() <= maxPos.getZ();
      return ret;
    };
    Function<BlockPos, Collection<BlockPos>> nextTargetGetter =
            (blockPos) -> BlockUtils.getBlockPosListWithinManhattan(blockPos, distance);
    return searchBlockCall(checkPredicate, rangePredicate, nextTargetGetter, blockPosQueue, Sets.newLinkedHashSet()).call();
  }

  /**
   * Search blocks to be destroyed.
   *
   * @param checkPredicate   Predicate for checking Block on BlockPos is valid
   * @param rangePredicate   Predicate for checking Block on BlockPos is in range
   * @param nextTargetGetter Function to get next target BlockPos collection
   * @param queue            queue of BlockPos to be checked
   * @param set              set of BlockPos that Block on BlockPos is valid and in range
   * @return LinkedHashSet of BlockPos that Block on BlockPos is valid and in range
   */
  private static TailCall<LinkedHashSet<BlockPos>> searchBlockCall(Predicate<BlockPos> checkPredicate,
                                                                   Predicate<BlockPos> rangePredicate,
                                                                   Function<BlockPos, Collection<BlockPos>> nextTargetGetter,
                                                                   Queue<BlockPos> queue, LinkedHashSet<BlockPos> set) {
    var blockPos = queue.poll();
    if (rangePredicate.test(blockPos) && checkPredicate.test(blockPos) && !set.contains(blockPos)) {
      set.add(blockPos);
      queue.addAll(nextTargetGetter.apply(blockPos));
    }
    return queue.isEmpty() ? TailCallUtils.complete(set)
            : TailCallUtils.nextCall(() -> searchBlockCall(checkPredicate, rangePredicate, nextTargetGetter, queue, set));
  }

  /**
   * 第四引数の集合内の座標にあるブロックを破壊する処理
   *
   * @param world             Worldクラス
   * @param player            プレイヤー
   * @param item              手持ちアイテム
   * @param connectedBlockSet つながっているブロックの座標集合
   */
  private static void destroyBlock(ServerLevel world, Player player, ItemStack item,
                                   Collection<BlockPos> connectedBlockSet) {
    for (var blockPos : connectedBlockSet) {
      if (destroyBlockAtPosition(world, player, blockPos, item)) {
        break;
      }
    }
  }

  /**
   * 壊そうとしているブロックが最初に壊したブロックと同じかどうか判定
   *
   * @param status          プレイヤーの設定情報
   * @param target          最初のブロック
   * @param check           壊そうとしているブロック
   * @param heldItem        手持ちアイテム
   * @param canHarvestBlock 手持ちアイテムで対象ブロックを採掘可能か
   * @return 同種ならtrue
   */
  private static boolean checkBlock(IAdditionalPlayerStatus status, BlockState target,
                                    BlockState check, ItemStack heldItem, boolean canHarvestBlock) {
    if (check.getBlock() == Blocks.AIR) {
      return false;
    }
    if (status.getModeType() == ModeType.BRANCH_MINING || status.getModeType() == ModeType.WALL_MINING) {
      return canHarvestBlock;
    } else if (status.isPrivateRegisterMode() && heldItem
            .getCapability(CapabilityAdditionalItemStackStatus.CAPABILITY, null).isPresent()) {
      var itemStatus = heldItem.getCapability(CapabilityAdditionalItemStackStatus.CAPABILITY)
              .orElse(new AdditionalItemStackStatus(heldItem));
      return StringUtils.match(
              status.isTreeMode() ? itemStatus.getEnableLogBlocks() : itemStatus.getEnableBlocks(), check) && canHarvestBlock;
    } else if (status.isTreeMode()) {
      return StringUtils.match(status.getEnableLogBlocks(), check) && canHarvestBlock;
    }
    return StringUtils.match(status.getEnableBlocks(), check) && canHarvestBlock;
  }
}
