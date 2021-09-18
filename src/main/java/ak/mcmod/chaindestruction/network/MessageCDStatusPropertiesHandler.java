package ak.mcmod.chaindestruction.network;

import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;

/**
 * 連鎖破壊ステータスハンドラクラス Created by A.K. on 14/07/31.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MessageCDStatusPropertiesHandler implements
        BiConsumer<MessageCDStatusProperties, Supplier<Context>> {

  @Override
  public void accept(MessageCDStatusProperties messageCDStatusProperties,
                     Supplier<Context> contextSupplier) {
    if (Objects.nonNull(Minecraft.getInstance().player)) {
      CDPlayerStatus.get(Minecraft.getInstance().player)
              .ifPresent(instance -> CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage()
                      .readNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, instance, null,
                              messageCDStatusProperties.getData()));
    }
  }
}
