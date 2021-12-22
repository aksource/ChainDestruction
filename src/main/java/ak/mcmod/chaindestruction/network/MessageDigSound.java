package ak.mcmod.chaindestruction.network;


import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * ブロック採掘音用メッセージクラス Created by A.K. on 15/01/13.
 */
public class MessageDigSound {

  public static final BiConsumer<MessageDigSound, FriendlyByteBuf> ENCODER = (messageDigSound, packetBuffer) -> packetBuffer.writeInt(messageDigSound.blockPos.getX()).writeInt(messageDigSound.blockPos.getY())
          .writeInt(messageDigSound.blockPos.getZ());
  public static final Function<FriendlyByteBuf, MessageDigSound> DECODER = packetBuffer -> new MessageDigSound(
          new BlockPos(packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt()));
  private BlockPos blockPos;

  @SuppressWarnings("unused")
  public MessageDigSound() {
  }

  public MessageDigSound(BlockPos pos) {
    this.blockPos = pos;
  }

  BlockPos getBlockPos() {
    return blockPos;
  }
}
