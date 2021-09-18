package ak.mcmod.chaindestruction.network;

import ak.mcmod.chaindestruction.ChainDestruction;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * ブロック採掘用メッセージハンドラクラス
 * Created by AKIRA on 15/01/13.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MessageDigSoundHandler implements IMessageHandler<MessageDigSound, IMessage> {
  @Override
  @Nullable
  public IMessage onMessage(MessageDigSound message, MessageContext ctx) {
    EntityPlayer player = ChainDestruction.proxy.getEntityPlayer();
    World world = player.getEntityWorld();
    BlockPos blockPos = message.getBlockPos();
    world.playBroadcastSound(2001, blockPos, Block.getStateId(world.getBlockState(blockPos)));
    return null;
  }
}
