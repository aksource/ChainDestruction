package ak.ChainDestruction.network;

import ak.ChainDestruction.ChainDestruction;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by A.K. on 14/07/31.
 */
@SideOnly(Side.SERVER)
public class MessageKeyPressedHandler implements IMessageHandler<MessageKeyPressed, IMessage> {
    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        if (ctx.getServerHandler().playerEntity != null) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            ChainDestruction.interactblockhook.doKeyEvent(player.getCurrentEquippedItem(), player, message.key);
        }
        return null;
    }
}
