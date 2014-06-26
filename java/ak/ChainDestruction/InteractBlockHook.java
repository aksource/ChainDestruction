package ak.ChainDestruction;

import ak.ChainDestruction.network.MessageKeyPressed;
import ak.ChainDestruction.network.PacketHandler;
import ak.MultiToolHolders.ItemMultiToolHolder;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class InteractBlockHook {
    private int[] blockPos = new int[]{0, 0, 0, 0, 0};
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;

    private static final byte RegKEY = 0;
    private static final byte DigKEY = 1;
    private static final byte TreeKEY = 2;

//    public boolean toggle = false;

    private boolean digUnder = ChainDestruction.digUnder;
    private boolean treeMode = ChainDestruction.treeMode;
    private boolean doChain = false;

    private static final int sideDirection = 6;
    private static final int diagonalDirection = 26;

    private byte getKeyIndex() {
        byte key = -1;
        if (ClientProxy.registItemKey.isPressed()) {
            key = RegKEY;
        } else if (ClientProxy.digUnderKey.isPressed()) {
            key = DigKEY;
        } else if (ClientProxy.treeKey.isPressed()) {
            key = TreeKEY;
        }
        return key;
    }

    @SubscribeEvent
    public void KeyPressEvent(KeyInputEvent event) {
        if (FMLClientHandler.instance().getClient().inGameHasFocus && FMLClientHandler.instance().getClientPlayerEntity() != null) {
            byte keyIndex = getKeyIndex();
            if (keyIndex != -1) {
                EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
                doKeyClient(null, player, keyIndex);
                PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(keyIndex));
            }
        }
    }

    public void doKeyClient(ItemStack item, EntityPlayer player, byte key) {
        if (key == DigKEY) {
            ChainDestruction.digUnder = this.digUnder;
        }
    }

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
            if (event.action == Action.RIGHT_CLICK_BLOCK) {
                if (treeMode) {
                    addAndRemoveBlocks(ChainDestruction.enableLogBlocks, player, block);
                } else {
                    addAndRemoveBlocks(ChainDestruction.enableBlocks, player, block);
                }
            } else if (event.action == Action.LEFT_CLICK_BLOCK
                    && checkBlockValidate(GameRegistry.findUniqueIdentifierFor(block).toString())
                    && ChainDestruction.enableItems.contains(GameRegistry.findUniqueIdentifierFor(item.getItem()).toString())) {
                int meta = world.getBlockMetadata(event.x, event.y, event.z);
                blockPos[0] = event.x;
                blockPos[1] = event.y;
                blockPos[2] = event.z;
                blockPos[3] = event.face;
                blockPos[4] = meta;
            }
        }
    }

    /*引数はブロックの固有文字列。モード別に文字列セットに含まれているかを返す。*/
    private boolean checkBlockValidate(String blockId) {
        if (treeMode) {
            return ChainDestruction.enableLogBlocks.contains(blockId);
        } else {
            return ChainDestruction.enableBlocks.contains(blockId);
        }
    }

    private void addAndRemoveBlocks(HashSet<String> set, EntityPlayer player, Block block) {
        String chat;
        if (player.isSneaking() && !set.contains(GameRegistry.findUniqueIdentifierFor(block).toString())) {
            set.add(ChainDestruction.getUniqueStrings(block));
            chat = String.format("Add Block : %s", GameRegistry.findUniqueIdentifierFor(block).toString());
            player.addChatMessage(new ChatComponentText(chat));
        } else if (!player.isSneaking() && set.contains(GameRegistry.findUniqueIdentifierFor(block).toString())) {
            set.remove(ChainDestruction.getUniqueStrings(block));
            chat = String.format("Remove Block : %s", GameRegistry.findUniqueIdentifierFor(block).toString());
            player.addChatMessage(new ChatComponentText(chat));
        }
    }

    @SubscribeEvent
    public void HarvestEvent(HarvestDropsEvent event) {
        if (!event.world.isRemote && !doChain
                && checkBlockValidate(GameRegistry.findUniqueIdentifierFor(event.block).toString())
                && event.harvester != null
                && event.harvester.getCurrentEquippedItem() != null
                && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(event.harvester.getCurrentEquippedItem().getItem()))) {
            doChain = true;
            setBlockBounds(event.harvester);
            EntityItem ei;
            for (ItemStack stack : event.drops) {
                ei = new EntityItem(event.world, event.harvester.posX, event.harvester.posY, event.harvester.posZ, stack);
                ei.delayBeforeCanPickup = 0;
                event.world.spawnEntityInWorld(ei);
            }
            event.drops.clear();
            ChainDestroyBlock(event.world, event.harvester, event.block, event.harvester.getCurrentEquippedItem());
            getFirstDestroyedBlock(event.world, event.harvester, event.block);

            Arrays.fill(blockPos, 0);
            doChain = false;
        }
    }

    public void getFirstDestroyedBlock(World world, EntityPlayer player, Block block) {
        @SuppressWarnings("unchecked")
        List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, player.boundingBox.expand(2d, 2d, 2d));
        if (list == null) return;
        double d0, d1, d2;
        float f1 = player.rotationYaw * (float)(2 * Math.PI / 360);
