package ak.ChainDestruction.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ChunkCoordinates;


/**
 * Created by AKIRA on 15/01/13.
 */
public class MessageDigSound implements IMessage {

    private ChunkCoordinates chunkCoordinates;

    public MessageDigSound(){}

    public MessageDigSound(ChunkCoordinates pos) {
        this.chunkCoordinates = pos;
    }
    @Override
    public void fromBytes(ByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        this.chunkCoordinates = new ChunkCoordinates(x, y, z);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.chunkCoordinates.posX);
        buf.writeInt(this.chunkCoordinates.posY);
        buf.writeInt(this.chunkCoordinates.posZ);
    }

    public ChunkCoordinates getChunkCoordinates() {
        return chunkCoordinates;
    }
}
