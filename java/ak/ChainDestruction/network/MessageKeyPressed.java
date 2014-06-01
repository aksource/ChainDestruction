package ak.ChainDestruction.network;

import ak.ChainDestruction.ChainDestruction;
import ak.MultiToolHolders.IKeyEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by A.K. on 14/06/01.
 */
public class MessageKeyPressed implements IMessage, IMessageHandler<MessageKeyPressed, IMessage> {

    private byte key;

    public MessageKeyPressed() {
    }

    public MessageKeyPressed(byte keyPressed) {
        this.key = keyPressed;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.key = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.key);
    }

    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        if (ctx.getServerHandler().playerEntity != null) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            ChainDestruction.interactblockhook.doKeyEvent(player.getCurrentEquippedItem(), player, message.key);
        }
        return null;
    }
}