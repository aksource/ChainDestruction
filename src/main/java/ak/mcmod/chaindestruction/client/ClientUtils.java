package ak.mcmod.chaindestruction.client;

import ak.mcmod.chaindestruction.client.event.ClientEvents;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import static ak.mcmod.chaindestruction.api.Constants.*;

public class ClientUtils {
  public static final KeyMapping REGISTER_ITEM_KEY = new KeyMapping(KEY_REGISTER_ITEM, GLFW.GLFW_KEY_K, KEY_CATEGORY);
  public static final KeyMapping DIG_UNDER_KEY = new KeyMapping(KEY_DIG_UNDER, GLFW.GLFW_KEY_U, KEY_CATEGORY);
  public static final KeyMapping TREE_KEY = new KeyMapping(KEY_CHANGE_MODE, GLFW.GLFW_KEY_SEMICOLON, KEY_CATEGORY);

  public static void registerClientInfo() {
    MinecraftForge.EVENT_BUS.register(new ClientEvents());
    ClientRegistry.registerKeyBinding(REGISTER_ITEM_KEY);
    ClientRegistry.registerKeyBinding(DIG_UNDER_KEY);
    ClientRegistry.registerKeyBinding(TREE_KEY);
  }
}