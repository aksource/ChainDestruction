package ak.ChainDestruction;

import ak.ChainDestruction.network.MessageKeyPressed;
import ak.ChainDestruction.network.MessageMousePressed;
import ak.ChainDestruction.network.PacketHandler;
import ak.akapi.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

/**
 * クライアント側のマウス・キーb−どイベントクラス
 * Created by A.K. on 14/08/01.
 */
public class ClientEvent {
    private static Minecraft mc = Minecraft.getMinecraft();

    private byte getKeyIndex() {
        byte key = -1;
        if (ClientProxy.registItemKey.isPressed()) {
            key = Constants.RegKEY;
        } else if (ClientProxy.digUnderKey.isPressed()) {
            key = Constants.DigKEY;
        } else if (ClientProxy.treeKey.isPressed()) {
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
                doKeyClient(null, player, keyIndex);
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
            byte mouseIndex = getMouseIndex();
            if (mouseIndex != -1 && mouseIndex == Constants.MIDDLE_CLICK) {
                boolean isFocusObject = mc.objectMouseOver != null || mc.pointedEntity != null;
                PacketHandler.INSTANCE.sendToServer(new MessageMousePressed(mouseIndex, isFocusObject));
            }
        }
    }

    public void doKeyClient(ItemStack item, EntityPlayer player, byte key) {
        if (key == Constants.DigKEY) {
            ChainDestruction.digUnder = CDStatus.get(player).isDigUnder();
        }
    }
}
