package ak.chaindestruction.network;

import ak.chaindestruction.ChainDestruction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * マウスクリック用メッセージハンドラクラス Created by A.K. on 14/10/14.
 */
public class MessageMousePressedHandler implements
    BiConsumer<MessageMousePressed, Supplier<Context>> {

  @Override
  public void accept(MessageMousePressed messageMousePressed, Supplier<Context> contextSupplier) {
    PlayerEntity player = contextSupplier.get().getSender();
    if (Objects.nonNull(player) && !player.getHeldItemMainhand().isEmpty()) {
      ItemStack equippedItem = player.getHeldItemMainhand();
      ChainDestruction.interactblockhook
          .doMouseEvent(equippedItem, player, messageMousePressed.getMouseIndex(),
              messageMousePressed.isFocusObject());
    }
  }
}
