package ak.mcmod.chaindestruction.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * ブロック採掘用メッセージハンドラクラス
 * Created by A.K. on 15/01/13.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MessageDigSoundHandler implements BiConsumer<MessageDigSound, Supplier<NetworkEvent.Context>> {
  public void accept(MessageDigSound messageDigSound, Supplier<NetworkEvent.Context> contextSupplier) {
    var player = Minecraft.getInstance().player;
    if (Objects.nonNull(player)) {
      var world = player.getCommandSenderWorld();
      var blockPos = messageDigSound.getBlockPos();
      world.globalLevelEvent(2001, blockPos, Block.getId(world.getBlockState(blockPos)));
    }
  }
}
