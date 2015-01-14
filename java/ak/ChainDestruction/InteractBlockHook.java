package ak.ChainDestruction;

import ak.MultiToolHolders.ItemMultiToolHolder;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.*;

public class InteractBlockHook {
    private EnumFacing face;
    private IBlockState state;
    private Range<Integer> rangeX;
    private Range<Integer> rangeY;
    private Range<Integer> rangeZ;
    private Range<Double> dropRangeX;
    private Range<Double> dropRangeY;
    private Range<Double> dropRangeZ;

    private Set<EntityItem> dropItemSet = Sets.newHashSet();

    private List<BlockPos> candidateBlockList = new ArrayList<>();
    private LinkedHashSet<BlockPos> destroyingBlockSet = new LinkedHashSet<>();

    public static final byte RegKEY = 0;
    public static final byte DigKEY = 1;
    public static final byte ModeKEY = 2;

    public static final byte MIDDLE_CLICK = 2;
    public static final byte RIGHT_CLICK_CTRL = 1 + 10;

    public boolean digUnder;
    private boolean treeMode;
    public boolean privateRegisterMode;
    private boolean doChain = false;

    public void doKeyEvent(ItemStack item, EntityPlayer player, byte key) {
        String chat;
        if (key == RegKEY) {
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
        if (key == DigKEY) {
            this.digUnder = !this.digUnder;
            chat = String.format("Dig Under %b", this.digUnder);
            player.addChatMessage(new ChatComponentText(chat));
            ChainDestruction.digUnder = this.digUnder;
        }
        if (key == ModeKEY) {
            if (player.isSneaking()) {
                this.privateRegisterMode = !this.privateRegisterMode;
                chat = String.format("Private Register Mode %b", this.privateRegisterMode);
                player.addChatMessage(new ChatComponentText(chat));
            } else {
                this.treeMode = !this.treeMode;
                chat = String.format("Tree Mode %b", this.treeMode);
                player.addChatMessage(new ChatComponentText(chat));
            }
        }
    }

    public void doMouseEvent(ItemStack item, EntityPlayer player, byte mouse, boolean isFocusObject) {
        try {
            if (!ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(item))) {
                return;
            }
            String chat;
            if (mouse == MIDDLE_CLICK && !isFocusObject) {
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

    @SubscribeEvent
    public void interactBlock(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        World world = player.worldObj;
        ItemStack item = event.entityPlayer.getCurrentEquippedItem();
        if (world.isRemote || item == null) return;
        String uniqueName = ChainDestruction.getUniqueStrings(item.getItem());
        if (ChainDestruction.enableItems.contains(uniqueName)) {
            IBlockState state = world.getBlockState(event.pos);
//            Block block = world.getBlockState(event.pos).getBlock();
            if (event.action == Action.RIGHT_CLICK_BLOCK) {
                if (privateRegisterMode) {
                    if (!ChainDestruction.privateItemBlockMap.containsKey(uniqueName)) {
                        ChainDestruction.privateItemBlockMap.put(uniqueName, new HashSet<String>());
                    }
                    addAndRemoveBlocks(ChainDestruction.privateItemBlockMap.get(uniqueName), player, state);
                } else {
                    if (treeMode) {
                        addAndRemoveBlocks(ChainDestruction.enableLogBlocks, player, state);
                    } else {
                        addAndRemoveBlocks(ChainDestruction.enableBlocks, player, state);
                    }
                }
            }
            if (event.action == Action.LEFT_CLICK_BLOCK
                    && checkBlockValidate(state, item)
                    && ChainDestruction.enableItems.contains(GameRegistry.findUniqueIdentifierFor(item.getItem()).toString())) {
                face = event.face;
            }
        }
    }

    /*引数はBlockState。モード別に文字列セットに含まれているかを返す。*/
    private boolean checkBlockValidate(IBlockState state, ItemStack heldItem) {
        if (state == null) {
            return false;
        }
        String uniqueName = ChainDestruction.getUniqueStrings(heldItem.getItem());
        if (privateRegisterMode && ChainDestruction.privateItemBlockMap.containsKey(uniqueName)) {
            return match(ChainDestruction.privateItemBlockMap.get(uniqueName), state);
        }
        if (treeMode) {
            return match(ChainDestruction.enableLogBlocks, state);
        }
        return match(ChainDestruction.enableBlocks, state);
    }

    /*ブロックの登録／削除*/
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

    private boolean matchBlockMetaNames(Set<String> set, String uid, String uidmeta) {
        return set.contains(uid) || set.contains(uidmeta);
    }

    /*コレクションの共通部分を取り、要素があるかどうかで判定。*/
    private boolean matchOreNames(Set<String> set, List<String> oreNames) {
        for (String string : oreNames) {
            if (set.contains(string)) return true;
        }
        return false;
    }

    /*与えられた集合内に該当のものがあるかどうか*/
    private boolean match(Set<String> set, IBlockState state) {
        Block block = state.getBlock();
        String uidStr = GameRegistry.findUniqueIdentifierFor(block).toString();
        String uidMetaStr = state.toString();
        List<String> oreNames = ChainDestruction.makeStringDataFromBlockState(state);
        return matchOreNames(set, oreNames) || matchBlockMetaNames(set, uidStr, uidMetaStr);
    }

    /*２つのBlockMetaPairが鉱石辞書名経由で等しいかどうか*/
    private boolean matchTwoBlocks(IBlockState pair1, IBlockState pair2) {
        List<String> targetOreNames = ChainDestruction.makeStringDataFromBlockState(pair1);
        List<String> checkOreNames = ChainDestruction.makeStringDataFromBlockState(pair2);
        for (String str : checkOreNames) {
            if (targetOreNames.contains(str)) return true;
        }
        return false;
    }

    /*偽装しているブロックへの対応含めこちらで処理したほうが良い*/
    @SubscribeEvent
    public void breakBlock(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof FakePlayer) && !event.world.isRemote) {
            this.state = event.state;
            if (isChainDestructionActionable(state, event.getPlayer().getCurrentEquippedItem())) {
                setup(event.getPlayer(), event.world, event.pos);
            }
        }
    }
    /*BlockBreakEventに処理を移行した todo 削除予定*/
//    @SubscribeEvent
    public void HarvestEvent(HarvestDropsEvent event) {
        if (!event.world.isRemote && !doChain
                && event.harvester != null
                && event.harvester.getCurrentEquippedItem() != null
                /*通常は左の判定だけで良いが、別のブロックに偽装するブロックに対応するため、右の判定を追加*/
                && (checkBlockValidate(event.state, event.harvester.getCurrentEquippedItem()) || checkBlockValidate(state, event.harvester.getCurrentEquippedItem()))
                && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(event.harvester.getCurrentEquippedItem().getItem()))) {
            //通常の破壊処理からこのイベントが呼ばれるので、連鎖処理を初回のみにするための処置
            doChain = true;
            EntityItem ei;
            for (ItemStack stack : event.drops) {
                ei = new EntityItem(event.world, event.harvester.posX, event.harvester.posY, event.harvester.posZ, stack);
                ei.setNoPickupDelay();
                event.world.spawnEntityInWorld(ei);
            }
            event.drops.clear();
            setup(event.harvester, event.world, event.pos);
            doChain = false;
        }
    }

