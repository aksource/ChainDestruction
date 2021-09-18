package ak.mcmod.chaindestruction.network;

import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.chaindestruction.api.Constants;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * キー押下用メッセージハンドラクラス
 * Created by A.K. on 14/07/31.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MessageKeyPressedHandler implements BiConsumer<MessageKeyPressed, Supplier<Context>> {

  /**
   * キーイベント処理。MessageHandlerから呼ばれる
   *
   * @param item   手持ちアイテム
   * @param player プレイヤー
   * @param key    押下キーを表すbyte
   */
  public static void doKeyEvent(@Nullable ItemStack item, PlayerEntity player, byte key) {
    CDPlayerStatus.get(player).ifPresent(status -> {
      String chat;
      if (Objects.isNull(item)) {
        return;
      }
      if (key == Constants.RegKEY && !item.isEmpty()) {
        Set<String> enableItems = status.getEnableItems();
        String uniqueName = StringUtils.getUniqueString(item.getItem().getRegistryName());
        if (player.isShiftKeyDown() && enableItems.contains(uniqueName)) {
          enableItems.remove(uniqueName);
          chat = String.format("Remove Tool : %s", uniqueName);
          player.sendMessage(new StringTextComponent(chat), Util.NIL_UUID);
        }
        if (!player.isShiftKeyDown() && !enableItems.contains(uniqueName)) {
          enableItems.add(uniqueName);
          chat = String.format("Add Tool : %s", uniqueName);
          player.sendMessage(new StringTextComponent(chat), Util.NIL_UUID);
        }
      }
      if (key == Constants.DigKEY) {
        status.setDigUnder(!status.isDigUnder());
        chat = String.format("Dig Under %b", status.isDigUnder());
        player.sendMessage(new StringTextComponent(chat), Util.NIL_UUID);
      }
      if (key == Constants.ModeKEY) {
        if (player.isShiftKeyDown()) {
          status.setPrivateRegisterMode(!status.isPrivateRegisterMode());
          chat = String.format("Private Register Mode %b", status.isPrivateRegisterMode());
        } else {
          status.setModeType(status.getModeType().getNextModeType());
          chat = String.format("Mode %s", status.getModeType().name());
        }
        player.sendMessage(new StringTextComponent(chat), Util.NIL_UUID);
      }
      PacketHandler.INSTANCE.sendTo(new MessageCDStatusProperties(player),
              ((ServerPlayerEntity) player).connection.getConnection(),
              NetworkDirection.PLAY_TO_CLIENT);
    });
  }

  @Override
  public void accept(MessageKeyPressed messageKeyPressed, Supplier<Context> contextSupplier) {
    PlayerEntity player = contextSupplier.get().getSender();
    if (Objects.nonNull(player) && !player.getMainHandItem().isEmpty()) {
      doKeyEvent(player.getMainHandItem(), player, messageKeyPressed.getKey());
    }
  }
}
