package ak.mcmod.chaindestruction.network;

import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;

/**
 * 連鎖破壊用メッセージクラス Created by A.K. on 14/06/02.
 */
public class MessageCDStatusProperties {

  public static final BiConsumer<MessageCDStatusProperties, PacketBuffer> ENCODER = (messageCDStatusProperties, packetBuffer) -> packetBuffer
          .writeNbt(messageCDStatusProperties.getData());
  public static final Function<PacketBuffer, MessageCDStatusProperties> DECODER = packetBuffer -> new MessageCDStatusProperties(
          packetBuffer.readNbt());

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
