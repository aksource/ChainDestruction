package ak.mcmod.chaindestruction.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static ak.mcmod.chaindestruction.capability.CapabilityAdditionalPlayerStatus.CAPABILITY;

/**
 * 連鎖破壊ステータスハンドラクラス Created by A.K. on 14/07/31.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MessageSyncAdditionalStatusHandler implements BiConsumer<MessageSyncAdditionalPayerStatus, Supplier<NetworkEvent.Context>> {

  @Override
  public void accept(MessageSyncAdditionalPayerStatus messageCDStatusProperties, Supplier<NetworkEvent.Context> contextSupplier) {
    if (Objects.nonNull(Minecraft.getInstance().player)) {
      Minecraft.getInstance().player.getCapability(CAPABILITY).ifPresent(
              instance -> instance.deserializeNBT(messageCDStatusProperties.getData()));
    }
  }
}
