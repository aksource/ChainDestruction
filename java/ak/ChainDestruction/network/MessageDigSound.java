package ak.ChainDestruction.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by AKIRA on 15/01/13.
 */
public class MessageDigSound implements IMessage {

    private BlockPos blockPos;

    public MessageDigSound(){}

    public MessageDigSound(BlockPos pos) {
        this.blockPos = pos;
    }
    @Override
    public void fromBytes(ByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        this.blockPos = new BlockPos(x, y, z);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.blockPos.getX());
        buf.writeInt(this.blockPos.getY());
        buf.writeInt(this.blockPos.getZ());
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }
}
