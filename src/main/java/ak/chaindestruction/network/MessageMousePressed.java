package ak.chaindestruction.network;

import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.PacketBuffer;

/**
 * マウスクリック用メッセージクラス Created by A.K. on 14/10/14.
 */
public class MessageMousePressed {

  public static BiConsumer<MessageMousePressed, PacketBuffer> encoder = (messageMousePressed, packetBuffer) -> {
    packetBuffer.writeByte(messageMousePressed.getMouseIndex())
        .writeBoolean(messageMousePressed.isFocusObject);
  };

  public static Function<PacketBuffer, MessageMousePressed> decoder = packetBuffer -> new MessageMousePressed(
      packetBuffer.readByte(), packetBuffer.readBoolean());
  private byte mouseIndex;
  private boolean isFocusObject;

  @SuppressWarnings("unused")
  public MessageMousePressed() {
  }

  public MessageMousePressed(byte mouseIndex, boolean isFocusObject) {
    this.mouseIndex = mouseIndex;
    this.isFocusObject = isFocusObject;
  }

  byte getMouseIndex() {
    return this.mouseIndex;
  }

  boolean isFocusObject() {
    return this.isFocusObject;
  }
}
