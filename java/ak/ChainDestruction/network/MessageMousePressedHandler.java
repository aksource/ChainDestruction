package ak.ChainDestruction.network;

import ak.ChainDestruction.ChainDestruction;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Created by A.K. on 14/10/14.
 */
public class MessageMousePressedHandler implements IMessageHandler<MessageMousePressed, IMessage> {
    @Override
    public IMessage onMessage(MessageMousePressed message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null && player.getCurrentEquippedItem() != null) {
            ItemStack equippedItem = player.getCurrentEquippedItem();
            ChainDestruction.interactblockhook.doMouseEvent(equippedItem, player, message.getMouseIndex(), message.isFocusObject());
        }
        return null;
    }
}
