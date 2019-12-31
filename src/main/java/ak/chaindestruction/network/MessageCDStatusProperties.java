package ak.chaindestruction.network;

import static ak.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;

import ak.chaindestruction.capability.CDPlayerStatus;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

/**
 * 連鎖破壊用メッセージクラス Created by A.K. on 14/06/02.
 */
public class MessageCDStatusProperties {

  public static BiConsumer<MessageCDStatusProperties, PacketBuffer> encoder = (messageCDStatusProperties, packetBuffer) -> packetBuffer
      .writeCompoundTag(messageCDStatusProperties.getData());
  public static Function<PacketBuffer, MessageCDStatusProperties> decoder = packetBuffer -> new MessageCDStatusProperties(
      packetBuffer.readCompoundTag());

  private CompoundNBT data;

  @SuppressWarnings("unused")
  public MessageCDStatusProperties() {
  }

  public MessageCDStatusProperties(CompoundNBT compound) {
    this.data = compound;
  }

  public MessageCDStatusProperties(PlayerEntity PlayerEntity) {
    this.data = CDPlayerStatus.get(PlayerEntity)
        .map(instance -> {
          CompoundNBT CompoundNBT = (CompoundNBT) CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage()
            .writeNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, instance, null);
          return Objects.nonNull(CompoundNBT) ? CompoundNBT : new CompoundNBT();
        })
        .orElse(new CompoundNBT());
  }

  CompoundNBT getData() {
    return data;
  }
}
