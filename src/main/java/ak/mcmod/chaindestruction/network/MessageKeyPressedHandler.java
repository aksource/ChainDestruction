package ak.mcmod.chaindestruction.network;

import ak.mcmod.chaindestruction.ChainDestruction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * キー押下用メッセージハンドラクラス
 * Created by A.K. on 14/07/31.
 */
public class MessageKeyPressedHandler implements BiConsumer<MessageKeyPressed, Supplier<Context>> {

    @Override
    public void accept(MessageKeyPressed messageKeyPressed, Supplier<Context> contextSupplier) {
        PlayerEntity player = contextSupplier.get().getSender();
        if (Objects.nonNull(player) && !player.getMainHandItem().isEmpty()) {
            ChainDestruction.interactBlockHook.doKeyEvent(player.getMainHandItem(), player, messageKeyPressed.getKey());
        }
    }
}
