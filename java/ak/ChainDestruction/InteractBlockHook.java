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
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

import java.util.List;
import java.util.Set;

public class InteractBlockHook {
    private int face;
    private Range<Integer> rangeX;
    private Range<Integer> rangeY;
    private Range<Integer> rangeZ;

    private BlockMetaPair blockmeta = null;

    public static final byte RegKEY = 0;
    public static final byte DigKEY = 1;
    public static final byte TreeKEY = 2;

    public boolean digUnder = ChainDestruction.digUnder;
    private boolean treeMode = ChainDestruction.treeMode;
    private boolean doChain = false;

    private static final int sideDirection = 6;
    private static final int diagonalDirection = 26;

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

    @SubscribeEvent
    public void PlayerInteractBlock(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        World world = player.worldObj;
        ItemStack item = event.entityPlayer.getCurrentEquippedItem();
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
            return  match(ChainDestruction.enableBlocks, blockMetaPair);
        }
    }

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

    //コレクションの共通部分を取り、要素があるかどうかで判定。
    private boolean matchOreNames(Set<String> set, List<String> oreNames) {
        for (String string : oreNames) {
            if (set.contains(string)) return true;
        }
        return false;
    }

    private boolean match(Set<String> set, BlockMetaPair blockMetaPair) {
        Block block = blockMetaPair.getBlock();
        int meta = blockMetaPair.getMeta();
        String uidStr = GameRegistry.findUniqueIdentifierFor(block).toString();
        String uidMetaStr = String.format("%s:%d", uidStr, meta);
        List<String> oreNames = ChainDestruction.makeStringDataFromBlockAndMeta(blockMetaPair);
        return matchOreNames(set, oreNames) || matchBlockMetaNames(set, uidStr, uidMetaStr);
    }

    private boolean matchTwoBlocks(BlockMetaPair pair1, BlockMetaPair pair2) {
        List<String> targetOreNames = ChainDestruction.makeStringDataFromBlockAndMeta(pair1);
        List<String> checkOreNames = ChainDestruction.makeStringDataFromBlockAndMeta(pair2);
        for (String str : checkOreNames) {
            if (targetOreNames.contains(str)) return true;
        }
        return false;
    }

    @SubscribeEvent
    public void breakBlockEvent(BlockEvent.BreakEvent event) {
        this.blockmeta = BlockMetaPair.getPair(event.block, event.blockMetadata);
    }

    @SubscribeEvent
    public void HarvestEvent(HarvestDropsEvent event) {
        if (!event.world.isRemote && !doChain
                && (checkBlockValidate(BlockMetaPair.getPair(event.block, event.blockMetadata)) || checkBlockValidate(blockmeta))
                && event.harvester != null
                && event.harvester.getCurrentEquippedItem() != null
                && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(event.harvester.getCurrentEquippedItem().getItem()))) {
            doChain = true;
            setBlockBounds(event.harvester, event.x, event.y, event.z);
            EntityItem ei;
            for (ItemStack stack : event.drops) {
                ei = new EntityItem(event.world, event.harvester.posX, event.harvester.posY, event.harvester.posZ, stack);
                ei.delayBeforeCanPickup = 0;
                event.world.spawnEntityInWorld(ei);
            }
            event.drops.clear();
            ChunkPosition blockChunk = new ChunkPosition(event.x, event.y, event.z);
            ChainDestroyBlock(event.world, event.harvester, blockmeta, blockChunk, event.harvester.getCurrentEquippedItem());
            getFirstDestroyedBlock(event.world, event.harvester, event.block);

            face = 0;
            doChain = false;
        }
    }

    private void getFirstDestroyedBlock(World world, EntityPlayer player, Block block) {
        @SuppressWarnings("unchecked")
        List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, player.boundingBox.expand(2d, 2d, 2d));
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

    private void ChainDestroyBlock(World world, EntityPlayer player, BlockMetaPair target, ChunkPosition blockChunk, ItemStack item) {
        ChunkPosition chunk;
        Block checkingBlock;
        int checkingMeta;
        BlockMetaPair checking = null;
        int sideNumber = treeMode ? diagonalDirection : sideDirection;
        boolean checkBreak = false;

        for (int side = 0; side < sideNumber; side++) {
            if (side == face) continue;
            chunk = this.getNextChunkPosition(blockChunk, side);
            checkingBlock = world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
            checkingMeta = world.getBlockMetadata(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
            checking = BlockMetaPair.setPair(checking, checkingBlock, checkingMeta);
            if (checkChunkInBounds(chunk) && checkBlock(target, checking)) {
                checkBreak = this.searchBlock(world, player, target, chunk, ChainDirection.OPPOSITES[side], item);
            }
            if (checkBreak) break;
        }
    }
    /*判定アイテムがnullの時やアイテムが壊れた時はtrueを返す。falseで続行。*/
    private boolean destroyBlockAtPosition(World world, EntityPlayer player, ChunkPosition chunk, ItemStack item) {
        boolean isMultiToolHolder = false;
        int slotNum = 0;
        Block block = world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
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
        int meta = world.getBlockMetadata(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
        if (item.getItem().onBlockDestroyed(item, world, block, chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, player)) {
            if (world.setBlockToAir(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ)) {
                block.onBlockHarvested(world, chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, meta, player);
                block.onBlockDestroyedByPlayer(world, chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, meta);
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

    public void destroyItem(EntityPlayer player, ItemStack item, boolean isInMultiTool, IInventory tools, int slotnum) {
        if (isInMultiTool) {
            tools.setInventorySlotContents(slotnum, null);
            MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, item));
        } else {
            player.destroyCurrentEquippedItem();
        }
    }

    /*trueで処理の中止。手持ちアイテムが壊れたらtrueを返す*/
    public boolean searchBlock(World world, EntityPlayer player, BlockMetaPair target, ChunkPosition chunkPos, int face, ItemStack heldItem) {
        Block checkingBlock;
        ChunkPosition chunk;
        int checkingMeta;
        BlockMetaPair checking = null;
        if (this.destroyBlockAtPosition(world, player, chunkPos, heldItem))  return true;
        int sideNumber = treeMode ? diagonalDirection : sideDirection;
        boolean checkBreak = false;
        for (int side = 0; side < sideNumber; side++) {
            if (side == face) continue;
            chunk = getNextChunkPosition(chunkPos, side);
            checkingBlock = world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
            checkingMeta = world.getBlockMetadata(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
            checking = BlockMetaPair.setPair(checking, checkingBlock, checkingMeta);
            if (checkChunkInBounds(chunk) && checkBlock(target, checking)) {
                checkBreak = this.searchBlock(world, player, target, chunk, ChainDirection.OPPOSITES[side], heldItem);
            }
            if (checkBreak) break;
        }
        return false;
    }

    private ChunkCoordinates getNextChunkCoordinates(ChunkCoordinates chunk, int side) {
        int dx = ChainDirection.getOrientation(side).offsetX;
        int dy = ChainDirection.getOrientation(side).offsetY;
        int dz = ChainDirection.getOrientation(side).offsetZ;
        chunk.set(chunk.posX, chunk.posY, chunk.posZ);
        return chunk;
    }

    private ChunkPosition getNextChunkPosition(ChunkPosition chunk, int side) {
        int dx = ChainDirection.getOrientation(side).offsetX;
        int dy = ChainDirection.getOrientation(side).offsetY;
        int dz = ChainDirection.getOrientation(side).offsetZ;
        return new ChunkPosition(chunk.chunkPosX + dx, chunk.chunkPosY + dy, chunk.chunkPosZ + dz);
    }

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
    private boolean checkChunkInBounds(ChunkPosition chunk) {
        boolean bx, by, bz;
        bx = rangeX.contains(chunk.chunkPosX);
        by = rangeY.contains(chunk.chunkPosY);
        bz = rangeZ.contains(chunk.chunkPosZ);
        return bx && by && bz;
    }

    private void setBlockBounds(EntityPlayer player, int x, int y, int z) {
        rangeX = Range.closed(x - ChainDestruction.maxDestroyedBlock, x + ChainDestruction.maxDestroyedBlock);
        if (ChainDestruction.digUnder) {
            rangeY = Range.closed(y - ChainDestruction.maxDestroyedBlock, y + ChainDestruction.maxDestroyedBlock);
        } else if (face != 1) {
            rangeY = Range.closed(MathHelper.floor_double(player.posY), y + ChainDestruction.maxDestroyedBlock);
        } else {
            rangeY = Range.closed(MathHelper.floor_double(player.posY) - 1, y + ChainDestruction.maxDestroyedBlock);
        }
        rangeZ = Range.closed(z - ChainDestruction.maxDestroyedBlock, z + ChainDestruction.maxDestroyedBlock);
    }

    /*26方向走査できるようにForgeDirectionを拡張。*/
    public enum ChainDirection {
        /**
         * -Y
         */
        DOWN(0, -1, 0),

        /**
         * +Y
         */
        UP(0, 1, 0),

        /**
         * -Z
         */
        NORTH(0, 0, -1),

        /**
         * +Z
         */
        SOUTH(0, 0, 1),

        /**
         * -X
         */
        WEST(-1, 0, 0),

        /**
         * +X
         */
        EAST(1, 0, 0),

        /**
         * -X-Z
         */
        NORTHWEST(-1, 0, -1),

        /**
         * +X+Z
         */
        SOUTHEAST(1, 0, 1),

        /**
         * -X+Z
         */
        NORTHEAST(-1, 0, 1),

        /**
         * +X-Z
         */
        SOUTHWEST(1, 0, -1),

        /**
         * -Y-Z
         */
        DOWNNORTH(0, -1, -1),

        /**
         * +Y+Z
         */
        UPSOUTH(0, 1, 1),

        /**
         * -X-Y-Z
         */
        DOWNNORTHWEST(-1, -1, -1),

        /**
         * +X+Y+Z
         */
        UPSOUTHEAST(1, 1, 1),

        /**
         * -X-Y
         */
        DOWNWEST(-1, -1, 0),

        /**
         * +X+Y
         */
        UPEAST(1, 1, 0),

        /**
         * -X-Y+Z
         */
        DOWNSOUTHWEST(-1, -1, 1),

        /**
         * +X+Y-Z
         */
        UPNORTHEAST(1, 1, -1),

        /**
         * -Y+Z
         */
        DOWNSOUTH(0, -1, 1),

        /**
         * +Y-Z
         */
        UPNORTH(0, 1, -1),

        /**
         * +X-Y+Z
         */
        DOWNSOUTHEAST(1, -1, 1),

        /**
         * -X+Y-Z
         */
        UPNORTHWEST(-1, 1, -1),

        /**
         * +X-Y
         */
        DOWNEAST(1, -1, 0),

        /**
         * -X+Y
         */
        UPWEST(-1, 1, 0),

        /**
         * +X-Y-Z
         */
        DOWNNORTHEAST(1, -1, -1),

        /**
         * -X+Y+Z
         */
        UPSOUTHWEST(-1, 1, 1),

        /**
         * Used only by getOrientation, for invalid inputs
         */
        UNKNOWN(0, 0, 0);

        public final int offsetX;
        public final int offsetY;
        public final int offsetZ;
        public final int flag;
        public static final ChainDirection[] VALID_DIRECTIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST,
                NORTHWEST, SOUTHEAST, NORTHEAST, SOUTHWEST,
                DOWNNORTH, UPSOUTH, DOWNNORTHWEST, UPSOUTHEAST, DOWNWEST, UPEAST, DOWNSOUTHWEST, UPNORTHEAST, DOWNSOUTH, UPNORTH, DOWNSOUTHEAST, UPNORTHWEST, DOWNEAST, UPWEST, DOWNNORTHEAST, UPSOUTHWEST};
        public static final int[] OPPOSITES = {1, 0, 3, 2, 5, 4, 7, 6, 9, 8, 11, 10, 13, 12, 15, 14, 17, 16, 19, 18, 21, 20, 23, 22, 25, 24, 26};

        private ChainDirection(int x, int y, int z) {
            offsetX = x;
            offsetY = y;
            offsetZ = z;
            flag = 1 << ordinal();
        }

        public static ChainDirection getOrientation(int id) {
            if (id >= 0 && id < VALID_DIRECTIONS.length) {
                return VALID_DIRECTIONS[id];
            }
            return UNKNOWN;
        }

        public ChainDirection getOpposite() {
            return getOrientation(OPPOSITES[ordinal()]);
        }

    }
}