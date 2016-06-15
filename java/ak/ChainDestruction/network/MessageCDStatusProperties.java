package ak.ChainDestruction.network;

import ak.ChainDestruction.CDStatus;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by A.K. on 14/06/02.
 */
public class MessageCDStatusProperties implements IMessage {

    public NBTTagCompound data;

    public MessageCDStatusProperties() {
    }

    public MessageCDStatusProperties(EntityPlayer entityPlayer) {
        this.data = new NBTTagCompound();
        CDStatus.get(entityPlayer).saveNBTData(data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }
}