//        int i1 = EnchantmentHelper.getFortuneModifier(player);
        for (EntityItem eItem : list) {
//			if(eItem.getEntityItem().getItem() instanceof ItemBlock && GameRegistry.findUniqueIdentifierFor(block).equals(GameRegistry.findUniqueIdentifierFor(eItem.getEntityItem().getItem()))
//					|| GameRegistry.findUniqueIdentifierFor(block.getItemDropped(blockPos[4], world.rand, i1)).equals(GameRegistry.findUniqueIdentifierFor(eItem.getEntityItem().getItem()))){
            eItem.delayBeforeCanPickup = 0;
            d0 = player.posX - MathHelper.sin(f1) * 0.5D;
            d1 = player.posY + 0.5D;
            d2 = player.posZ + MathHelper.cos(f1) * 0.5D;
            eItem.setPosition(d0, d1, d2);
//			}
        }
    }

    public void ChainDestroyBlock(World world, EntityPlayer player, Block targetBlock, ItemStack item) {
        ChunkPosition chunk;
        Block checkingBlock;

        int sideNumber = treeMode ? diagonalDirection : sideDirection;
        boolean checkBreak = false;

        for (int side = 0; side < sideNumber; side++) {
            if (side == blockPos[3]) continue;
            chunk = this.getNextChunkPosition(new ChunkPosition(blockPos[0], blockPos[1], blockPos[2]), side);
            checkingBlock = world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
            if (checkChunkInBounds(chunk) && checkBlock(GameRegistry.findUniqueIdentifierFor(targetBlock).toString(), GameRegistry.findUniqueIdentifierFor(checkingBlock).toString())) {
                checkBreak = this.searchBlock(world, player, targetBlock, chunk, ChainDirection.OPPOSITES[side], item);
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
            slotNum = ((ItemMultiToolHolder) item.getItem()).getSlotNumFromItemStack(item);
            item = tooldata.getStackInSlot(slotNum);
            isMultiToolHolder = true;
        }
        int meta = world.getBlockMetadata(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
        if (item == null) return true;
        else if (item.getItem().onBlockDestroyed(item, world, block, chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, player)) {
            if (world.setBlockToAir(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ)) {
                block.onBlockHarvested(world, chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, meta, player);
                block.onBlockDestroyedByPlayer(world, chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, meta);
                block.harvestBlock(world, player, MathHelper.ceiling_double_int(player.posX), MathHelper.ceiling_double_int(player.posY), MathHelper.ceiling_double_int(player.posZ), meta);
                int exp = block.getExpDrop(world, meta, EnchantmentHelper.getFortuneModifier(player));
                block.dropXpOnBlockBreak(world, MathHelper.ceiling_double_int(player.posX), MathHelper.ceiling_double_int(player.posY), MathHelper.ceiling_double_int(player.posZ), exp);
                if (item.stackSize == 0) {
                    destroyItem(player, item, isMultiToolHolder, tooldata, slotNum);
                    return true;
                } else return false;
            } else return true;
        } else return true;
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
    public boolean searchBlock(World world, EntityPlayer player, Block targetBlock, ChunkPosition chunkPos, int face, ItemStack heldItem) {
        Block checkingBlock;
        ChunkPosition chunk;
        if (this.destroyBlockAtPosition(world, player, chunkPos, heldItem))  return true;
        int sideNumber = treeMode ? diagonalDirection : sideDirection;
        boolean checkBreak = false;
        for (int side = 0; side < sideNumber; side++) {
            if (side == face) continue;
            chunk = getNextChunkPosition(chunkPos, side);
            checkingBlock = world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
            if (checkChunkInBounds(chunk) && checkBlock(GameRegistry.findUniqueIdentifierFor(targetBlock).toString(), GameRegistry.findUniqueIdentifierFor(checkingBlock).toString())/*GameRegistry.findUniqueIdentifierFor(block).equals(GameRegistry.findUniqueIdentifierFor(id))*/) {
                checkBreak = this.searchBlock(world, player, targetBlock, chunk, ChainDirection.OPPOSITES[side], heldItem);
            }
            if (checkBreak) break;
        }
        return false;
    }

    private ChunkPosition getNextChunkPosition(ChunkPosition chunk, int side) {
        int dx = ChainDirection.getOrientation(side).offsetX;
        int dy = ChainDirection.getOrientation(side).offsetY;
        int dz = ChainDirection.getOrientation(side).offsetZ;
        return new ChunkPosition(chunk.chunkPosX + dx, chunk.chunkPosY + dy, chunk.chunkPosZ + dz);
    }

    /*第一引数は最初に壊したブロック。第二引数は壊そうとしているブロック*/
    private boolean checkBlock(String targetId, String checkId) {
        if (treeMode) {
            return ChainDestruction.enableLogBlocks.contains(checkId);
        } else {
            return targetId.equals(checkId);
        }
    }

    /*与えられた座標が採掘範囲内かどうか*/
    private boolean checkChunkInBounds(ChunkPosition chunk) {
        boolean bx, by, bz;
        bx = chunk.chunkPosX >= minX && chunk.chunkPosX <= maxX;
        by = chunk.chunkPosY >= minY && chunk.chunkPosY <= maxY;
        bz = chunk.chunkPosZ >= minZ && chunk.chunkPosZ <= maxZ;
        return bx && by && bz;
    }

    private void setBlockBounds(EntityPlayer player) {
        minX = blockPos[0] - ChainDestruction.maxDestroyedBlock;
        maxX = blockPos[0] + ChainDestruction.maxDestroyedBlock;
        if (ChainDestruction.digUnder)
            minY = blockPos[1] - ChainDestruction.maxDestroyedBlock;
        else if (blockPos[3] != 1)
            minY = MathHelper.floor_double(player.posY);
        else
            minY = MathHelper.floor_double(player.posY) - 1;
        maxY = blockPos[1] + ChainDestruction.maxDestroyedBlock;
        minZ = blockPos[2] - ChainDestruction.maxDestroyedBlock;
        maxZ = blockPos[2] + ChainDestruction.maxDestroyedBlock;
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