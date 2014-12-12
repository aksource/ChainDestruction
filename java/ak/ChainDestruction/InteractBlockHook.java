package ak.ChainDestruction;

import ak.MultiToolHolders.ItemMultiToolHolder;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

import java.util.*;

public class InteractBlockHook {
    private int face;
    private Range<Integer> rangeX;
    private Range<Integer> rangeY;
    private Range<Integer> rangeZ;
    private Range<Double> dropRangeX;
    private Range<Double> dropRangeY;
    private Range<Double> dropRangeZ;

    private Set<EntityItem> dropItemSet = Sets.newHashSet();

    private BlockMetaPair blockmeta = null;
    private List<ChunkCoordinates> candidateBlockList = new ArrayList<>();
    private List<ChunkCoordinates> destroyingBlockList = new ArrayList<>();

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
            Block block = world.getBlock(event.x, event.y, event.z);
            int meta = world.getBlockMetadata(event.x, event.y, event.z);
            if (event.action == Action.RIGHT_CLICK_BLOCK) {
                if (privateRegisterMode) {
                    if (!ChainDestruction.privateItemBlockMap.containsKey(uniqueName)) {
                        ChainDestruction.privateItemBlockMap.put(uniqueName, new HashSet<String>());
                    }
                    addAndRemoveBlocks(ChainDestruction.privateItemBlockMap.get(uniqueName), player, BlockMetaPair.getPair(block, meta));
                } else {
                    if (treeMode) {
                        addAndRemoveBlocks(ChainDestruction.enableLogBlocks, player, BlockMetaPair.getPair(block, meta));
                    } else {
                        addAndRemoveBlocks(ChainDestruction.enableBlocks, player, BlockMetaPair.getPair(block, meta));
                    }
                }
            }
            if (event.action == Action.LEFT_CLICK_BLOCK
                    && checkBlockValidate(BlockMetaPair.getPair(block, meta), item)
                    && ChainDestruction.enableItems.contains(GameRegistry.findUniqueIdentifierFor(item.getItem()).toString())) {
                face = event.face;
            }
        }
    }

    /*引数はブロックとMeta値。モード別に文字列セットに含まれているかを返す。*/
    private boolean checkBlockValidate(BlockMetaPair blockMetaPair, ItemStack heldItem) {
        if (blockMetaPair == null) {
            return false;
        }
        String uniqueName = ChainDestruction.getUniqueStrings(heldItem.getItem());
        if (privateRegisterMode && ChainDestruction.privateItemBlockMap.containsKey(uniqueName)) {
            return match(ChainDestruction.privateItemBlockMap.get(uniqueName), blockMetaPair);
        }
        if (treeMode) {
            return match(ChainDestruction.enableLogBlocks, blockMetaPair);
        }
        return match(ChainDestruction.enableBlocks, blockMetaPair);
    }

    /*ブロックの登録／削除*/
    private void addAndRemoveBlocks(Set<String> set, EntityPlayer player, BlockMetaPair blockMetaPair) {
        Block block = blockMetaPair.getBlock();
        int meta = blockMetaPair.getMeta();
        //ブロックの固有文字列
        String uidStr = GameRegistry.findUniqueIdentifierFor(block).toString();
        //Meta値付き固有文字列
        String uidMetaStr = String.format("%s:%d", uidStr, meta);
        //鉱石辞書名かMeta値付き固有文字列のリスト
        List<String> oreNames = ChainDestruction.makeStringDataFromBlockAndMeta(blockMetaPair);
        String chat;
        if (player.isSneaking()) {
            //鉱石辞書名かmeta値付き固有文字列があって、登録されて無ければ、そちらを登録。
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
            if (match(set, blockMetaPair)) {
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
    private boolean match(Set<String> set, BlockMetaPair blockMetaPair) {
        Block block = blockMetaPair.getBlock();
        int meta = blockMetaPair.getMeta();
        String uidStr = GameRegistry.findUniqueIdentifierFor(block).toString();
        String uidMetaStr = String.format("%s:%d", uidStr, meta);
        List<String> oreNames = ChainDestruction.makeStringDataFromBlockAndMeta(blockMetaPair);
        return matchOreNames(set, oreNames) || matchBlockMetaNames(set, uidStr, uidMetaStr);
    }

    /*２つのBlockMetaPairが鉱石辞書名経由で等しいかどうか*/
    private boolean matchTwoBlocks(BlockMetaPair pair1, BlockMetaPair pair2) {
        List<String> targetOreNames = ChainDestruction.makeStringDataFromBlockAndMeta(pair1);
        List<String> checkOreNames = ChainDestruction.makeStringDataFromBlockAndMeta(pair2);
        for (String str : checkOreNames) {
            if (targetOreNames.contains(str)) return true;
        }
        return false;
    }

    /*偽装しているブロックへの対応*/
    @SubscribeEvent
    public void breakBlock(BlockEvent.BreakEvent event) {
        this.blockmeta = BlockMetaPair.getPair(event.block, event.blockMetadata);
    }

    @SubscribeEvent
    public void HarvestEvent(HarvestDropsEvent event) {
        if (!event.world.isRemote && !doChain
                && event.harvester != null
                && event.harvester.getCurrentEquippedItem() != null
                /*通常は左の判定だけで良いが、別のブロックに偽装するブロックに対応するため、右の判定を追加*/
                && (checkBlockValidate(BlockMetaPair.getPair(event.block, event.blockMetadata), event.harvester.getCurrentEquippedItem()) || checkBlockValidate(blockmeta, event.harvester.getCurrentEquippedItem()))
                && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(event.harvester.getCurrentEquippedItem().getItem()))) {
            //通常の破壊処理からこのイベントが呼ばれるので、連鎖処理を初回のみにするための処置
            doChain = true;
            setBlockBounds(event.harvester, event.x, event.y, event.z);
            EntityItem ei;
            for (ItemStack stack : event.drops) {
                ei = new EntityItem(event.world, event.harvester.posX, event.harvester.posY, event.harvester.posZ, stack);
                ei.delayBeforeCanPickup = 0;
                event.world.spawnEntityInWorld(ei);
            }
            event.drops.clear();
            ChunkCoordinates blockChunk = new ChunkCoordinates(event.x, event.y, event.z);
            if (searchBlock(event.world, event.harvester, blockmeta, blockChunk)) {
                Collections.sort(candidateBlockList, new CompareToOrigin(blockChunk));
                generateDestroyingBlockList(blockChunk);
                destoryBlock(event.world, event.harvester, event.harvester.getCurrentEquippedItem());
            }
//            ChainDestroyBlock(event.world, event.harvester, blockmeta, blockChunk, event.harvester.getCurrentEquippedItem());
            getFirstDestroyedBlock(event.world, event.harvester);

            face = 0;
            doChain = false;
            this.blockmeta = BlockMetaPair.getPair(Blocks.air, 0);
        }
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
            eItem.delayBeforeCanPickup = 0;
            d0 = player.posX - MathHelper.sin(f1) * 0.5D;
            d1 = player.posY + 0.5D;
            d2 = player.posZ + MathHelper.cos(f1) * 0.5D;
            eItem.setPosition(d0, d1, d2);
        }
        dropItemSet.clear();
    }

    /*判定アイテムがnullの時やアイテムが壊れた時はtrueを返す。falseで続行。*/
    private boolean destroyBlockAtPosition(World world, EntityPlayer player, ChunkCoordinates chunk, ItemStack item) {
        boolean isMultiToolHolder = false;
        int slotNum = 0;
        Block block = world.getBlock(chunk.posX, chunk.posY, chunk.posZ);
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
        int meta = world.getBlockMetadata(chunk.posX, chunk.posY, chunk.posZ);
        boolean startBreakingBlock = item.getItem().onBlockStartBreak(item, chunk.posX, chunk.posY, chunk.posZ, player);
        boolean blockDestroyed = item.getItem().onBlockDestroyed(item, world, block, chunk.posX, chunk.posY, chunk.posZ, player);
        if (!startBreakingBlock && blockDestroyed) {
            if (world.setBlockToAir(chunk.posX, chunk.posY, chunk.posZ)) {
                block.onBlockHarvested(world, chunk.posX, chunk.posY, chunk.posZ, meta, player);
                block.onBlockDestroyedByPlayer(world, chunk.posX, chunk.posY, chunk.posZ, meta);
                block.harvestBlock(world, player, MathHelper.ceiling_double_int(player.posX), MathHelper.ceiling_double_int(player.posY), MathHelper.ceiling_double_int(player.posZ), meta);
                if (EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, item) == 0) {
                    int exp = block.getExpDrop(world, meta, EnchantmentHelper.getFortuneModifier(player));
                    block.dropXpOnBlockBreak(world, MathHelper.ceiling_double_int(player.posX), MathHelper.ceiling_double_int(player.posY), MathHelper.ceiling_double_int(player.posZ), exp);
                }
                if (item.stackSize == 0) {
                    destroyItem(player, item, isMultiToolHolder, tooldata, slotNum);
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    /*手持ちアイテムの破壊処理。ツールホルダーの処理のため。*/
    public void destroyItem(EntityPlayer player, ItemStack item, boolean isInMultiTool, IInventory tools, int slotnum) {
        if (isInMultiTool) {
            tools.setInventorySlotContents(slotnum, null);
            MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, item));
        } else {
            player.destroyCurrentEquippedItem();
        }
    }

    /*target block と同型のブロックの収集*/
    private boolean searchBlock(World world, EntityPlayer player, BlockMetaPair target, ChunkCoordinates targetCoord) {
        BlockMetaPair checkPair = null;
        Block checkBlock;
        int checkMeta;
        ChunkCoordinates chunkCoordinates;
        for (int i = rangeX.lowerEndpoint(); i <= rangeX.upperEndpoint(); i++) {
            for (int j = rangeY.lowerEndpoint(); j <= rangeY.upperEndpoint(); j++) {
                for (int k = rangeZ.lowerEndpoint(); k <= rangeZ.upperEndpoint(); k++) {
                    checkBlock = world.getBlock(i, j, k);
                    checkMeta = world.getBlockMetadata(i, j, k);
                    checkPair = BlockMetaPair.setPair(checkPair, checkBlock, checkMeta);
                    chunkCoordinates = new ChunkCoordinates(i, j, k);
                    if (checkBlock(target, checkPair, player.getCurrentEquippedItem()) && !candidateBlockList.contains(chunkCoordinates)) {
                        candidateBlockList.add(new ChunkCoordinates(i, j, k));
                    }
                }
            }
        }
        candidateBlockList.remove(targetCoord);
        destroyingBlockList.add(targetCoord);
        return !candidateBlockList.isEmpty();
    }

    /*集めたblockから繋がってるものを取得。U型のクラスタはmaxDestroyedBlockの長さまで対応*/
    private void generateDestroyingBlockList(ChunkCoordinates targetCoord) {
        int distance = treeMode ? 3 : 1;
        ChunkCoordinates checkCoord = null;
        for (int count = 0; count < ChainDestruction.maxDestroyedBlock; count++) {
            for (ChunkCoordinates chunkCoordinates : candidateBlockList) {
                for (ChunkCoordinates destroyingCoord : destroyingBlockList) {
                    if (!destroyingCoord.equals(chunkCoordinates) && destroyingCoord.getDistanceSquaredToChunkCoordinates(chunkCoordinates) <= distance) {
                        checkCoord = chunkCoordinates;
                    }
                }
                if (checkCoord != null) {
                    destroyingBlockList.add(checkCoord);
                    checkCoord = null;
                }

            }
            //ループの計算量を減らすため、登録したものは削除。
            candidateBlockList.removeAll(destroyingBlockList);
        }
        //最初のブロックはそもそも破壊されてるので、判定後は削除。
        destroyingBlockList.remove(targetCoord);
    }

    /*destroyingBlockListで登録した座標のブロックを破壊*/
    private void destoryBlock(World world, EntityPlayer player, ItemStack item) {
        for (ChunkCoordinates chunkCoordinates : destroyingBlockList) {
            if (destroyBlockAtPosition(world, player, chunkCoordinates, item)) {
                break;
            }
        }
        candidateBlockList.clear();
        destroyingBlockList.clear();
    }

    /*第一引数は最初に壊したブロック。第二引数は壊そうとしているブロック*/
    private boolean checkBlock(BlockMetaPair target, BlockMetaPair check, ItemStack heldItem) {
        if (check.getBlock() == Blocks.air) return false;
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
        } else if (face != 1) {
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