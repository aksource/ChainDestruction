package ak.mcmod.chaindestruction.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static ak.mcmod.chaindestruction.capability.CapabilityAdditionalPlayerStatus.CAPABILITY;

/**
 * 連鎖破壊用メッセージクラス Created by A.K. on 14/06/02.
 */
public class MessageSyncAdditionalPayerStatus {

  public static final BiConsumer<MessageSyncAdditionalPayerStatus, FriendlyByteBuf> ENCODER = (messageCDStatusProperties, packetBuffer) -> packetBuffer
          .writeNbt(messageCDStatusProperties.getData());
  public static final Function<FriendlyByteBuf, MessageSyncAdditionalPayerStatus> DECODER = packetBuffer -> new MessageSyncAdditionalPayerStatus(
          packetBuffer.readNbt());

  private CompoundTag data;

  @SuppressWarnings("unused")
  public MessageSyncAdditionalPayerStatus() {
  }

  public MessageSyncAdditionalPayerStatus(CompoundTag compound) {
    this.data = compound;
  }

  public MessageSyncAdditionalPayerStatus(Player player) {
    this.data = player.getCapability(CAPABILITY)
            .map(instance -> {
              var compoundTag = instance.serializeNBT();
              return Objects.nonNull(compoundTag) ? compoundTag : new CompoundTag();
            })
            .orElse(new CompoundTag());
  }

  CompoundTag getData() {
    return data;
  }
}
