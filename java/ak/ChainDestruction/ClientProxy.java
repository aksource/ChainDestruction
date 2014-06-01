package ak.ChainDestruction;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy
{
	public static final KeyBinding registItemKey = new KeyBinding("Key.CDRegistItem",Keyboard.KEY_K, "ChainDestruction");
	public static final KeyBinding digUnderKey = new KeyBinding("Key.CDDIgUnder",Keyboard.KEY_U, "ChainDestruction");
    public static final KeyBinding treeKey = new KeyBinding("Key.CDTree", Keyboard.KEY_COLON, "ChainDestruction");
	@Override
	public void registerClientInfo(){
		ClientRegistry.registerKeyBinding(registItemKey);
		ClientRegistry.registerKeyBinding(digUnderKey);
        ClientRegistry.registerKeyBinding(treeKey);
	}
}