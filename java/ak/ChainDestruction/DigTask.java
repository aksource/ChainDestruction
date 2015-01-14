package ak.ChainDestruction;

import ak.ChainDestruction.network.MessageDigSound;
import ak.ChainDestruction.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.util.LinkedHashSet;

/**
 * Created by AKIRA on 15/01/13.
 */
public class DigTask {
    private LinkedHashSet<ChunkCoordinates> blockToDestroySet = new LinkedHashSet<>();
    private EntityPlayer digger;
    private ItemStack heldItem;
    private int counter;
    public DigTask(EntityPlayer player, ItemStack itemStack, LinkedHashSet<ChunkCoordinates> blockPosSet, ChunkCoordinates origin) {
        this.digger = player;
        this.heldItem = itemStack;
        this.blockToDestroySet.addAll(blockPosSet);
    }

    //return true : when all block destroyed or heldItem broken
    public boolean increaseCount() {
        counter++;
        if (counter >= ChainDestruction.digTaskMaxCounter) {
            counter = 0;
            return destroyBlock();
        }
        return false;
    }

    public boolean destroyBlock() {
        if (blockToDestroySet.isEmpty()) return true;
        ChunkCoordinates first = blockToDestroySet.iterator().next();
        blockToDestroySet.remove(first);
        World world = this.digger.worldObj;
        int x = first.posX;
        int y = first.posY;
        int z = first.posZ;
        world.playAuxSFXAtEntity(digger, 2001, x, y, z,  Block.getIdFromBlock(world.getBlock(x, y, z)) + (world.getBlockMetadata(x, y, z) << 12));
        PacketHandler.INSTANCE.sendTo(new MessageDigSound(first), (EntityPlayerMP)digger);
        return InteractBlockHook.destroyBlockAtPosition(world, digger, first, heldItem);
    }

    public EntityPlayer getDigger() {
        return digger;
    }
}
