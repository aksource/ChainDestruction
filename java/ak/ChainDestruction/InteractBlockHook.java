package ak.ChainDestruction;

import ak.MultiToolHolders.ItemMultiToolHolder;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class InteractBlockHook
{
	private int[] blockPos = new int[]{0,0,0,0,0};
	private int minX;
	private int maxX;
	private int minY;
	private int maxY;
	private int minZ;
	private int maxZ;
	public boolean toggle = false;
	public boolean digUnderToggle = false;
    public boolean treeToggle = false;
	private boolean digUnder = ChainDestruction.digUnder;
    private boolean treeMode = ChainDestruction.treeMode;
	private boolean doChain = false;
	
	private boolean pressRegisterKey = false;
	private boolean pressDigUnderKey = false;
	private boolean pressTreeKey = false;

    private int sideDirection = 6;
    private int diagonalDirection = 26;

	@SubscribeEvent
	public void KeyPressEvent(KeyInputEvent event)
	{
		while(ClientProxy.registItemKey.isPressed()){
			this.pressRegisterKey = true;
		}
		while(ClientProxy.digUnderKey.isPressed()){
			this.pressDigUnderKey = true;
		}
        while(ClientProxy.treeKey.isPressed()) {
            this.pressTreeKey = true;
        }
	}
	
	@SubscribeEvent
	public void PlayerInteractBlock(PlayerInteractEvent event)
	{
		EntityPlayer player = event.entityPlayer;
		World world = player.worldObj;
		ItemStack item = event.entityPlayer.getCurrentEquippedItem();
		if(item != null && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(item.getItem())))
		{
			Block block = world.getBlock(event.x, event.y, event.z);
			if(event.action == Action.RIGHT_CLICK_BLOCK)
			{
                if (treeMode) {
                    addAndRemoveBlocks(ChainDestruction.enableLogBlocks, player, block);
                } else {
                    addAndRemoveBlocks(ChainDestruction.enableBlocks, player, block);
                }
			}
			else if(event.action == Action.LEFT_CLICK_BLOCK
					&& checkBlockValidate(GameRegistry.findUniqueIdentifierFor(block).toString())
					&& ChainDestruction.enableItems.contains(GameRegistry.findUniqueIdentifierFor(item.getItem()).toString()))
			{
				int meta = world.getBlockMetadata(event.x, event.y, event.z);
				blockPos[0] = event.x;
				blockPos[1] = event.y;
				blockPos[2] = event.z;
				blockPos[3] = event.face;
				blockPos[4] = meta;
			}
		}
	}


    private boolean checkBlockValidate(String blockid) {
        if (treeMode) {
            return ChainDestruction.enableLogBlocks.contains(blockid);
        } else {
            return ChainDestruction.enableBlocks.contains(blockid);
        }
    }

    private void addAndRemoveBlocks(HashSet<String> set, EntityPlayer player, Block block) {
        String chat;
        if(player.isSneaking() && !set.contains(GameRegistry.findUniqueIdentifierFor(block).toString()))
        {
            set.add(ChainDestruction.getUniqueStrings(block));
            chat = String.format("Add Block : %s", GameRegistry.findUniqueIdentifierFor(block).toString());
            player.addChatMessage(new ChatComponentTranslation(chat, new Object[0]));
        }
        else if(!player.isSneaking() && set.contains(GameRegistry.findUniqueIdentifierFor(block).toString()))
        {
            set.remove(ChainDestruction.getUniqueStrings(block));
            chat = String.format("Remove Block : %s", GameRegistry.findUniqueIdentifierFor(block).toString());
            player.addChatMessage(new ChatComponentTranslation(chat, new Object[0]));
        }
    }

	@SubscribeEvent
	public void HarvestEvent(HarvestDropsEvent event)
	{
		if(!event.world.isRemote && !doChain && checkBlockValidate(GameRegistry.findUniqueIdentifierFor(event.block).toString()) &&  event.harvester.getCurrentEquippedItem() != null && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings( event.harvester.getCurrentEquippedItem().getItem())))
		{
			doChain = true;
			setBlockBounds(event.harvester);
			EntityItem ei;
			for(ItemStack stack:event.drops){
				ei = new EntityItem(event.world, event.harvester.posX,event.harvester.posY, event.harvester.posZ, stack);
				ei.delayBeforeCanPickup = 0;
				event.world.spawnEntityInWorld(ei);
			}
			event.drops.clear();
			ChainDestroyBlock(event.world,event.harvester,event.block, event.harvester.getCurrentEquippedItem());
			getFirstDestroyedBlock(event.world,event.harvester,event.block,  event.harvester.getCurrentEquippedItem());

			Arrays.fill(blockPos, 0);
			doChain = false;
		}
	}
	@SubscribeEvent
	public void LivingUpdate(LivingUpdateEvent event)
	{
		if(event.entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			ItemStack item = player.getCurrentEquippedItem();
			World world = event.entityLiving.worldObj;
			String chat;
			if(world.isRemote)
			{
				this.toggle = this.pressRegisterKey;
				this.digUnderToggle = this.pressDigUnderKey;
                this.treeToggle = this.pressTreeKey;
				//キー判定のboolean変数をpacketに載せて、サーバーに送信。
				ChainDestruction.packetPipeline.sendToServer(new KeyHandlingPacket(toggle, digUnderToggle, treeToggle));
			}
			if(this.toggle && item != null)
			{
				pressRegisterKey = false;
				if(player.isSneaking() && ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(item)))
				{
					ChainDestruction.enableItems.remove(ChainDestruction.getUniqueStrings(item));
					chat = String.format("Remove Tool : %s", ChainDestruction.getUniqueStrings(item));
					player.addChatMessage(new ChatComponentTranslation(chat, new Object[0]));
				}
				if(!player.isSneaking() && !ChainDestruction.enableItems.contains(ChainDestruction.getUniqueStrings(item)))
				{
					ChainDestruction.enableItems.add(ChainDestruction.getUniqueStrings(item));
					chat = String.format("Add Tool : %s", ChainDestruction.getUniqueStrings(item));
					player.addChatMessage(new ChatComponentTranslation(chat, new Object[0]));
				}
			}
			if(this.digUnderToggle && !world.isRemote)
			{
				pressDigUnderKey = false;
				this.digUnder = !this.digUnder;
				chat = String.format("Dig Under %b", this.digUnder);
				player.addChatMessage(new ChatComponentTranslation(chat, new Object[0]));
			}
            if (this.treeToggle && !world.isRemote) {
                pressTreeKey = false;
                this.treeMode = !this.treeMode;
                chat = String.format("Dig Under %b", this.treeMode);
                player.addChatMessage(new ChatComponentTranslation(chat, new Object[0]));
            }
			ChainDestruction.digUnder = this.digUnder;
		}
	}

	public void getFirstDestroyedBlock(World world, EntityPlayer player, Block block, ItemStack item)
	{
		List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, player.boundingBox.expand(5d, 5d, 5d));
		if(list == null)
			return;
		double d0;
		double d1;
		double d2;
		float f1 = player.rotationYaw * 0.01745329F;
		int i1 = EnchantmentHelper.getFortuneModifier(player);
		for(EntityItem eItem: list){
			if(eItem.getEntityItem().getItem() instanceof ItemBlock && GameRegistry.findUniqueIdentifierFor(block).equals(GameRegistry.findUniqueIdentifierFor(eItem.getEntityItem().getItem()))
					|| GameRegistry.findUniqueIdentifierFor(block.getItemDropped(blockPos[4], world.rand, i1)).equals(GameRegistry.findUniqueIdentifierFor(eItem.getEntityItem().getItem()))){
				eItem.delayBeforeCanPickup = 0;
				d0 = player.posX - MathHelper.sin(f1) * 0.5D;
				d1 = player.posY + 0.5D;
				d2 = player.posZ + MathHelper.cos(f1) * 0.5D;
				eItem.setPosition(d0, d1, d2);
			}
		}
	}
	public void ChainDestroyBlock(World world, EntityPlayer player, Block block, ItemStack item)
	{
		ChunkPosition chunk;
		Block id;

        int sideNumber = treeMode ? this.diagonalDirection : this.sideDirection;

		for(int side = 0;side < sideNumber;side++)
		{
			if(side == blockPos[3]){
				continue;
			}
			chunk = this.getNextChunkPosition(new ChunkPosition(blockPos[0], blockPos[1], blockPos[2]), side);
			id = world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
			if(checkChunkInBounds(chunk) && checkBlock(GameRegistry.findUniqueIdentifierFor(block).toString(), GameRegistry.findUniqueIdentifierFor(id).toString()))
			{
				this.searchBlock(world, player, block, chunk, ChainDirection.OPPOSITES[side], item);
			}
		}
	}
	private boolean destroyBlockAtPosition(World world, EntityPlayer player, ChunkPosition chunk, ItemStack item)
	{
		boolean isMultiToolHolder = false;
		int slotNum = 0;
        Block block = world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
		IInventory tooldata = null;
        if (ChainDestruction.loadMTH && item.getItem() instanceof ItemMultiToolHolder)
        {
            tooldata = ((ItemMultiToolHolder) item.getItem()).tools;
            slotNum = ((ItemMultiToolHolder) item.getItem()).SlotNum;
            item = ((IInventory) tooldata).getStackInSlot(slotNum);
            isMultiToolHolder = true;
        }
		int meta = world.getBlockMetadata(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
		if(item == null){
			return true;
		}
		if(item.getItem().onBlockDestroyed(item, world, block, chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, player)){
			if(world.setBlockToAir(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ)){
                block.onBlockHarvested(world, chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, meta, player);
				block.onBlockDestroyedByPlayer(world,chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, meta);
				block.harvestBlock(world, player, MathHelper.ceiling_double_int( player.posX), MathHelper.ceiling_double_int( player.posY), MathHelper.ceiling_double_int( player.posZ), meta);
                int exp = block.getExpDrop(world, meta, EnchantmentHelper.getFortuneModifier(player));
                block.dropXpOnBlockBreak(world, MathHelper.ceiling_double_int( player.posX), MathHelper.ceiling_double_int( player.posY), MathHelper.ceiling_double_int( player.posZ), exp);
				if(item.stackSize == 0){
					destroyItem(player, item, isMultiToolHolder, tooldata, slotNum);
					return true;
				}else return false;
			}else return true;
		}else return true;
	}
	public void destroyItem(EntityPlayer player, ItemStack item, boolean isInMultiTool, IInventory tools, int slotnum)
	{
		if(isInMultiTool){
			tools.setInventorySlotContents(slotnum, null);
			MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, item));
		}else{
			player.destroyCurrentEquippedItem();
		}
	}
	public void searchBlock(World world, EntityPlayer player, Block block, ChunkPosition chunkpos, int face, ItemStack heldItem)
	{
		Block id;
		ChunkPosition chunk;
		if(this.destroyBlockAtPosition(world,player, chunkpos, heldItem)){
			return;
		}
        int sideNumber = treeMode ? this.diagonalDirection : this.sideDirection;

		for(int side = 0;side < sideNumber;side++){
			if(side == face){
				continue;
			}
			chunk = getNextChunkPosition(chunkpos, side);
			id = world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
			if(checkChunkInBounds(chunk) && checkBlock(GameRegistry.findUniqueIdentifierFor(block).toString(), GameRegistry.findUniqueIdentifierFor(id).toString())/*GameRegistry.findUniqueIdentifierFor(block).equals(GameRegistry.findUniqueIdentifierFor(id))*/){
				this.searchBlock(world, player, block, chunk, ChainDirection.OPPOSITES[side], heldItem);
			}
		}
	}
	private ChunkPosition getNextChunkPosition(ChunkPosition chunk, int side)
	{
		int dx = ChainDirection.getOrientation(side).offsetX;
		int dy = ChainDirection.getOrientation(side).offsetY;
		int dz = ChainDirection.getOrientation(side).offsetZ;
		return new ChunkPosition(chunk.chunkPosX + dx,chunk.chunkPosY + dy,chunk.chunkPosZ + dz);
	}

    private boolean checkBlock(String targetId, String checkId) {
        if (treeMode) {
            return ChainDestruction.enableLogBlocks.contains(checkId);
        } else {
            return targetId.equals(checkId);
        }
    }
    
	public boolean checkChunkInBounds(ChunkPosition chunk)
	{
		boolean bx,by,bz;
		bx = chunk.chunkPosX >= minX && chunk.chunkPosX <= maxX;
		by = chunk.chunkPosY >= minY && chunk.chunkPosY <= maxY;
		bz = chunk.chunkPosZ >= minZ && chunk.chunkPosZ <= maxZ;
		return bx && by && bz;
	}
	public void setBlockBounds(EntityPlayer player)
	{
		minX = blockPos[0] - ChainDestruction.maxDestroyedBlock;
		maxX = blockPos[0] + ChainDestruction.maxDestroyedBlock;
		if(ChainDestruction.digUnder)
			minY = blockPos[1] - ChainDestruction.maxDestroyedBlock;
		else if(blockPos[3] != 1)
			minY = MathHelper.floor_double(player.posY);
		else
			minY = MathHelper.floor_double(player.posY) - 1;
		maxY = blockPos[1] + ChainDestruction.maxDestroyedBlock;
		minZ = blockPos[2] - ChainDestruction.maxDestroyedBlock;
		maxZ = blockPos[2] + ChainDestruction.maxDestroyedBlock;
	}

    public enum ChainDirection {
        /** -Y */
        DOWN(0, -1, 0),

        /** +Y */
        UP(0, 1, 0),

        /** -Z */
        NORTH(0, 0, -1),

        /** +Z */
        SOUTH(0, 0, 1),

        /** -X */
        WEST(-1, 0, 0),

        /** +X */
        EAST(1, 0, 0),
	    
	    /** -X-Z */
	    NORTHWEST(-1, 0, -1),
	    
	    /** +X+Z */
	    SOUTHEAST(1, 0, 1),
	    
	    /** -X+Z */
	    NORTHEAST(-1, 0, 1),
	    
	    /** +X-Z */
	    SOUTHWEST(1, 0, -1),
		
        /** -Y-Z */
        DOWNNORTH(0, -1, -1),

        /** +Y+Z */
        UPSOUTH(0, 1, 1),

        /** -X-Y-Z */
        DOWNNORTHWEST(-1, -1, -1),

        /** +X+Y+Z */
        UPSOUTHEAST(1, 1, 1),

        /** -X-Y */
        DOWNWEST(-1, -1, 0),

        /** +X+Y */
        UPEAST(1, 1, 0),

        /** -X-Y+Z */
        DOWNSOUTHWEST(-1, -1, 1),

        /** +X+Y-Z */
        UPNORTHEAST(1, 1, -1),

        /** -Y+Z */
        DOWNSOUTH(0, -1, 1),

        /** +Y-Z */
        UPNORTH(0, 1, -1),

        /** +X-Y+Z */
        DOWNSOUTHEAST(1, -1, 1),

        /** -X+Y-Z */
        UPNORTHWEST(-1, 1, -1),

        /** +X-Y */
        DOWNEAST(1, -1, 0),

        /** -X+Y */
        UPWEST(-1, 1, 0),

        /** +X-Y-Z */
        DOWNNORTHEAST(1, -1, -1),

        /** -X+Y+Z */
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

        private ChainDirection(int x, int y, int z)
        {
            offsetX = x;
            offsetY = y;
            offsetZ = z;
            flag = 1 << ordinal();
        }

        public static ChainDirection getOrientation(int id)
        {
            if (id >= 0 && id < VALID_DIRECTIONS.length)
            {
                return VALID_DIRECTIONS[id];
            }
            return UNKNOWN;
        }

        public ChainDirection getOpposite()
        {
            return getOrientation(OPPOSITES[ordinal()]);
        }

    }
}