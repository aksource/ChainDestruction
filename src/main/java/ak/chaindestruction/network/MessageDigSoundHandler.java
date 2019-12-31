package ak.chaindestruction.network;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * ブロック採掘用メッセージハンドラクラス
 * Created by A.K. on 15/01/13.
 */
public class MessageDigSoundHandler implements BiConsumer<MessageDigSound, Supplier<Context>> {
    public void accept(MessageDigSound messageDigSound, Supplier<Context> contextSupplier) {
        PlayerEntity player = Minecraft.getInstance().player;
        World world = player.getEntityWorld();
        BlockPos blockPos = messageDigSound.getBlockPos();
        world.playBroadcastSound(2001, blockPos, Block.getStateId(world.getBlockState(blockPos)));
    }
}
