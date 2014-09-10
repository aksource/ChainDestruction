package ak.ChainDestruction;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy
{
	public static final KeyBinding registItemKey = new KeyBinding("Key.CDRegistItem",Keyboard.KEY_K, "ChainDestruction");
	public static final KeyBinding digUnderKey = new KeyBinding("Key.CDDIgUnder",Keyboard.KEY_U, "ChainDestruction");
    public static final KeyBinding treeKey = new KeyBinding("Key.CDTree", Keyboard.KEY_SEMICOLON, "ChainDestruction");
	@Override
	public void registerClientInfo(){
        FMLCommonHandler.instance().bus().register(new ClientEvent());
		ClientRegistry.registerKeyBinding(registItemKey);
		ClientRegistry.registerKeyBinding(digUnderKey);
        ClientRegistry.registerKeyBinding(treeKey);
	}
}