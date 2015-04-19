package ak.ChainDestruction.network;

import ak.ChainDestruction.ChainDestruction;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

/**
 * Created by AKIRA on 15/01/13.
 */
public class MessageDigSoundHandler implements IMessageHandler<MessageDigSound, IMessage> {
    @Override
    public IMessage onMessage(MessageDigSound message, MessageContext ctx) {
        EntityPlayer player = ChainDestruction.proxy.getEntityPlayer();
        World world = player.worldObj;
        ChunkCoordinates blockPos = message.getChunkCoordinates();
        int x = blockPos.posX;
        int y = blockPos.posY;
        int z = blockPos.posZ;
        world.playAuxSFXAtEntity(player, 2001, x, y, z, Block.getIdFromBlock(world.getBlock(x, y, z)) + (world.getBlockMetadata(x, y, z) << 12));
        return null;
    }
}
