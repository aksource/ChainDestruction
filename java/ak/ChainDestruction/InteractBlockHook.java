package ak.ChainDestruction;

import ak.MultiToolHolders.ItemMultiToolHolder;
import com.google.common.collect.Range;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
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
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class InteractBlockHook {
    private int face;
    private Range<Integer> rangeX;
    private Range<Integer> rangeY;
    private Range<Integer> rangeZ;

    private BlockMetaPair blockmeta = null;
    private List<ChunkCoordinates> candidateBlockList = new ArrayList<>();
    private List<ChunkCoordinates> destroyingBlockList = new ArrayList<>();

    public static final byte RegKEY = 0;
    public static final byte DigKEY = 1;
    public static final byte TreeKEY = 2;

    public static final byte MIDDLE_CLICK = 2;
    public static final byte RIGHT_CLICK_CTRL = 1 + 3;

    public boolean digUnder;
    private boolean treeMode;
    private boolean doChain = false;

    private static final int SIDE_DIRECTION_NUMBER = 6;
    private static final int DIAGONAL_DIRECTION_NUMBER = 26;

    public static double dropItemGetRange = 10d;

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
        } else if (key == DigKEY) {
            this.digUnder = !this.digUnder;
            chat = String.format("Dig Under %b", this.digUnder);
            player.addChatMessage(new ChatComponentText(chat));
            ChainDestruction.digUnder = this.digUnder;
        } else if (key == TreeKEY) {
            this.treeMode = !this.treeMode;
            chat = String.format("Tree Mode %b", this.treeMode);
            player.addChatMessage(new ChatComponentText(chat));
        }
    }

    public void doMouseEvent(ItemStack item, EntityPlayer player, byte mouse, boolean isFocusObject) {
        if (!ChainDestruction.enableItems.contains(GameRegistry.findUniqueIdentifierFor(item.getItem()).toString())) {
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
    }


    @SubscribeEvent
    public void PlayerInteractBlock(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        World world = player.worldObj;
        ItemStack item = event.entityPlayer.getCurrentEquippedItem();
        if (world.isRemote) return;
        if (item != null && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(item.getItem()))) {
            Block block = world.getBlock(event.x, event.y, event.z);
            int meta = world.getBlockMetadata(event.x, event.y, event.z);
            if (event.action == Action.RIGHT_CLICK_BLOCK) {
                if (treeMode) {
                    addAndRemoveBlocks(ChainDestruction.enableLogBlocks, player, BlockMetaPair.getPair(block, meta));
                } else {
                    addAndRemoveBlocks(ChainDestruction.enableBlocks, player, BlockMetaPair.getPair(block, meta));
                }
            }
            if (event.action == Action.LEFT_CLICK_BLOCK
                    && checkBlockValidate(BlockMetaPair.getPair(block, meta))
                    && ChainDestruction.enableItems.contains(GameRegistry.findUniqueIdentifierFor(item.getItem()).toString())) {
                face = event.face;
            }
        }
    }

    /*引数はブロックとMeta値。モード別に文字列セットに含まれているかを返す。*/
    private boolean checkBlockValidate(BlockMetaPair blockMetaPair) {
        if (blockMetaPair == null) {
            return false;
        }
        if (treeMode) {
            return match(ChainDestruction.enableLogBlocks, blockMetaPair);
        } else {
            return match(ChainDestruction.enableBlocks, blockMetaPair);
        }
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
            } else {//いらない文字列を削除。
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

    @SubscribeEvent
    public void HarvestEvent(HarvestDropsEvent event) {
        if (!event.world.isRemote && !doChain
                && (checkBlockValidate(BlockMetaPair.getPair(event.block, event.blockMetadata)) || checkBlockValidate(blockmeta))
                && event.harvester != null
                && event.harvester.getCurrentEquippedItem() != null
                && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(event.harvester.getCurrentEquippedItem().getItem()))) {
            //通常の破壊処理からこのイベントが呼ばれるので、連載処理を初回のみにするための処置
            doChain = true;
            this.blockmeta = BlockMetaPair.getPair(event.block, event.blockMetadata);
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
            getFirstDestroyedBlock(event.world, event.harvester, event.block);

            face = 0;
            doChain = false;
        }
    }

    /*ドロップアイテムをプレイヤーのそばに持ってきて拾わせる*/
    private void getFirstDestroyedBlock(World world, EntityPlayer player, Block block) {
        @SuppressWarnings("unchecked")
        List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, player.boundingBox.expand(dropItemGetRange, dropItemGetRange, dropItemGetRange));
        if (list == null) return;
        double d0, d1, d2;
        float f1 = player.rotationYaw * (float)(2 * Math.PI / 360);
        for (EntityItem eItem : list) {
            eItem.delayBeforeCanPickup = 0;
            d0 = player.posX - MathHelper.sin(f1) * 0.5D;
            d1 = player.posY + 0.5D;
            d2 = player.posZ + MathHelper.cos(f1) * 0.5D;
            eItem.setPosition(d0, d1, d2);
        }
    }

