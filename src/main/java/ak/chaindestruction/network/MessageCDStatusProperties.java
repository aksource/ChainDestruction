package ak.chaindestruction.network;

import static ak.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;

import ak.chaindestruction.capability.CDPlayerStatus;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

/**
 * 連鎖破壊用メッセージクラス Created by A.K. on 14/06/02.
 */
public class MessageCDStatusProperties {

  public static BiConsumer<MessageCDStatusProperties, PacketBuffer> encoder = (messageCDStatusProperties, packetBuffer) -> packetBuffer
      .writeCompoundTag(messageCDStatusProperties.getData());
  public static Function<PacketBuffer, MessageCDStatusProperties> decoder = packetBuffer -> new MessageCDStatusProperties(
      packetBuffer.readCompoundTag());

  private NBTTagCompound data;

  @SuppressWarnings("unused")
  public MessageCDStatusProperties() {
  }

  public MessageCDStatusProperties(NBTTagCompound compound) {
    this.data = compound;
  }

  public MessageCDStatusProperties(EntityPlayer entityPlayer) {
    this.data = CDPlayerStatus.get(entityPlayer)
        .map(instance -> {
          NBTTagCompound nbtTagCompound = (NBTTagCompound) CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage()
            .writeNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, instance, null);
          return Objects.nonNull(nbtTagCompound) ? nbtTagCompound : new NBTTagCompound();
        })
        .orElse(new NBTTagCompound());
  }

  NBTTagCompound getData() {
    return data;
  }
}
