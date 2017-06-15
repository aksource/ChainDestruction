package ak.chaindestruction.network;

import ak.chaindestruction.ChainDestruction;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * マウスクリック用メッセージハンドラクラス
 * Created by A.K. on 14/10/14.
 */
public class MessageMousePressedHandler implements IMessageHandler<MessageMousePressed, IMessage> {
    @Override
    public IMessage onMessage(MessageMousePressed message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null && player.getHeldItemMainhand() != null) {
            ItemStack equippedItem = player.getHeldItemMainhand();
            ChainDestruction.interactblockhook.doMouseEvent(equippedItem, player, message.getMouseIndex(), message.isFocusObject());
        }
        return null;
    }
}
