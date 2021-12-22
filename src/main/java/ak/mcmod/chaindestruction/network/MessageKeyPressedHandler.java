package ak.mcmod.chaindestruction.network;

import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.chaindestruction.api.Constants;
import ak.mcmod.chaindestruction.capability.CapabilityAdditionalPlayerStatus;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * キー押下用メッセージハンドラクラス
 * Created by A.K. on 14/07/31.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MessageKeyPressedHandler implements BiConsumer<MessageKeyPressed, Supplier<NetworkEvent.Context>> {

  /**
   * キーイベント処理。MessageHandlerから呼ばれる
   *
   * @param item   手持ちアイテム
   * @param player プレイヤー
   * @param key    押下キーを表すbyte
   */
  public static void doKeyEvent(@Nullable ItemStack item, Player player, byte key) {
    player.getCapability(CapabilityAdditionalPlayerStatus.CAPABILITY).ifPresent(status -> {
      var chat = "";
      if (Objects.isNull(item)) {
        return;
      }
      if (key == Constants.REG_KEY && !item.isEmpty()) {
        var enableItems = status.getEnableItems();
        var uniqueName = StringUtils.getUniqueString(item.getItem().getRegistryName());
        if (player.isShiftKeyDown() && enableItems.contains(uniqueName)) {
          enableItems.remove(uniqueName);
          chat = String.format("Remove Tool : %s", uniqueName);
          player.sendMessage(new TextComponent(chat), Util.NIL_UUID);
        }
        if (!player.isShiftKeyDown() && !enableItems.contains(uniqueName)) {
          enableItems.add(uniqueName);
          chat = String.format("Add Tool : %s", uniqueName);
          player.sendMessage(new TextComponent(chat), Util.NIL_UUID);
        }
      }
      if (key == Constants.DIG_KEY) {
        status.setDigUnder(!status.isDigUnder());
        chat = String.format("Dig Under %b", status.isDigUnder());
        player.sendMessage(new TextComponent(chat), Util.NIL_UUID);
      }
      if (key == Constants.MODE_KEY) {
        if (player.isShiftKeyDown()) {
          status.setPrivateRegisterMode(!status.isPrivateRegisterMode());
          chat = String.format("Private Register Mode %b", status.isPrivateRegisterMode());
        } else {
          status.setModeType(status.getModeType().getNextModeType());
          chat = String.format("Mode %s", status.getModeType().name());
        }
        player.sendMessage(new TextComponent(chat), Util.NIL_UUID);
      }
      PacketHandler.INSTANCE.sendTo(new MessageSyncAdditionalPayerStatus(player),
              ((ServerPlayer) player).connection.getConnection(),
              NetworkDirection.PLAY_TO_CLIENT);
    });
  }

  @Override
  public void accept(MessageKeyPressed messageKeyPressed, Supplier<NetworkEvent.Context> contextSupplier) {
    Player player = contextSupplier.get().getSender();
    if (Objects.nonNull(player) && !player.getMainHandItem().isEmpty()) {
      doKeyEvent(player.getMainHandItem(), player, messageKeyPressed.getKey());
    }
  }
}
