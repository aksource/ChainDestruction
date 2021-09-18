package ak.mcmod.chaindestruction.client;

import ak.mcmod.chaindestruction.client.event.ClientEvents;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import static ak.mcmod.chaindestruction.api.Constants.*;

public class ClientUtils {
  public static final KeyBinding REGISTER_ITEM_KEY = new KeyBinding(KEY_REGISTER_ITEM, GLFW.GLFW_KEY_K, KEY_CATEGORY);
  public static final KeyBinding DIG_UNDER_KEY = new KeyBinding(KEY_DIG_UNDER, GLFW.GLFW_KEY_U, KEY_CATEGORY);
  public static final KeyBinding TREE_KEY = new KeyBinding(KEY_CHANGE_MODE, GLFW.GLFW_KEY_SEMICOLON, KEY_CATEGORY);

  public static void registerClientInfo() {
    MinecraftForge.EVENT_BUS.register(new ClientEvents());
    ClientRegistry.registerKeyBinding(REGISTER_ITEM_KEY);
    ClientRegistry.registerKeyBinding(DIG_UNDER_KEY);
    ClientRegistry.registerKeyBinding(TREE_KEY);
  }
}