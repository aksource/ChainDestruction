package ak.ChainDestruction.network;

import ak.ChainDestruction.ChainDestruction;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by AKIRA on 15/01/13.
 */
public class MessageDigSoundHandler implements IMessageHandler<MessageDigSound, IMessage> {
    @Override
    public IMessage onMessage(MessageDigSound message, MessageContext ctx) {
        EntityPlayer player = ChainDestruction.proxy.getEntityPlayer();
        World world = player.worldObj;
        BlockPos blockPos = message.getBlockPos();
        world.playAuxSFXAtEntity(player, 2001, blockPos, Block.getStateId(world.getBlockState(blockPos)));
        return null;
    }
}
