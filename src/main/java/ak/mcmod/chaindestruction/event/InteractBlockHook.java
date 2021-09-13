package ak.mcmod.chaindestruction.event;

import ak.mcmod.chaindestruction.ChainDestruction;
import ak.mcmod.chaindestruction.api.Constants;
import ak.mcmod.chaindestruction.capability.CDItemStackStatus;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.ICDItemStackStatusHandler;
import ak.mcmod.chaindestruction.capability.ICDPlayerStatusHandler;
import ak.mcmod.chaindestruction.network.MessageCDStatusProperties;
import ak.mcmod.chaindestruction.network.PacketHandler;
import ak.mcmod.chaindestruction.util.DigTask;
import ak.mcmod.chaindestruction.util.StringUtils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_ITEM;
import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InteractBlockHook {
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
          destroyItem(player, item);
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
   * 手持ちアイテム破壊処理。ツールホルダー用
   *
   * @param player プレイヤー
   * @param item   手持ちアイテム
   */
  public static void destroyItem(EntityPlayer player, ItemStack item) {
    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, item, EnumHand.MAIN_HAND);
    player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
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
   * キーイベント処理。MessageHandlerから呼ばれる
   *
   * @param item   手持ちアイテム
   * @param player プレイヤー
   * @param key    押下キーを表すbyte
   */
  public void doKeyEvent(ItemStack item, EntityPlayer player, byte key) {
    String chat;
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    if (key == Constants.RegKEY && !item.isEmpty()) {
      Set<String> enableItems = status.getEnableItems();
      String uniqueName = StringUtils.getUniqueString(item.getItem().getRegistryName());
      if (player.isSneaking() && enableItems.contains(uniqueName)) {
        enableItems.remove(uniqueName);
        chat = String.format("Remove Tool : %s", uniqueName);
        player.sendMessage(new TextComponentString(chat));
      }
      if (!player.isSneaking() && !enableItems.contains(uniqueName)) {
        enableItems.add(uniqueName);
        chat = String.format("Add Tool : %s", uniqueName);
        player.sendMessage(new TextComponentString(chat));
      }
    }
    if (key == Constants.DigKEY) {
      status.setDigUnder(!status.isDigUnder());
      chat = String.format("Dig Under %b", status.isDigUnder());
      player.sendMessage(new TextComponentString(chat));
    }
    if (key == Constants.ModeKEY) {
      if (player.isSneaking()) {
        status.setPrivateRegisterMode(!status.isPrivateRegisterMode());
        chat = String.format("Private Register Mode %b", status.isPrivateRegisterMode());
        player.sendMessage(new TextComponentString(chat));
      } else {
        status.setTreeMode(!status.isTreeMode());
        chat = String.format("Tree Mode %b", status.isTreeMode());
        player.sendMessage(new TextComponentString(chat));
      }
    }
    PacketHandler.INSTANCE.sendTo(new MessageCDStatusProperties(player), (EntityPlayerMP) player);
  }

  /**
   * マウスイベント処理。MessageHandlerから呼ばれる
   *
   * @param item          手持ちアイテム
   * @param player        プレイヤー
   * @param mouse         押下したマウスのキーを表すbyte
   * @param isFocusObject オブジェクトにフォーカスしているかどうか
   */
  public void doMouseEvent(ItemStack item, EntityPlayer player, byte mouse, boolean isFocusObject) {
    try {
      ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
      if (!status.getEnableItems().contains(StringUtils.getUniqueString(item.getItem().getRegistryName()))) {
        return;
      }
      String chat;
      if (mouse == Constants.MIDDLE_CLICK && !isFocusObject) {
        int maxDestroyedBlock = status.getMaxDestroyedBlock();
        if (player.isSneaking() && maxDestroyedBlock > 0) {
          status.setMaxDestroyedBlock(--maxDestroyedBlock);
        } else {
          status.setMaxDestroyedBlock(++maxDestroyedBlock);
        }
        chat = String.format("New Max Destroyed : %d", maxDestroyedBlock);
        player.sendMessage(new TextComponentString(chat));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * ブロックを右クリックした際に呼ばれるイベント
   *
   * @param event 右クリックイベント
   */
  @SuppressWarnings("unused")
  @SubscribeEvent
  public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    EntityPlayer player = event.getEntityPlayer();
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    World world = player.getEntityWorld();
    ItemStack itemStack = player.getHeldItemMainhand();
    if (world.isRemote || itemStack.isEmpty()) return;
    String uniqueName = StringUtils.getUniqueString(itemStack.getItem().getRegistryName());
    if (status.getEnableItems().contains(uniqueName)) {
      IBlockState state = world.getBlockState(event.getPos());
      if (status.isPrivateRegisterMode() && itemStack.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
        ICDItemStackStatusHandler itemStatus = CDItemStackStatus.get(itemStack);
        Set<String> enableBlocks = status.isTreeMode() ? itemStatus.getEnableLogBlocks() : itemStatus.getEnableBlocks();
        addAndRemoveBlocks(enableBlocks, player, state);
      } else {
        addAndRemoveBlocks(status.isTreeMode() ? status.getEnableLogBlocks() : status.getEnableBlocks(), player, state);

      }
    }
  }

  @SuppressWarnings("unused")
  @SubscribeEvent
  public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
    EntityPlayer player = event.getEntityPlayer();
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    Set<String> enableItems = status.getEnableItems();
    World world = player.getEntityWorld();
    ItemStack itemStack = player.getHeldItemMainhand();
    if (world.isRemote || itemStack.isEmpty()) return;
    String uniqueName = StringUtils.getUniqueString(itemStack.getItem().getRegistryName());
    if (enableItems.contains(uniqueName)) {
      IBlockState state = world.getBlockState(event.getPos());
      if (checkBlockValidate(player, state, itemStack)
              && enableItems.contains(StringUtils.getUniqueString(itemStack.getItem().getRegistryName()))) {
        status.setFace(event.getFace());
      }
    }
  }

  /**
   * IBlockStateが破壊対象かどうかを判定
   *
   * @param player   プレイヤー
   * @param state    判定されるIBlockState
   * @param heldItem 手持ちアイテム
   * @return 破壊対象かどうか
   */
  private boolean checkBlockValidate(EntityPlayer player, IBlockState state, ItemStack heldItem) {
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    if (heldItem.isEmpty()) {
      return false;
    }
    if (status.isPrivateRegisterMode() && heldItem.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
      ICDItemStackStatusHandler itemStatus = CDItemStackStatus.get(heldItem);
      return StringUtils.match(status.isTreeMode() ? itemStatus.getEnableLogBlocks() : itemStatus.getEnableBlocks(), state);
    }
    return StringUtils.match(status.isTreeMode() ? status.getEnableLogBlocks() : status.getEnableBlocks(), state);

  }

  /**
   * ブロックの登録／削除
   *
   * @param set    登録／削除する対象集合
   * @param player プレイヤー
   * @param state  対象ブロックのIBlockState
   */
  private void addAndRemoveBlocks(Set<String> set, EntityPlayer player, IBlockState state) {
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
  private boolean matchTwoBlocks(IBlockState pair1, IBlockState pair2) {
    List<String> targetOreNames = StringUtils.makeStringDataFromBlockState(pair1);
    List<String> checkOreNames = StringUtils.makeStringDataFromBlockState(pair2);
    for (String str : checkOreNames) {
      if (targetOreNames.contains(str)) return true;
    }
    return false;
  }

  /*偽装しているブロックへの対応含めこちらで処理したほうが良い*/
  @SuppressWarnings("unused")
  @SubscribeEvent
  public void blockBreakingEvent(BlockEvent.BreakEvent event) {
    if (!(event.getPlayer() instanceof FakePlayer) && !event.getWorld().isRemote) {
      if (isChainDestructionActionable(event.getPlayer(), event.getState(), event.getPlayer().getHeldItemMainhand())) {
        setup(event.getState(), event.getPlayer(), event.getWorld(), event.getPos());
      }
    }
  }

  @SubscribeEvent
  @SuppressWarnings("unused")
  //Dimension移動時や、リスポーン時に呼ばれるイベント。古いインスタンスと新しいインスタンスの両方を参照できる。
  public void onCloningPlayer(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
    //死亡時に呼ばれてるかどうか
    if (event.isWasDeath()) {
      //古いカスタムデータ
      ICDPlayerStatusHandler oldCDS = event.getOriginal().getCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
      //新しいカスタムデータ
      ICDPlayerStatusHandler newCDS = event.getEntityPlayer().getCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
      NBTBase nbt = CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage().writeNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, oldCDS, null);
      CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage().readNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, newCDS, null, nbt);

    }
  }

  /**
   * 連鎖破壊処理が動くかどうか
   *
   * @param player   プレイヤー
   * @param state    破壊した最初のブロックのIBlockState
   * @param heldItem 手持ちアイテム
   * @return 連鎖破壊処理が動くならtrue
   */
  private boolean isChainDestructionActionable(EntityPlayer player, IBlockState state, ItemStack heldItem) {
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    return checkBlockValidate(player, state, heldItem)
            && status.getEnableItems().contains(StringUtils.getUniqueString(heldItem.getItem().getRegistryName()));
  }

  private void setup(IBlockState firstBrokenBlockState, EntityPlayer player, World world, BlockPos blockPos) {
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    Set<BlockPos> searchedBlockSet = searchBlock(world, status, player, firstBrokenBlockState, blockPos);
    LinkedHashSet<BlockPos> connectedBlockSet = getConnectedBlockSet(status, blockPos, searchedBlockSet);
    if (!ChainDestruction.destroyingSequentially) {
      destroyBlock(world, player, player.getHeldItemMainhand(), connectedBlockSet);
    } else {
      ChainDestruction.digTaskEvent.digTaskSet.add(new DigTask(player, player.getHeldItemMainhand(), connectedBlockSet, blockPos));
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
  @Nonnull
  private Set<BlockPos> searchBlock(World world, ICDPlayerStatusHandler status, EntityPlayer player, IBlockState target, BlockPos targetPos) {
    BlockPos minPos = status.getMinPos(player, targetPos);
    BlockPos maxPos = status.getMaxPos(targetPos);
    return StreamSupport.stream(BlockPos.getAllInBox(minPos, maxPos).spliterator(), true)
            .filter(blockPos -> checkBlock(status, target, world.getBlockState(blockPos), player.getHeldItemMainhand()))
            .collect(Collectors.toSet());
  }

  /**
   * 範囲内の同種ブロックから最初のブロックとつながっているブロックを取得する
   *
   * @param status           連鎖破壊ステータスクラス
   * @param origin           最初に破壊したブロック
   * @param searchedBlockSet 同種ブロックの座標集合
   * @return 最初のブロックとつながっているブロックの座標集合
   */
  @Nonnull
  private LinkedHashSet<BlockPos> getConnectedBlockSet(ICDPlayerStatusHandler status, BlockPos origin, Set<BlockPos> searchedBlockSet) {
    LinkedHashSet<BlockPos> connectedBlockSet = new LinkedHashSet<>();
    connectedBlockSet.add(origin);
    int distance = status.isTreeMode() ? 3 : 1;
    boolean check = true;
    while (check) {
      check = false;
      Iterator<BlockPos> iterator = searchedBlockSet.iterator();
      while (iterator.hasNext()) {
        BlockPos blockPos = iterator.next();
        for (BlockPos listedBlockPos : connectedBlockSet) {
          if (listedBlockPos.distanceSq(blockPos) <= distance) {
            connectedBlockSet.add(blockPos);
            iterator.remove();
            check = true;
            break;
          }
        }
      }
    }
    connectedBlockSet.remove(origin);
    return connectedBlockSet;
  }

  /**
   * 第四引数の集合内の座標にあるブロックを破壊する処理
   *
   * @param world             Worldクラス
   * @param player            プレイヤー
   * @param item              手持ちアイテム
   * @param connectedBlockSet つながっているブロックの座標集合
   */
  private void destroyBlock(World world, EntityPlayer player, ItemStack item, LinkedHashSet<BlockPos> connectedBlockSet) {
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
   * @return 同種ならtrue
   */
  private boolean checkBlock(ICDPlayerStatusHandler status, IBlockState target, IBlockState check, ItemStack heldItem) {
    if (check.getBlock() == Blocks.AIR) return false;
    if (status.isTreeMode()) {
      if (status.isPrivateRegisterMode() && heldItem.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
        ICDItemStackStatusHandler itemStatus = CDItemStackStatus.get(heldItem);
        return StringUtils.match(status.isTreeMode() ? itemStatus.getEnableLogBlocks() : itemStatus.getEnableBlocks(), check);
      }
      return StringUtils.match(status.getEnableLogBlocks(), check);
    }
    return matchTwoBlocks(target, check);
  }
}