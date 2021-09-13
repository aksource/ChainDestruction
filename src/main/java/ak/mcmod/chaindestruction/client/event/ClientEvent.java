package ak.mcmod.chaindestruction.client.event;

import ak.mcmod.chaindestruction.api.Constants;
import ak.mcmod.chaindestruction.client.ClientProxy;
import ak.mcmod.chaindestruction.network.MessageKeyPressed;
import ak.mcmod.chaindestruction.network.MessageMousePressed;
import ak.mcmod.chaindestruction.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

/**
 * クライアント側のマウス・キーボードイベントクラス
 * Created by A.K. on 14/08/01.
 */
public class ClientEvent {
    private static final Minecraft mc = Minecraft.getMinecraft();

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

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void KeyPressEvent(InputEvent.KeyInputEvent event) {
        if (FMLClientHandler.instance().getClient().inGameHasFocus && FMLClientHandler.instance().getClientPlayerEntity() != null) {
            byte keyIndex = getKeyIndex();
            if (keyIndex != -1) {
                EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
//                doKeyClient(null, player, keyIndex);
                PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(keyIndex));
            }
        }
    }

    private byte getMouseIndex() {
        byte mouse = -1;
//        if (mc.gameSettings.keyBindUseItem.getIsKeyPressed()) {
//            mouse = 1;
//        }
        if (mc.gameSettings.keyBindPickBlock.isKeyDown()) {
            mouse = Constants.MIDDLE_CLICK;
        }
//        if (mouse != -1 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
//            mouse += 3;
//        }
        return mouse;
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void mouseClickEvent(InputEvent.MouseInputEvent event) {
        if (mc.inGameHasFocus) {
            if (mouseCounter > 0) {
                mouseCounter--;
            }
            byte mouseIndex = getMouseIndex();
            if (mouseIndex != -1 && mouseIndex == Constants.MIDDLE_CLICK) {
                if (mouseCounter == 0) {
                    mouseCounter = 5;
                    boolean isFocusObject = mc.objectMouseOver != null || mc.pointedEntity != null;
                    PacketHandler.INSTANCE.sendToServer(new MessageMousePressed(mouseIndex, isFocusObject));
                }
            }
        }
    }

//    public void doKeyClient(ItemStack item, EntityPlayer player, byte key) {
//        if (key == Constants.DigKEY) {
//            ChainDestruction.digUnder = CDPlayerStatus.get(player).isDigUnder();
//        }
//    }
}
