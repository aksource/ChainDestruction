package ak.mcmod.chaindestruction.client.event;

import ak.mcmod.chaindestruction.api.Constants;
import ak.mcmod.chaindestruction.client.ClientProxy;
import ak.mcmod.chaindestruction.network.MessageKeyPressed;
import ak.mcmod.chaindestruction.network.MessageMousePressed;
import ak.mcmod.chaindestruction.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

/**
 * クライアント側のマウス・キーボードイベントクラス
 * Created by A.K. on 14/08/01.
 */
public class ClientEvents {
    private static final Minecraft MC = Minecraft.getMinecraft();

    /** チャタリング防止用 */
    private int mouseCounter = 0;

    private byte getKeyIndex() {
        byte key = -1;
        if (ClientProxy.REGISTER_ITEM_KEY.isPressed()) {
            key = Constants.RegKEY;
        } else if (ClientProxy.DIG_UNDER_KEY.isPressed()) {
            key = Constants.DigKEY;
        } else if (ClientProxy.TREE_KEY.isPressed()) {
            key = Constants.ModeKEY;
        }
        return key;
    }

    @SubscribeEvent
    public void KeyPressEvent(InputEvent.KeyInputEvent event) {
        if (FMLClientHandler.instance().getClient().inGameHasFocus && FMLClientHandler.instance().getClientPlayerEntity() != null) {
            byte keyIndex = getKeyIndex();
            if (keyIndex != -1) {
                PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(keyIndex));
            }
        }
    }

    private byte getMouseIndex() {
        byte mouse = -1;
        if (MC.gameSettings.keyBindPickBlock.isKeyDown()) {
            mouse = Constants.MIDDLE_CLICK;
        }
        return mouse;
    }

    @SubscribeEvent
    public void mouseClickEvent(InputEvent.MouseInputEvent event) {
        if (MC.inGameHasFocus) {
            if (mouseCounter > 0) {
                mouseCounter--;
            }
            byte mouseIndex = getMouseIndex();
            if (mouseIndex == Constants.MIDDLE_CLICK) {
                if (mouseCounter == 0) {
                    mouseCounter = 5;
                    boolean isFocusObject = MC.objectMouseOver != null || MC.pointedEntity != null;
                    PacketHandler.INSTANCE.sendToServer(new MessageMousePressed(mouseIndex, isFocusObject));
                }
            }
        }
    }
}
