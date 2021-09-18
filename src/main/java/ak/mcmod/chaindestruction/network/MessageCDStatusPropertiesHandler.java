package ak.mcmod.chaindestruction.network;

import ak.mcmod.chaindestruction.ChainDestruction;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.ICDPlayerStatusHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;

/**
 * 連鎖破壊ステータスハンドラクラス
 * Created by A.K. on 14/07/31.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MessageCDStatusPropertiesHandler implements IMessageHandler<MessageCDStatusProperties, IMessage> {
  @Override
  @Nullable
  public IMessage onMessage(MessageCDStatusProperties message, MessageContext ctx) {
    ICDPlayerStatusHandler instance = CDPlayerStatus.get(ChainDestruction.proxy.getEntityPlayer());
    CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage().readNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, instance, null, message.data);
    return null;
  }
}
