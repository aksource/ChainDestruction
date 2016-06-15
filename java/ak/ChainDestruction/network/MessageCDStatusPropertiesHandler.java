package ak.ChainDestruction.network;

import ak.ChainDestruction.CDStatus;
import ak.ChainDestruction.ChainDestruction;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by A.K. on 14/07/31.
 */
public class MessageCDStatusPropertiesHandler implements IMessageHandler<MessageCDStatusProperties, IMessage> {
    @Override
    public IMessage onMessage(MessageCDStatusProperties message, MessageContext ctx) {
        CDStatus.get(ChainDestruction.proxy.getEntityPlayer()).loadNBTData(message.data);
        return null;
    }
}
