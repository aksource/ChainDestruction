package ak.ChainDestruction;

import ak.ChainDestruction.network.MessageKeyPressed;
import ak.ChainDestruction.network.MessageMousePressed;
import ak.ChainDestruction.network.PacketHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

import static ak.ChainDestruction.InteractBlockHook.*;

/**
 * Created by A.K. on 14/08/01.
 */
public class ClientEvent {
    private static Minecraft mc = Minecraft.getMinecraft();

    private byte getKeyIndex() {
        byte key = -1;
        if (ClientProxy.registItemKey.isPressed()) {
            key = RegKEY;
        } else if (ClientProxy.digUnderKey.isPressed()) {
            key = DigKEY;
        } else if (ClientProxy.treeKey.isPressed()) {
            key = ModeKEY;
        }
        return key;
    }
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
        if (mc.gameSettings.keyBindUseItem.isPressed()) {
            mouse = 1;
        }
        if (mc.gameSettings.keyBindPickBlock.isPressed()) {
            mouse = 2;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            mouse += 3;
        }
        return mouse;
    }

    @SubscribeEvent
    public void mouseClickEvent(InputEvent.MouseInputEvent event) {
        if (mc.inGameHasFocus) {
            byte mouseIndex = getMouseIndex();
            if (mouseIndex != -1) {
                boolean isFocusObject = mc.objectMouseOver != null || mc.pointedEntity != null;
                PacketHandler.INSTANCE.sendToServer(new MessageMousePressed(mouseIndex, isFocusObject));
            }
        }
    }

    public void doKeyClient(ItemStack item, EntityPlayer player, byte key) {
        if (key == DigKEY) {
            ChainDestruction.digUnder = ChainDestruction.interactblockhook.digUnder;
        }
    }
}