//    private void ChainDestroyBlock(World world, EntityPlayer player, BlockMetaPair target, ChunkCoordinates blockChunk, ItemStack item) {
//        ChunkCoordinates chunk;
//        Block checkingBlock;
//        int checkingMeta;
//        BlockMetaPair checking = null;
//        int sideNumber = treeMode ? DIAGONAL_DIRECTION_NUMBER : SIDE_DIRECTION_NUMBER;
//        boolean checkBreak = false;
//
//        for (int side = 0; side < sideNumber; side++) {
//            if (side == face) continue;
//            chunk = this.getNextChunkPosition(blockChunk, side);
//            checkingBlock = world.getBlock(chunk.posX, chunk.posY, chunk.posZ);
//            checkingMeta = world.getBlockMetadata(chunk.posX, chunk.posY, chunk.posZ);
//            checking = BlockMetaPair.setPair(checking, checkingBlock, checkingMeta);
//            if (checkChunkInBounds(chunk) && checkBlock(target, checking)) {
//                checkBreak = this.searchBlock(world, player, target, chunk, ChainDirection.OPPOSITES[side], item);
//            }
//            if (checkBreak) break;
//        }
//    }
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
                int exp = block.getExpDrop(world, meta, EnchantmentHelper.getFortuneModifier(player));
                block.dropXpOnBlockBreak(world, MathHelper.ceiling_double_int(player.posX), MathHelper.ceiling_double_int(player.posY), MathHelper.ceiling_double_int(player.posZ), exp);
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
                    if (checkBlock(target, checkPair) && !candidateBlockList.contains(chunkCoordinates)) {
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
        boolean addFlag = false;
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

    /*trueで処理の中止。手持ちアイテムが壊れたらtrueを返す*/
//    public boolean searchBlock(World world, EntityPlayer player, BlockMetaPair target, ChunkCoordinates chunkPos, int face, ItemStack heldItem) {
//        Block checkingBlock;
//        ChunkCoordinates chunk;
//        int checkingMeta;
//        BlockMetaPair checking = null;
//        if (this.destroyBlockAtPosition(world, player, chunkPos, heldItem))  return true;
//        int sideNumber = treeMode ? DIAGONAL_DIRECTION_NUMBER : SIDE_DIRECTION_NUMBER;
//        boolean checkBreak = false;
//        for (int side = 0; side < sideNumber; side++) {
//            if (side == face) continue;
//            chunk = getNextChunkPosition(chunkPos, side);
//            checkingBlock = world.getBlock(chunk.posX, chunk.posY, chunk.posZ);
//            checkingMeta = world.getBlockMetadata(chunk.posX, chunk.posY, chunk.posZ);
//            checking = BlockMetaPair.setPair(checking, checkingBlock, checkingMeta);
//            if (checkChunkInBounds(chunk) && checkBlock(target, checking)) {
//                checkBreak = this.searchBlock(world, player, target, chunk, ChainDirection.OPPOSITES[side], heldItem);
//            }
//            if (checkBreak) break;
//        }
//        return false;
//    }

//    private ChunkCoordinates getNextChunkCoordinates(ChunkCoordinates chunk, int side) {
//        int dx = ChainDirection.getOrientation(side).offsetX;
//        int dy = ChainDirection.getOrientation(side).offsetY;
//        int dz = ChainDirection.getOrientation(side).offsetZ;
//        chunk.set(chunk.posX, chunk.posY, chunk.posZ);
//        return chunk;
//    }
//
//    private ChunkCoordinates getNextChunkPosition(ChunkCoordinates chunk, int side) {
//        int dx = ChainDirection.getOrientation(side).offsetX;
//        int dy = ChainDirection.getOrientation(side).offsetY;
//        int dz = ChainDirection.getOrientation(side).offsetZ;
//        return new ChunkCoordinates(chunk.posX + dx, chunk.posY + dy, chunk.posZ + dz);
//    }

    /*第一引数は最初に壊したブロック。第二引数は壊そうとしているブロック*/
    private boolean checkBlock(BlockMetaPair target, BlockMetaPair check) {
        if (check.getBlock() == Blocks.air) return false;
        if (treeMode) {
            return match(ChainDestruction.enableLogBlocks, check);
        } else {
            return matchTwoBlocks(target, check);
        }
    }

    /*与えられた座標が採掘範囲内かどうか*/
//    private boolean checkChunkInBounds(ChunkCoordinates chunk) {
//        boolean bx, by, bz;
//        bx = rangeX.contains(chunk.posX);
//        by = rangeY.contains(chunk.posY);
//        bz = rangeZ.contains(chunk.posZ);
//        return bx && by && bz;
//    }

    /*破壊予定ブロックの走査範囲の設定*/
    private void setBlockBounds(EntityPlayer player, int x, int y, int z) {
        rangeX = Range.closed(x - ChainDestruction.maxDestroyedBlock, x + ChainDestruction.maxDestroyedBlock);
        int maxY = (treeMode)? ChainDestruction.maxYforTreeMode : y + ChainDestruction.maxDestroyedBlock;
        if (ChainDestruction.digUnder) {
            rangeY = Range.closed(y - ChainDestruction.maxDestroyedBlock, maxY);
        } else if (face != 1) {
            rangeY = Range.closed(MathHelper.floor_double(player.posY), maxY);
        } else {
            rangeY = Range.closed(MathHelper.floor_double(player.posY) - 1, maxY);
        }
        rangeZ = Range.closed(z - ChainDestruction.maxDestroyedBlock, z + ChainDestruction.maxDestroyedBlock);
    }

    public void setDigUnder(boolean digUnder) {
        this.digUnder = digUnder;
    }

    public void setTreeMode(boolean treeMode) {
        this.treeMode = treeMode;
    }
}