package ak.chaindestruction.network;

import ak.chaindestruction.ChainDestruction;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * キー押下用メッセージハンドラクラス
 * Created by A.K. on 14/07/31.
 */
public class MessageKeyPressedHandler implements BiConsumer<MessageKeyPressed, Supplier<Context>> {

    @Override
    public void accept(MessageKeyPressed messageKeyPressed, Supplier<Context> contextSupplier) {
        if (contextSupplier.get().getSender() != null) {
            EntityPlayer player = contextSupplier.get().getSender();
            ChainDestruction.interactblockhook.doKeyEvent(player.getHeldItemMainhand(), player, messageKeyPressed.getKey());
        }
    }
}
