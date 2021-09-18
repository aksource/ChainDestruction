package ak.mcmod.chaindestruction.network;

import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.ICDPlayerStatusHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;

/**
 * 連鎖破壊用メッセージクラス
 * Created by A.K. on 14/06/02.
 */
public class MessageCDStatusProperties implements IMessage {

  public NBTTagCompound data;

  @SuppressWarnings("unused")
  public MessageCDStatusProperties() {
  }

  public MessageCDStatusProperties(EntityPlayer entityPlayer) {
    ICDPlayerStatusHandler instance = CDPlayerStatus.get(entityPlayer);
    this.data = (NBTTagCompound) CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage().writeNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, instance, null);
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
