package ak.ChainDestruction.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * Created by A.K. on 14/10/14.
 */
public class MessageMousePressed implements IMessage {
    private byte mouseIndex;
    private boolean isFocusObject;

    public MessageMousePressed(){}

    public MessageMousePressed(byte mouseIndex, boolean isFocusObject) {
        this.mouseIndex = mouseIndex;
        this.isFocusObject = isFocusObject;
    }
    @Override
    public void fromBytes(ByteBuf buf) {
        this.mouseIndex = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.mouseIndex);
    }

    public byte getMouseIndex() {
        return this.mouseIndex;
    }

    public boolean isFocusObject() {
        return this.isFocusObject;
    }
}
