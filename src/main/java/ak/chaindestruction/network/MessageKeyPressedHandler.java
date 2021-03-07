package ak.chaindestruction.network;

import ak.chaindestruction.ChainDestruction;
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
        if (Objects.nonNull(player) && !player.getHeldItemMainhand().isEmpty()) {
            ChainDestruction.interactblockhook.doKeyEvent(player.getHeldItemMainhand(), player, messageKeyPressed.getKey());
        }
    }
}
