package ak.mcmod.chaindestruction.network;

import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * キー押下用メッセージクラス Created by A.K. on 14/06/01.
 */
public class MessageKeyPressed {

  public static final BiConsumer<MessageKeyPressed, PacketBuffer> encoder = ((messageKeyPressed, packetBuffer) -> packetBuffer
      .writeByte(messageKeyPressed.getKey()));
  public static final Function<PacketBuffer, MessageKeyPressed> decoder = packetBuffer -> new MessageKeyPressed(
      packetBuffer.readByte());
  private byte key;

  @SuppressWarnings("unused")
  public MessageKeyPressed() {
  }

  public MessageKeyPressed(byte keyPressed) {
    this.key = keyPressed;
  }

  byte getKey() {
    return key;
  }
}