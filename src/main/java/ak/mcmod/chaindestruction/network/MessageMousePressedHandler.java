package ak.mcmod.chaindestruction.network;

import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.chaindestruction.api.Constants;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * マウスクリック用メッセージハンドラクラス Created by A.K. on 14/10/14.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MessageMousePressedHandler implements
        BiConsumer<MessageMousePressed, Supplier<Context>> {

  /**
   * マウスイベント処理。MessageHandlerから呼ばれる
   *
   * @param item          手持ちアイテム
   * @param player        プレイヤー
   * @param mouse         押下したマウスのキーを表すbyte
   * @param isFocusObject オブジェクトにフォーカスしているかどうか
   */
  public static void doMouseEvent(ItemStack item, PlayerEntity player, byte mouse,
                                  boolean isFocusObject) {
    try {
      CDPlayerStatus.get(player).ifPresent(status -> {
        if (!status.getEnableItems()
                .contains(StringUtils.getUniqueString(item.getItem().getRegistryName()))) {
          return;
        }
        String chat;
        if (mouse == Constants.MIDDLE_CLICK && !isFocusObject) {
          int maxDestroyedBlock = status.getMaxDestroyedBlock();
          if (player.isShiftKeyDown() && maxDestroyedBlock > 0) {
            status.setMaxDestroyedBlock(--maxDestroyedBlock);
          } else {
            status.setMaxDestroyedBlock(++maxDestroyedBlock);
          }
          chat = String.format("New Max Destroyed : %d", maxDestroyedBlock);
          player.sendMessage(new StringTextComponent(chat), Util.NIL_UUID);
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void accept(MessageMousePressed messageMousePressed, Supplier<Context> contextSupplier) {
    PlayerEntity player = contextSupplier.get().getSender();
    if (Objects.nonNull(player) && !player.getMainHandItem().isEmpty()) {
      ItemStack equippedItem = player.getMainHandItem();
      doMouseEvent(equippedItem, player, messageMousePressed.getMouseIndex(),
              messageMousePressed.isFocusObject());
    }
  }
}
