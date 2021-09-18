package ak.mcmod.chaindestruction.client.event;

import ak.mcmod.chaindestruction.api.Constants;
import ak.mcmod.chaindestruction.client.ClientUtils;
import ak.mcmod.chaindestruction.network.MessageKeyPressed;
import ak.mcmod.chaindestruction.network.MessageMousePressed;
import ak.mcmod.chaindestruction.network.PacketHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * クライアント側のマウス・キーボードイベントクラス
 * Created by A.K. on 14/08/01.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ClientEvents {
  private static final Minecraft MC = Minecraft.getInstance();

  /**
   * チャタリング防止用
   */
  private int mouseCounter = 0;

  private byte getKeyIndex() {
    byte key = -1;
    if (ClientUtils.REGISTER_ITEM_KEY.consumeClick()) {
      key = Constants.RegKEY;
    } else if (ClientUtils.DIG_UNDER_KEY.consumeClick()) {
      key = Constants.DigKEY;
    } else if (ClientUtils.TREE_KEY.consumeClick()) {
      key = Constants.ModeKEY;
    }
    return key;
  }

  private void keyPressEvent() {
    if (MC.isWindowActive() && Objects.nonNull(MC.player)) {
      byte keyIndex = getKeyIndex();
      if (keyIndex != -1) {
        PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(keyIndex));
      }
    }
  }

  private byte getMouseIndex() {
    byte mouse = -1;
    if (MC.options.keyPickItem.isDown()) {
      mouse = Constants.MIDDLE_CLICK;
    }
    return mouse;
  }

  private void mouseClickEvent() {
    if (MC.isWindowActive()) {
      if (mouseCounter > 0) {
        mouseCounter--;
      }
      byte mouseIndex = getMouseIndex();
      if (mouseIndex == Constants.MIDDLE_CLICK) {
        if (mouseCounter == 0) {
          mouseCounter = 5;
          boolean isFocusObject = (Objects.nonNull(MC.hitResult) && MC.hitResult.getType() != Type.MISS) || Objects.nonNull(MC.crosshairPickEntity);
          PacketHandler.INSTANCE.sendToServer(new MessageMousePressed(mouseIndex, isFocusObject));
        }
      }
    }
  }

  @SubscribeEvent
  public void clientTickEvent(final ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      mouseClickEvent();
      keyPressEvent();
    }
  }
}
