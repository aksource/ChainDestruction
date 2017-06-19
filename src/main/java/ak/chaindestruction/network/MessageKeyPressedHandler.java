package ak.chaindestruction.network;

import ak.chaindestruction.ChainDestruction;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * キー押下用メッセージハンドラクラス
 * Created by A.K. on 14/07/31.
 */
public class MessageKeyPressedHandler implements IMessageHandler<MessageKeyPressed, IMessage> {
    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        if (ctx.getServerHandler().player != null) {
            EntityPlayer player = ctx.getServerHandler().player;
            ChainDestruction.interactblockhook.doKeyEvent(player.getHeldItemMainhand(), player, message.key);
        }
        return null;
    }
}
