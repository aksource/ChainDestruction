package ak.mcmod.chaindestruction.network;

import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.chaindestruction.api.Constants;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.ICDPlayerStatusHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * マウスクリック用メッセージハンドラクラス
 * Created by A.K. on 14/10/14.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MessageMousePressedHandler implements IMessageHandler<MessageMousePressed, IMessage> {
  /**
   * マウスイベント処理。MessageHandlerから呼ばれる
   *
   * @param item          手持ちアイテム
   * @param player        プレイヤー
   * @param mouse         押下したマウスのキーを表すbyte
   * @param isFocusObject オブジェクトにフォーカスしているかどうか
   */
  public static void doMouseEvent(ItemStack item, EntityPlayer player, byte mouse, boolean isFocusObject) {
    try {
      ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
      if (Objects.isNull(status)) {
        return;
      }
      if (!status.getEnableItems().contains(StringUtils.getUniqueString(item.getItem().getRegistryName()))) {
        return;
      }
      String chat;
      if (mouse == Constants.MIDDLE_CLICK && !isFocusObject) {
        int maxDestroyedBlock = status.getMaxDestroyedBlock();
        if (player.isSneaking() && maxDestroyedBlock > 0) {
          status.setMaxDestroyedBlock(--maxDestroyedBlock);
        } else {
          status.setMaxDestroyedBlock(++maxDestroyedBlock);
        }
        chat = String.format("New Max Destroyed : %d", maxDestroyedBlock);
        player.sendMessage(new TextComponentString(chat));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  @Nullable
  public IMessage onMessage(MessageMousePressed message, MessageContext ctx) {
    EntityPlayer player = ctx.getServerHandler().player;
    if (player != null && !player.getHeldItemMainhand().isEmpty()) {
      ItemStack equippedItem = player.getHeldItemMainhand();
      doMouseEvent(equippedItem, player, message.getMouseIndex(), message.isFocusObject());
    }
    return null;
  }
}
