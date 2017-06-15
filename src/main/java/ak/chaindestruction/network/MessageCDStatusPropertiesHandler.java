package ak.chaindestruction.network;

import ak.chaindestruction.capability.CDPlayerStatus;
import ak.chaindestruction.ChainDestruction;
import ak.chaindestruction.capability.ICDPlayerStatusHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static ak.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;

/**
 * 連鎖破壊ステータスハンドラクラス
 * Created by A.K. on 14/07/31.
 */
public class MessageCDStatusPropertiesHandler implements IMessageHandler<MessageCDStatusProperties, IMessage> {
    @Override
    public IMessage onMessage(MessageCDStatusProperties message, MessageContext ctx) {
        ICDPlayerStatusHandler instance = CDPlayerStatus.get(ChainDestruction.proxy.getEntityPlayer());
        CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage().readNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, instance, null, message.data);
        return null;
    }
}
