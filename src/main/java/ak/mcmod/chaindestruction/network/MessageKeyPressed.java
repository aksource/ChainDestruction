package ak.mcmod.chaindestruction.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * キー押下用メッセージクラス
 * Created by A.K. on 14/06/01.
 */
public class MessageKeyPressed implements IMessage {

  public byte key;

  @SuppressWarnings("unused")
  public MessageKeyPressed() {
  }

  public MessageKeyPressed(byte keyPressed) {
    this.key = keyPressed;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.key = buf.readByte();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeByte(this.key);
  }
}