package ak.mcmod.chaindestruction.util;

import ak.mcmod.ak_lib.block.BlockUtils;
import ak.mcmod.ak_lib.item.ItemUtils;
import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.ak_lib.util.TailCall;
import ak.mcmod.ak_lib.util.TailCallUtils;
import ak.mcmod.chaindestruction.ChainDestruction;
import ak.mcmod.chaindestruction.capability.CDItemStackStatus;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.ICDItemStackStatusHandler;
import ak.mcmod.chaindestruction.capability.ICDPlayerStatusHandler;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_ITEM;

/**
 * Created by A.K. on 2021/09/18.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChainDestructionLogic {
  private ChainDestructionLogic(){}

  /**
   * 指定座標のブロックを破壊する処理
   *
   * @param world    World
   * @param player   プレイヤー
   * @param blockPos 指定座標
   * @param item     手持ちアイテム
   * @return 判定アイテムがnullの時やアイテムが壊れた時はtrueを返す。falseで続行。
   */
  public static boolean destroyBlockAtPosition(World world, EntityPlayer player, BlockPos blockPos, ItemStack item) {
    IBlockState state = world.getBlockState(blockPos);
    boolean startBreakingBlock = item.getItem().onBlockStartBreak(item, blockPos, player);
    boolean blockDestroyed = item.getItem().onBlockDestroyed(item, world, state, blockPos, player);
    if (!startBreakingBlock && blockDestroyed) {
      if (world.setBlockToAir(blockPos)) {
        state.getBlock().onBlockHarvested(world, blockPos, state, player);
        state.getBlock().onPlayerDestroy(world, blockPos, state);
        state.getBlock().harvestBlock(world, player, new BlockPos(player.posX, player.posY, player.posZ), state, null, item);
        if (ChainDestruction.destroyingSequentially) {
          dropItemNearPlayer(world, player, blockPos);
        }
        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, item) == 0) {
          int exp = state.getBlock().getExpDrop(state, world, blockPos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, item));
          state.getBlock().dropXpOnBlockBreak(world, new BlockPos(player.posX, player.posY, player.posZ), exp);
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
  private static void dropItemNearPlayer(World world, EntityPlayer player, BlockPos blockPos) {
    List<EntityItem> entityItemList = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(
            blockPos.getX(), blockPos.getY(), blockPos.getZ(),
            blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1)
            .grow(1, 1, 1));
    double d0, d1, d2;
    float f1 = player.rotationYaw * (float) (2 * Math.PI / 360);
    for (EntityItem eItem : entityItemList) {
      eItem.setNoPickupDelay();
      d0 = player.posX - MathHelper.sin(f1) * 0.5D;
      d1 = player.posY + 0.5D;
      d2 = player.posZ + MathHelper.cos(f1) * 0.5D;
      eItem.setPosition(d0, d1, d2);
    }
  }

  /**
   * ツールの耐久値が１以下の時を判定
   *
   * @param itemStack 手持ちアイテム
   * @return 壊れない設定でかつ耐久が1以下の場合true
   */
  private static boolean isItemBreakingSoon(ItemStack itemStack) {
    return ChainDestruction.notToDestroyItem && (itemStack.getMaxDamage() - itemStack.getItemDamage() <= 1);
  }

  /**
   * IBlockStateが破壊対象かどうかを判定
   *
   * @param player   プレイヤー
   * @param state    判定されるIBlockState
   * @param heldItem 手持ちアイテム
   * @param canHarvestBlock 手持ちアイテムで対象ブロックを採掘可能か
   * @return 破壊対象かどうか
   */
  public static boolean checkBlockValidate(EntityPlayer player, IBlockState state, ItemStack heldItem, boolean canHarvestBlock) {
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    if (Objects.isNull(status)) {
      return false;
    }
    if (heldItem.isEmpty()) {
      return false;
    }
    if (status.getModeType() == ModeType.BRANCH_MINING || status.getModeType() == ModeType.WALL_MINING) {
      return canHarvestBlock;
    } else if (status.isPrivateRegisterMode() && heldItem.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
      ICDItemStackStatusHandler itemStatus = CDItemStackStatus.get(heldItem);
      if (Objects.isNull(itemStatus)) {
        return false;
      }
      return StringUtils.match(status.isTreeMode() ? itemStatus.getEnableLogBlocks() : itemStatus.getEnableBlocks(), state) && canHarvestBlock;
    }
    return StringUtils.match(status.isTreeMode() ? status.getEnableLogBlocks() : status.getEnableBlocks(), state) && canHarvestBlock;

  }

  /**
   * ブロックの登録／削除
   *
   * @param set    登録／削除する対象集合
   * @param player プレイヤー
   * @param state  対象ブロックのIBlockState
   */
  public static void addAndRemoveBlocks(Set<String> set, EntityPlayer player, IBlockState state) {
    Block block = state.getBlock();
    //ブロックの固有文字列
    String uidStr = StringUtils.getUniqueString(block.getRegistryName());
    //Meta値付き固有文字列
    String uidMetaStr = state.toString();
    //鉱石辞書名かMeta値付き固有文字列のリスト
    List<String> oreNames = StringUtils.makeStringDataFromBlockState(state);
    String chat;
    if (player.isSneaking()) {
      //鉱石辞書名かBlockState付き固有文字列があって、登録されて無ければ、そちらを登録。
      if (!StringUtils.matchOreNames(set, oreNames)) {
        set.addAll(oreNames);
        chat = String.format("Add Block : %s", oreNames);
        player.sendMessage(new TextComponentString(chat));
      }
      if (!oreNames.contains(uidStr)) {
        set.remove(uidStr);
      }
    } else {
      //文字列がマッチした場合のみ、チャット出力。
      if (StringUtils.match(set, state)) {
        chat = String.format("Remove Block and its OreDictionary Names: %s", uidMetaStr);
        player.sendMessage(new TextComponentString(chat));
      }
      set.remove(uidStr);
      oreNames.forEach(set::remove);
    }
  }

  /**
   * ２つのBlockMetaPairが鉱石辞書名経由で等しいかどうか
   *
   * @param pair1 判定元のIBlockState
   * @param pair2 判定されるIBlockState
   * @return 同じ鉱石辞書名を含む場合はtrue
   */
  private static boolean matchTwoBlocks(IBlockState pair1, IBlockState pair2) {
    List<String> targetOreNames = StringUtils.makeStringDataFromBlockState(pair1);
    List<String> checkOreNames = StringUtils.makeStringDataFromBlockState(pair2);
    for (String str : checkOreNames) {
      if (targetOreNames.contains(str)) return true;
    }
    return false;
  }

  /**
   * 連鎖破壊処理が動くかどうか
   *
   * @param world    ワールド
   * @param player   プレイヤー
   * @param state    破壊した最初のブロックのIBlockState
   * @param blockPos 破壊した最初のブロックのBlockPos
   * @param heldItem 手持ちアイテム
   * @return 連鎖破壊処理が動くならtrue
   */
  public static boolean isChainDestructionActionable(World world ,EntityPlayer player, IBlockState state, BlockPos blockPos, ItemStack heldItem) {
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    if (Objects.isNull(status)) {
      return false;
    }
    return checkBlockValidate(player, state, heldItem, ForgeHooks.canHarvestBlock(state.getBlock(), player, world, blockPos))
            && status.getEnableItems().contains(StringUtils.getUniqueString(heldItem.getItem().getRegistryName()));
  }

  public static void setup(IBlockState firstBrokenBlockState, EntityPlayer player, World world, BlockPos blockPos) {
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    if (Objects.isNull(status)) {
      return;
    }
    LinkedHashSet<BlockPos> searchedBlockSet = searchBlock(world, status, player, firstBrokenBlockState, blockPos);
    if (!ChainDestruction.destroyingSequentially) {
      destroyBlock(world, player, player.getHeldItemMainhand(), searchedBlockSet);
    } else {
      ChainDestruction.digTaskEvent.digTaskSet.add(new DigTask(player, player.getHeldItemMainhand(), searchedBlockSet));
    }
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
  private static LinkedHashSet<BlockPos> searchBlock(World world, ICDPlayerStatusHandler status, EntityPlayer player, IBlockState target, BlockPos targetPos) {
    BlockPos minPos = status.getMinPos(player, targetPos);
    BlockPos maxPos = status.getMaxPos(targetPos);
    int distance = status.isTreeMode() ? 3 : 2;
    Queue<BlockPos> blockPosQueue = Queues.newArrayDeque();
    blockPosQueue.add(targetPos);
    Predicate<BlockPos> checkPredicate = (blockPos) -> checkBlock(status, target, world.getBlockState(blockPos),
            player.getHeldItemMainhand(), ForgeHooks.canHarvestBlock(world.getBlockState(blockPos).getBlock(), player, world, blockPos));
    Predicate<BlockPos> rangePredicate = (blockPos) -> {
      boolean ret = blockPos.getX() >= minPos.getX() && blockPos.getY() >= minPos.getY() && blockPos.getZ() >= minPos.getZ();
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
    BlockPos blockPos = queue.poll();
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
  private static void destroyBlock(World world, EntityPlayer player, ItemStack item, LinkedHashSet<BlockPos> connectedBlockSet) {
    for (BlockPos blockPos : connectedBlockSet) {
      if (destroyBlockAtPosition(world, player, blockPos, item)) {
        break;
      }
    }
  }

  /**
   * 壊そうとしているブロックが最初に壊したブロックと同じかどうか判定
   *
   * @param status   プレイヤーの設定情報
   * @param target   最初のブロック
   * @param check    壊そうとしているブロック
   * @param heldItem 手持ちアイテム
   * @param canHarvestBlock 手持ちアイテムで対象ブロックを採掘可能か
   * @return 同種ならtrue
   */
  private static boolean checkBlock(ICDPlayerStatusHandler status, IBlockState target,
                                    IBlockState check, ItemStack heldItem, boolean canHarvestBlock) {
    if (check.getBlock() == Blocks.AIR) return false;
    if (status.getModeType() == ModeType.BRANCH_MINING || status.getModeType() == ModeType.WALL_MINING) {
      return canHarvestBlock;
    } else if (status.isTreeMode()) {
      if (status.isPrivateRegisterMode() && heldItem.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
        ICDItemStackStatusHandler itemStatus = CDItemStackStatus.get(heldItem);
        if (Objects.isNull(itemStatus)) {
          return false;
        }
        return StringUtils.match(status.isTreeMode() ? itemStatus.getEnableLogBlocks() : itemStatus.getEnableBlocks(), check) && canHarvestBlock;
      }
      return StringUtils.match(status.getEnableLogBlocks(), check) && canHarvestBlock;
    }
    return matchTwoBlocks(target, check) && canHarvestBlock;
  }
}
