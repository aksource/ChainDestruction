package ak.ChainDestruction;

import ak.ChainDestruction.network.MessageKeyPressed;
import ak.ChainDestruction.network.PacketHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import static ak.ChainDestruction.InteractBlockHook.*;

/**
 * Created by A.K. on 14/08/01.
 */
public class ClientEvent {

    private byte getKeyIndex() {
        byte key = -1;
        if (ClientProxy.registItemKey.isPressed()) {
            key = RegKEY;
        } else if (ClientProxy.digUnderKey.isPressed()) {
            key = DigKEY;
        } else if (ClientProxy.treeKey.isPressed()) {
            key = TreeKEY;
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

    public void doKeyClient(ItemStack item, EntityPlayer player, byte key) {
        if (key == DigKEY) {
            ChainDestruction.digUnder = ChainDestruction.interactblockhook.digUnder;
        }
    }
}
