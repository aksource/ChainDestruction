package ak.mcmod.chaindestruction.network;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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
public class MessageDigSoundHandler implements BiConsumer<MessageDigSound, Supplier<Context>> {
  public void accept(MessageDigSound messageDigSound, Supplier<Context> contextSupplier) {
    PlayerEntity player = Minecraft.getInstance().player;
    if (Objects.nonNull(player)) {
      World world = player.getCommandSenderWorld();
      BlockPos blockPos = messageDigSound.getBlockPos();
      world.globalLevelEvent(2001, blockPos, Block.getId(world.getBlockState(blockPos)));
    }
  }
}
