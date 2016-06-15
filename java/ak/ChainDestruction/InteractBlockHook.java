package ak.ChainDestruction;

import ak.ChainDestruction.network.MessageCDStatusProperties;
import ak.ChainDestruction.network.PacketHandler;
import ak.MultiToolHolders.ItemMultiToolHolder;
import ak.akapi.Constants;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.*;

public class InteractBlockHook {
    /**
     * キーイベント処理。MessageHandlerから呼ばれる
     * @param item 手持ちアイテム
     * @param player プレイヤー
     * @param key 押下キーを表すbyte
     */
    public void doKeyEvent(ItemStack item, EntityPlayer player, byte key) {
        String chat;
        CDStatus status = CDStatus.get(player);
        if (key == Constants.RegKEY) {
            if (player.isSneaking() && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(item))) {
                ChainDestruction.enableItems.remove(ChainDestruction.getUniqueStrings(item));
                chat = String.format("Remove Tool : %s", ChainDestruction.getUniqueStrings(item));
                player.addChatMessage(new ChatComponentText(chat));
            }
            if (!player.isSneaking() && !ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(item))) {
                ChainDestruction.enableItems.add(ChainDestruction.getUniqueStrings(item));
                chat = String.format("Add Tool : %s", ChainDestruction.getUniqueStrings(item));
                player.addChatMessage(new ChatComponentText(chat));
            }
        }
        if (key == Constants.DigKEY) {
            status.setDigUnder(!status.isDigUnder());
            chat = String.format("Dig Under %b", status.isDigUnder());
            player.addChatMessage(new ChatComponentText(chat));
        }
        if (key == Constants.ModeKEY) {
            if (player.isSneaking()) {
                status.setPrivateRegisterMode(!status.isPrivateRegisterMode());
                chat = String.format("Private Register Mode %b", status.isPrivateRegisterMode());
                player.addChatMessage(new ChatComponentText(chat));
            } else {
                status.setTreeMode(!status.isTreeMode());
                chat = String.format("Tree Mode %b", status.isTreeMode());
                player.addChatMessage(new ChatComponentText(chat));
            }
        }
        PacketHandler.INSTANCE.sendTo(new MessageCDStatusProperties(player), (EntityPlayerMP) player);
    }

    /**
     * マウスイベント処理。Messagehandlerから呼ばれる
     * @param item 手持ちアイテム
     * @param player プレイヤー
     * @param mouse 押下したマウスのキーを表すbyte
     * @param isFocusObject オブジェクトにフォーカスしているかどうか
     */
    public void doMouseEvent(ItemStack item, EntityPlayer player, byte mouse, boolean isFocusObject) {
        try {
            if (!ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(item))) {
                return;
            }
            String chat;
            if (mouse == Constants.MIDDLE_CLICK && !isFocusObject) {
                if (player.isSneaking() && ChainDestruction.maxDestroyedBlock > 0) {
                    ChainDestruction.maxDestroyedBlock--;
                } else {
                    ChainDestruction.maxDestroyedBlock++;
                }
                chat = String.format("New Max Destroyed : %d", ChainDestruction.maxDestroyedBlock);
                player.addChatMessage(new ChatComponentText(chat));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ブロックをクリック時に呼ばれるイベント
     * @param event PlayerInteractEvent
     */
    @SuppressWarnings("unused")
    @SubscribeEvent
    public void interactBlock(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        CDStatus status = CDStatus.get(player);
        World world = player.worldObj;
        ItemStack item = event.entityPlayer.getCurrentEquippedItem();
        if (world.isRemote || item == null) return;
        String uniqueName = ChainDestruction.getUniqueStrings(item.getItem());
        if (ChainDestruction.enableItems.contains(uniqueName)) {
            IBlockState state = world.getBlockState(event.pos);
            if (event.action == Action.RIGHT_CLICK_BLOCK) {
                if (status.isPrivateRegisterMode()) {
                    if (!ChainDestruction.privateItemBlockMap.containsKey(uniqueName)) {
                        ChainDestruction.privateItemBlockMap.put(uniqueName, new HashSet<String>());
                    }
                    addAndRemoveBlocks(ChainDestruction.privateItemBlockMap.get(uniqueName), player, state);
                } else {
                    if (status.isTreeMode()) {
                        addAndRemoveBlocks(ChainDestruction.enableLogBlocks, player, state);
                    } else {
                        addAndRemoveBlocks(ChainDestruction.enableBlocks, player, state);
                    }
                }
            }
            if (event.action == Action.LEFT_CLICK_BLOCK
                    && checkBlockValidate(player, state, item)
                    && ChainDestruction.enableItems.contains(GameRegistry.findUniqueIdentifierFor(item.getItem()).toString())) {
                status.setFace(event.face);
            }
        }
    }

    /**
     * IBlockStateが破壊対象かどうかを判定
     * @param state 判定されるIBlockState
     * @param heldItem 手持ちアイテム
     * @return 破壊対象かどうか
     */
    private boolean checkBlockValidate(EntityPlayer player, IBlockState state, ItemStack heldItem) {
        CDStatus status = CDStatus.get(player);
        if (state == null || heldItem == null) {
            return false;
        }
        String uniqueName = ChainDestruction.getUniqueStrings(heldItem.getItem());
        if (status.isPrivateRegisterMode() && ChainDestruction.privateItemBlockMap.containsKey(uniqueName)) {
            return match(ChainDestruction.privateItemBlockMap.get(uniqueName), state);
        }
        if (status.isTreeMode()) {
            return match(ChainDestruction.enableLogBlocks, state);
        }
        return match(ChainDestruction.enableBlocks, state);
    }

    /**
     * ブロックの登録／削除
     * @param set 登録／削除する対象集合
     * @param player プレイヤー
     * @param state 対象ブロックのIBlockState
     */
    private void addAndRemoveBlocks(Set<String> set, EntityPlayer player, IBlockState state) {
        Block block = state.getBlock();
        //ブロックの固有文字列
        String uidStr = GameRegistry.findUniqueIdentifierFor(block).toString();
        //Meta値付き固有文字列
        String uidMetaStr = state.toString();
        //鉱石辞書名かMeta値付き固有文字列のリスト
        List<String> oreNames = ChainDestruction.makeStringDataFromBlockState(state);
        String chat;
        if (player.isSneaking()) {
            //鉱石辞書名かBlockState付き固有文字列があって、登録されて無ければ、そちらを登録。
            if (!matchOreNames(set, oreNames)) {
                set.addAll(oreNames);
                chat = String.format("Add Block : %s", oreNames.toString());
                player.addChatMessage(new ChatComponentText(chat));
            }
            if (!oreNames.contains(uidStr)) {
                set.remove(uidStr);
            }
        } else {
            //文字列がマッチした場合のみ、チャット出力。
            if (match(set, state)) {
                chat = String.format("Remove Block and its OreDictionary Names: %s", uidMetaStr);
                player.addChatMessage(new ChatComponentText(chat));
            }
            set.remove(uidStr);
            set.removeAll(oreNames);
        }
    }

    /**
     * 破壊対象ブロック名集合内に固有文字列が含まれているかどうか
     * @param set 破壊対象ブロック名集合
     * @param uid ブロックの固有文字列
     * @param uidmeta メタ付きブロックの固有文字列　[固有文字列]:[meta]
     * @return 含まれていたらtrue
     */
    private boolean matchBlockMetaNames(Set<String> set, String uid, String uidmeta) {
        return set.contains(uid) || set.contains(uidmeta);
    }

    /**
     * 鉱石辞書名リスト内の要素と破壊対象ブロック名集合の要素で一致するものがあるかどうか
     * @param set 破壊対象ブロック名集合
     * @param oreNames 鉱石辞書名リスト
     * @return 一致する要素があるならtrue
     */
    private boolean matchOreNames(Set<String> set, List<String> oreNames) {
        for (String string : oreNames) {
            if (set.contains(string)) return true;
        }
        return false;
    }

    /**
     * 破壊対象ブロック名集合内に引数のIBlockStateが表すブロックが含まれるかどうか
     * @param set 破壊対象ブロック名集合
     * @param state 破壊対象判定IBlockState
     * @return 含まれていたらtrue
     */
    private boolean match(Set<String> set, IBlockState state) {
        Block block = state.getBlock();
        String uidStr = GameRegistry.findUniqueIdentifierFor(block).toString();
        String uidMetaStr = state.toString();
        List<String> oreNames = ChainDestruction.makeStringDataFromBlockState(state);
        return matchOreNames(set, oreNames) || matchBlockMetaNames(set, uidStr, uidMetaStr);
    }

    /**
     * ２つのBlockMetaPairが鉱石辞書名経由で等しいかどうか
     * @param pair1 判定元のIBlockState
     * @param pair2 判定されるIBlockState
     * @return 同じ鉱石辞書名を含む場合はtrue
     */
    private boolean matchTwoBlocks(IBlockState pair1, IBlockState pair2) {
        List<String> targetOreNames = ChainDestruction.makeStringDataFromBlockState(pair1);
        List<String> checkOreNames = ChainDestruction.makeStringDataFromBlockState(pair2);
        for (String str : checkOreNames) {
            if (targetOreNames.contains(str)) return true;
        }
        return false;
    }

    /*偽装しているブロックへの対応含めこちらで処理したほうが良い*/
    @SuppressWarnings("unused")
    @SubscribeEvent
    public void blockBreakingEvent(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof FakePlayer) && !event.world.isRemote) {
            if (isChainDestructionActionable(event.getPlayer(), event.state, event.getPlayer().getCurrentEquippedItem())) {
                setup(event.state, event.getPlayer(), event.world, event.pos);
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            CDStatus.register((EntityPlayer) event.entity);
        }
    }

    @SubscribeEvent
    //Dimension移動時や、リスポーン時に呼ばれるイベント。古いインスタンスと新しいインスタンスの両方を参照できる。
    public void onCloningPlayer(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        //死亡時に呼ばれてるかどうか
        if (event.wasDeath) {
            //古いカスタムデータ
            IExtendedEntityProperties oldEntityProperties = event.original.getExtendedProperties(CDStatus.EXT_PROP_NAME);
            //新しいカスタムデータ
            IExtendedEntityProperties newEntityProperties = event.entityPlayer.getExtendedProperties(CDStatus.EXT_PROP_NAME);
            NBTTagCompound playerData = new NBTTagCompound();
            //データの吸い出し
            oldEntityProperties.saveNBTData(playerData);
            //データの書き込み
            newEntityProperties.loadNBTData(playerData);

        }
    }

    /*BlockBreakEventに処理を移行した todo 削除予定*/
//    @SubscribeEvent
//    @SuppressWarnings("unused")
//    @Deprecated
//    public void HarvestEvent(HarvestDropsEvent event) {
//        if (!event.world.isRemote && !doChain
//                && event.harvester != null
//                && event.harvester.getCurrentEquippedItem() != null
//                /*通常は左の判定だけで良いが、別のブロックに偽装するブロックに対応するため、右の判定を追加*/
//                && (checkBlockValidate(event.state, event.harvester.getCurrentEquippedItem()) || checkBlockValidate(event.state, event.harvester.getCurrentEquippedItem()))
//                && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(event.harvester.getCurrentEquippedItem().getItem()))) {
//            //通常の破壊処理からこのイベントが呼ばれるので、連鎖処理を初回のみにするための処置
//            doChain = true;
//            EntityItem ei;
//            for (ItemStack stack : event.drops) {
//                ei = new EntityItem(event.world, event.harvester.posX, event.harvester.posY, event.harvester.posZ, stack);
//                ei.setNoPickupDelay();
//                event.world.spawnEntityInWorld(ei);
//            }
//            event.drops.clear();
//            setup(event.state, event.harvester, event.world, event.pos);
//            doChain = false;
//        }
//    }

    /**
     * 連鎖破壊処理が動くかどうか
     * @param state 破壊した最初のブロックのIBlockState
     * @param heldItem 手持ちアイテム
     * @return 連鎖破壊処理が動くならtrue
     */
    private boolean isChainDestructionActionable(EntityPlayer player, IBlockState state, ItemStack heldItem) {
        return checkBlockValidate(player, state, heldItem) && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(heldItem.getItem()));
    }

    private void setup(IBlockState firstBrokenBlockState, EntityPlayer player, World world, BlockPos blockPos) {
//        setBlockBounds(player, blockPos.getX(), blockPos.getY(), blockPos.getZ());
        CDStatus status = CDStatus.get(player);
        Set<BlockPos> searchedBlockSet = searchBlock(world, status, player, firstBrokenBlockState, blockPos);
        LinkedHashSet<BlockPos> connectedBlockSet = getConnectedBlockSet(status, blockPos, searchedBlockSet);
        if (!ChainDestruction.destroyingSequentially) {
            destroyBlock(world, player, player.getCurrentEquippedItem(), connectedBlockSet);
        } else {
            ChainDestruction.digTaskEvent.digTaskSet.add(new DigTask(player, player.getCurrentEquippedItem(), connectedBlockSet, blockPos));
        }
    }

