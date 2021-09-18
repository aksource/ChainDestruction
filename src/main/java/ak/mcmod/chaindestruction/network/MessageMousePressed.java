package ak.mcmod.chaindestruction.network;

import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * マウスクリック用メッセージクラス Created by A.K. on 14/10/14.
 */
public class MessageMousePressed {

  public static final BiConsumer<MessageMousePressed, PacketBuffer> ENCODER = (messageMousePressed, packetBuffer) -> packetBuffer.writeByte(messageMousePressed.getMouseIndex())
          .writeBoolean(messageMousePressed.isFocusObject);

  public static final Function<PacketBuffer, MessageMousePressed> DECODER = packetBuffer -> new MessageMousePressed(
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
