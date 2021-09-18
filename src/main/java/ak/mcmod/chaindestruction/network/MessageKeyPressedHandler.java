package ak.mcmod.chaindestruction.network;

import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.chaindestruction.api.Constants;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.ICDPlayerStatusHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Set;

/**
 * キー押下用メッセージハンドラクラス
 * Created by A.K. on 14/07/31.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MessageKeyPressedHandler implements IMessageHandler<MessageKeyPressed, IMessage> {
  /**
   * キーイベント処理。MessageHandlerから呼ばれる
   *
   * @param item   手持ちアイテム
   * @param player プレイヤー
   * @param key    押下キーを表すbyte
   */
  public static void doKeyEvent(ItemStack item, EntityPlayer player, byte key) {
    String chat;
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    if (Objects.isNull(status)) {
      return;
    }
    if (key == Constants.RegKEY && !item.isEmpty()) {
      Set<String> enableItems = status.getEnableItems();
      String uniqueName = StringUtils.getUniqueString(item.getItem().getRegistryName());
      if (player.isSneaking() && enableItems.contains(uniqueName)) {
        enableItems.remove(uniqueName);
        chat = String.format("Remove Tool : %s", uniqueName);
        player.sendMessage(new TextComponentString(chat));
      }
      if (!player.isSneaking() && !enableItems.contains(uniqueName)) {
        enableItems.add(uniqueName);
        chat = String.format("Add Tool : %s", uniqueName);
        player.sendMessage(new TextComponentString(chat));
      }
    }
    if (key == Constants.DigKEY) {
      status.setDigUnder(!status.isDigUnder());
      chat = String.format("Dig Under %b", status.isDigUnder());
      player.sendMessage(new TextComponentString(chat));
    }
    if (key == Constants.ModeKEY) {
      if (player.isSneaking()) {
        status.setPrivateRegisterMode(!status.isPrivateRegisterMode());
        chat = String.format("Private Register Mode %b", status.isPrivateRegisterMode());
      } else {
        status.setModeType(status.getModeType().getNextModeType());
        chat = String.format("Mode %s", status.getModeType().name());
      }
      player.sendMessage(new TextComponentString(chat));
    }
    PacketHandler.INSTANCE.sendTo(new MessageCDStatusProperties(player), (EntityPlayerMP) player);
  }

  @Override
  @Nullable
  public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
    if (ctx.getServerHandler().player != null) {
      EntityPlayer player = ctx.getServerHandler().player;
      doKeyEvent(player.getHeldItemMainhand(), player, message.key);
    }
    return null;
  }
}