//    /**
//     * ドロップアイテムがスポーンした際に呼ばれる処理。
//     * @param event EntityJoinWorldEvent
//     */
//    @SuppressWarnings("unused")
////    @SubscribeEvent
//    public void entityItemJoin(EntityJoinWorldEvent event) {
//        if (event.entity instanceof EntityItem && doChain && isEntityItemInRange((EntityItem)event.entity)) {
//            dropItemSet.add((EntityItem)event.entity);
//        }
//    }
//
//    private boolean isEntityItemInRange(EntityItem entityItem) {
//        return dropRangeX.contains(entityItem.posX) && dropRangeY.contains(entityItem.posY) && dropRangeZ.contains(entityItem.posZ);
//    }

    /*ドロップアイテムをプレイヤーのそばに持ってきて拾わせる*/
//    private void getFirstDestroyedBlock(World world, EntityPlayer player) {
//        if (dropItemSet.isEmpty()) return;
//        double d0, d1, d2;
//        float f1 = player.rotationYaw * (float)(2 * Math.PI / 360);
//        for (EntityItem eItem : dropItemSet) {
//            eItem.setNoPickupDelay();
//            d0 = player.posX - MathHelper.sin(f1) * 0.5D;
//            d1 = player.posY + 0.5D;
//            d2 = player.posZ + MathHelper.cos(f1) * 0.5D;
//            eItem.setPosition(d0, d1, d2);
//        }
//        dropItemSet.clear();
//    }

    /**
     * 指定座標のブロックを破壊する処理
     * @param world Worldラクス
     * @param player プレイヤー
     * @param blockPos 指定座標
     * @param item 手持ちアイテム
     * @return 判定アイテムがnullの時やアイテムが壊れた時はtrueを返す。falseで続行。
     */
    public static boolean destroyBlockAtPosition(World world, EntityPlayer player, BlockPos blockPos, ItemStack item) {
        boolean isMultiToolHolder = false;
        int slotNum = 0;
        IBlockState state = world.getBlockState(blockPos);
        IInventory toolData = null;
        if (ChainDestruction.loadMTH && item.getItem() instanceof ItemMultiToolHolder) {
            toolData = ((ItemMultiToolHolder) item.getItem()).getInventoryFromItemStack(item);
            slotNum = ItemMultiToolHolder.getSlotNumFromItemStack(item);
            item = toolData.getStackInSlot(slotNum);
            if (item == null) {
                return true;
            }
            isMultiToolHolder = true;
        }
        boolean startBreakingBlock = item.getItem().onBlockStartBreak(item, blockPos, player);
        boolean blockDestroyed = item.getItem().onBlockDestroyed(item, world, state.getBlock(), blockPos, player);
        if (!startBreakingBlock && blockDestroyed) {
            if (world.setBlockToAir(blockPos)) {
                state.getBlock().onBlockHarvested(world, blockPos, state, player);
                state.getBlock().onBlockDestroyedByPlayer(world, blockPos, state);
                state.getBlock().harvestBlock(world, player, new BlockPos(player.posX, player.posY, player.posZ), state, null);
                if (ChainDestruction.destroyingSequentially) {
                    dropItemNearPlayer(world, player, blockPos);
                }
                if (EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, item) == 0) {
                    int exp = state.getBlock().getExpDrop(world, blockPos, EnchantmentHelper.getFortuneModifier(player));
                    state.getBlock().dropXpOnBlockBreak(world, new BlockPos(player.posX, player.posY, player.posZ), exp);
                }
                if (item.stackSize == 0) {
                    destroyItem(player, item, isMultiToolHolder, toolData, slotNum);
                    return true;
                }
                return isItemBreakingSoon(item);
            }
        }
        return true;
    }

    /**
     * プレイヤーの足元にドロップを集める処理
     * @param world Worldクラス
     * @param player プレイヤー
     * @param blockPos 破壊したブロックの座標
     */
    private static void dropItemNearPlayer(World world, EntityPlayer player, BlockPos blockPos) {
        @SuppressWarnings("unchecked")
        List<EntityItem> entityItemList = world.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.fromBounds(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1).expand(1, 1, 1));
        double d0, d1, d2;
        float f1 = player.rotationYaw * (float)(2 * Math.PI / 360);
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
     * @param player プレイヤー
     * @param item 手持ちアイテム
     * @param isInMultiTool ツールホルダー内のアイテムかどうか
     * @param tools ツールホルダーのインベントリ
     * @param slotnum ツールホルダーのスロット番号
     */
    public static void destroyItem(EntityPlayer player, ItemStack item, boolean isInMultiTool, IInventory tools, int slotnum) {
        if (isInMultiTool) {
            tools.setInventorySlotContents(slotnum, null);
            MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, item));
        } else {
            player.destroyCurrentEquippedItem();
        }
    }

    /**
     * 最初に破壊したブロックと同種ブロックを範囲内から取得
     * @param world Worldクラス
     * @param status 連鎖破壊ステータスクラス
     * @param target 最初に破壊したブロックのIBlockState
     * @param targetPos 最初に破壊したブロックの座標
     * @return 最初に破壊したブロックと同種ブロックの座標集合
     */
    private Set<BlockPos> searchBlock(World world, CDStatus status, EntityPlayer player, IBlockState target, BlockPos targetPos) {
        Set<BlockPos> beBrokenBlockSet = Sets.newHashSet();
        IBlockState checkState;
        BlockPos minPos = this.getMinPos(player, status, targetPos);
        BlockPos maxPos = this.getMaxPos(status, targetPos);
        @SuppressWarnings("unchecked")
        Iterable<BlockPos> allBlockPos = BlockPos.getAllInBox(minPos, maxPos);

        for (BlockPos blockPos : allBlockPos) {
            checkState = world.getBlockState(blockPos);
            if (checkBlock(status, target, checkState, player.getCurrentEquippedItem())) {
                beBrokenBlockSet.add(blockPos);
            }
        }

        return beBrokenBlockSet;
    }

    /**
     * 範囲内の同種ブロックから最初のブロックとつながっているブロックを取得する
     * @param status 連鎖破壊ステータスクラス
     * @param origin 最初に破壊したブロック
     * @param searchedBlockSet 同種ブロックの座標集合
     * @return 最初のブロックとつながっているブロックの座標集合
     */
    private LinkedHashSet<BlockPos> getConnectedBlockSet(CDStatus status, BlockPos origin, Set<BlockPos> searchedBlockSet) {
        LinkedHashSet<BlockPos> connectedBlockLSet = new LinkedHashSet<>();
        connectedBlockLSet.add(origin);
        int distance = status.isTreeMode() ? 3 : 1;
        boolean check = true;
        while(check) {
            check = false;
            Iterator<BlockPos> iterator = searchedBlockSet.iterator();
            while(iterator.hasNext()) {
                BlockPos blockPos = iterator.next();
                for (BlockPos listedBlockPos : connectedBlockLSet) {
                    if (listedBlockPos.distanceSq(blockPos) <= distance) {
                        connectedBlockLSet.add(blockPos);
                        iterator.remove();
                        check = true;
                        break;
                    }
                }
            }
        }
        connectedBlockLSet.remove(origin);
        return connectedBlockLSet;
    }

    /**
     * 第四引数の集合内の座標にあるブロックを破壊する処理
     * @param world Worldクラス
     * @param player プレイヤー
     * @param item 手持ちアイテム
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
     * ツールの耐久値が１以下の時を判定
     * @param itemStack 手持ちアイテム
     * @return 壊れない設定でかつ耐久が1以下の場合true
     */
    private static boolean isItemBreakingSoon(ItemStack itemStack) {
        return ChainDestruction.notToDestroyItem && (itemStack.getMaxDamage() - itemStack.getItemDamage() <= 1);
    }

    /**
     * 壊そうとしているブロックが最初に壊したブロックと同じかどうか判定
     * @param target 最初のブロック
     * @param check 壊そうとしているブロック
     * @param heldItem 手持ちアイテム
     * @return 同種ならtrue
     */
    private boolean checkBlock(CDStatus status, IBlockState target, IBlockState check, ItemStack heldItem) {
        if (check == null || check.getBlock() == Blocks.air) return false;
        if (status.isTreeMode()) {
            String uniqueName = ChainDestruction.getUniqueStrings(heldItem.getItem());
            if (status.isPrivateRegisterMode() && ChainDestruction.privateItemBlockMap.containsKey(uniqueName)) {
                return match(ChainDestruction.privateItemBlockMap.get(uniqueName), check);
            }
            return match(ChainDestruction.enableLogBlocks, check);
        }
        return matchTwoBlocks(target, check);
    }

    /**
     * 破壊範囲の座標が小さい方の端点クラスを取得
     * @param status 連鎖破壊ステータスクラス
     * @param targetPos 最初に破壊したブロック
     * @return 端点クラス
     */
    private BlockPos getMinPos(EntityPlayer player, CDStatus status, BlockPos targetPos) {
        int y = targetPos.getY();
        if (status.isDigUnder()) {
            y = Math.max(Constants.MIN_Y, targetPos.getY() - ChainDestruction.maxDestroyedBlock);
        } else if (EnumFacing.UP != status.getFace()) {
            y = Math.max(Constants.MIN_Y, MathHelper.floor_double(player.posY));
        } else if (ChainDestruction.maxDestroyedBlock > 0){
            y = Math.max(Constants.MIN_Y, MathHelper.floor_double(player.posY) - 1);
        }
        return new BlockPos(targetPos.getX() - ChainDestruction.maxDestroyedBlock, y, targetPos.getZ() - ChainDestruction.maxDestroyedBlock);
    }

    /**
     * 破壊範囲の座標が大きい方の端点クラスを取得
     * @param status 連鎖破壊ステータスクラス
     * @param targetPos 最初に破壊したブロック
     * @return 端点クラス
     */
    private BlockPos getMaxPos(CDStatus status, BlockPos targetPos) {
        int y = (status.isTreeMode())? ChainDestruction.maxYforTreeMode : targetPos.getY() + ChainDestruction.maxDestroyedBlock;;
        return new BlockPos(targetPos.getX() + ChainDestruction.maxDestroyedBlock, y, targetPos.getZ() + ChainDestruction.maxDestroyedBlock);
    }
}