    private boolean isChainDestructionActionable(IBlockState state, ItemStack heldItem) {
        return checkBlockValidate(state, heldItem) && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(heldItem.getItem()));
    }

    private void setup(EntityPlayer player, World world, BlockPos blockPos) {
        setBlockBounds(player, blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (searchBlock(world, player, state, blockPos)) {
            Collections.sort(candidateBlockList, new CompareToOrigin(blockPos));
            generateDestroyingBlockList(blockPos);
            if (!ChainDestruction.destroyingSequentially) {
                destroyBlock(world, player, player.getCurrentEquippedItem());
            } else {
                ChainDestruction.digTaskEvent.digTaskSet.add(new DigTask(player, player.getCurrentEquippedItem(), destroyingBlockSet, blockPos));
                candidateBlockList.clear();
                destroyingBlockSet.clear();
            }
        }
        getFirstDestroyedBlock(world, player);

        face = EnumFacing.DOWN;
        this.state = Blocks.air.getDefaultState();
    }

    @SubscribeEvent
    public void entityItemJoin(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityItem && doChain && isEntityItemInRange((EntityItem)event.entity)) {
            dropItemSet.add((EntityItem)event.entity);
        }
    }

    private boolean isEntityItemInRange(EntityItem entityItem) {
        return dropRangeX.contains(entityItem.posX) && dropRangeY.contains(entityItem.posY) && dropRangeZ.contains(entityItem.posZ);
    }

    /*ドロップアイテムをプレイヤーのそばに持ってきて拾わせる*/
    private void getFirstDestroyedBlock(World world, EntityPlayer player) {
        if (dropItemSet.isEmpty()) return;
        double d0, d1, d2;
        float f1 = player.rotationYaw * (float)(2 * Math.PI / 360);
        for (EntityItem eItem : dropItemSet) {
            eItem.setNoPickupDelay();
            d0 = player.posX - MathHelper.sin(f1) * 0.5D;
            d1 = player.posY + 0.5D;
            d2 = player.posZ + MathHelper.cos(f1) * 0.5D;
            eItem.setPosition(d0, d1, d2);
        }
        dropItemSet.clear();
    }

    /*判定アイテムがnullの時やアイテムが壊れた時はtrueを返す。falseで続行。*/
    public static boolean destroyBlockAtPosition(World world, EntityPlayer player, BlockPos blockPos, ItemStack item) {
        boolean isMultiToolHolder = false;
        int slotNum = 0;
        IBlockState state = world.getBlockState(blockPos);
        IInventory tooldata = null;
        if (ChainDestruction.loadMTH && item.getItem() instanceof ItemMultiToolHolder) {
            tooldata = ((ItemMultiToolHolder) item.getItem()).getInventoryFromItemStack(item);
            slotNum = ItemMultiToolHolder.getSlotNumFromItemStack(item);
            item = tooldata.getStackInSlot(slotNum);
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
                    destroyItem(player, item, isMultiToolHolder, tooldata, slotNum);
                    return true;
                }
                return isItemBreakingSoon(item);
            }
        }
        return true;
    }

    private static void dropItemNearPlayer(World world, EntityPlayer player, BlockPos blockPos) {
        IBlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
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

    /*手持ちアイテムの破壊処理。ツールホルダーの処理のため。*/
    public static void destroyItem(EntityPlayer player, ItemStack item, boolean isInMultiTool, IInventory tools, int slotnum) {
        if (isInMultiTool) {
            tools.setInventorySlotContents(slotnum, null);
            MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, item));
        } else {
            player.destroyCurrentEquippedItem();
        }
    }

    /*target block と同型のブロックの収集*/
    private boolean searchBlock(World world, EntityPlayer player, IBlockState target, BlockPos targetPos) {
        IBlockState checkState;
        BlockPos blockPos;
        for (int i = rangeX.lowerEndpoint(); i <= rangeX.upperEndpoint(); i++) {
            for (int j = rangeY.lowerEndpoint(); j <= rangeY.upperEndpoint(); j++) {
                for (int k = rangeZ.lowerEndpoint(); k <= rangeZ.upperEndpoint(); k++) {
                    blockPos = new BlockPos(i, j, k);
                    checkState = world.getBlockState(blockPos);
                    if (checkBlock(target, checkState, player.getCurrentEquippedItem()) && !candidateBlockList.contains(blockPos)) {
                        candidateBlockList.add(blockPos);
                    }
                }
            }
        }
        candidateBlockList.remove(targetPos);
        destroyingBlockSet.add(targetPos);
        return !candidateBlockList.isEmpty();
    }

    /*集めたblockから繋がってるものを取得。U型のクラスタはmaxDestroyedBlockの長さまで対応*/
    private void generateDestroyingBlockList(BlockPos targetPos) {
        int distance = treeMode ? 3 : 1;
        BlockPos checkPos = null;
        for (int count = 0; count < ChainDestruction.maxDestroyedBlock; count++) {
            for (BlockPos blockPos : candidateBlockList) {
                for (BlockPos destroyingCoord : destroyingBlockSet) {
                    if (!destroyingCoord.equals(blockPos) && destroyingCoord.distanceSq(blockPos) <= distance) {
                        checkPos = blockPos;
                    }
                }
                if (checkPos != null) {
                    destroyingBlockSet.add(checkPos);
                    checkPos = null;
                }

            }
            //ループの計算量を減らすため、登録したものは削除。
            candidateBlockList.removeAll(destroyingBlockSet);
        }
        //最初のブロックはそもそも破壊されてるので、判定後は削除。
        destroyingBlockSet.remove(targetPos);
    }

    /*destroyingBlockListで登録した座標のブロックを破壊*/
    private void destroyBlock(World world, EntityPlayer player, ItemStack item) {
        for (BlockPos blockPos : destroyingBlockSet) {
            if (destroyBlockAtPosition(world, player, blockPos, item)) {
                break;
            }
        }
        candidateBlockList.clear();
        destroyingBlockSet.clear();
    }

    /*ツールの耐久値が１以下の時を判定*/
    private static boolean isItemBreakingSoon(ItemStack itemStack) {
        return ChainDestruction.notToDestroyItem && (itemStack.getMaxDamage() - itemStack.getItemDamage() <= 1);
    }

    /*第一引数は最初に壊したブロック。第二引数は壊そうとしているブロック*/
    private boolean checkBlock(IBlockState target, IBlockState check, ItemStack heldItem) {
        if (check == null || check.getBlock() == Blocks.air) return false;
        if (treeMode) {
            String uniqueName = ChainDestruction.getUniqueStrings(heldItem.getItem());
            if (privateRegisterMode && ChainDestruction.privateItemBlockMap.containsKey(uniqueName)) {
                return match(ChainDestruction.privateItemBlockMap.get(uniqueName), check);
            }
            return match(ChainDestruction.enableLogBlocks, check);
        }
        return matchTwoBlocks(target, check);
    }

    /*破壊予定ブロックの走査範囲の設定*/
    private void setBlockBounds(EntityPlayer player, int x, int y, int z) {
        rangeX = Range.closed(x - ChainDestruction.maxDestroyedBlock, x + ChainDestruction.maxDestroyedBlock);
        dropRangeX = Range.closed((double)(x - ChainDestruction.maxDestroyedBlock - 1), (double)(x + ChainDestruction.maxDestroyedBlock + 1));
        int maxY = (treeMode)? ChainDestruction.maxYforTreeMode : y + ChainDestruction.maxDestroyedBlock;
        if (ChainDestruction.digUnder) {
            rangeY = Range.closed(y - ChainDestruction.maxDestroyedBlock, maxY);
            dropRangeY = Range.closed((double)(y - ChainDestruction.maxDestroyedBlock), (double)maxY);
        } else if (face != EnumFacing.UP) {
            rangeY = Range.closed(MathHelper.floor_double(player.posY), maxY);
            dropRangeY = Range.closed(player.posY, (double)maxY);
        } else {
            rangeY = Range.closed(MathHelper.floor_double(player.posY) - 1, maxY);
            dropRangeY = Range.closed(player.posY - 1, (double)maxY);
        }
        rangeZ = Range.closed(z - ChainDestruction.maxDestroyedBlock, z + ChainDestruction.maxDestroyedBlock);
        dropRangeZ = Range.closed((double)(z - ChainDestruction.maxDestroyedBlock - 1), (double)(z + ChainDestruction.maxDestroyedBlock + 1));
    }

    public void setDigUnder(boolean digUnder) {
        this.digUnder = digUnder;
    }

    public void setTreeMode(boolean treeMode) {
        this.treeMode = treeMode;
    }

    public void setPrivateRegisterMode(boolean privateRegisterMode) {
        this.privateRegisterMode = privateRegisterMode;
    }
}