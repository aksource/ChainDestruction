package ak.mcmod.chaindestruction.client;

import ak.mcmod.chaindestruction.CommonProxy;
import ak.mcmod.chaindestruction.client.event.ClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

import static ak.mcmod.chaindestruction.api.Constants.*;

public class ClientProxy extends CommonProxy {
    public static final KeyBinding REGISTER_ITEM_KEY = new KeyBinding(KEY_REGISTER_ITEM, Keyboard.KEY_K, KEY_CATEGORY);
    public static final KeyBinding DIG_UNDER_KEY = new KeyBinding(KEY_DIG_UNDER, Keyboard.KEY_U, KEY_CATEGORY);
    public static final KeyBinding TREE_KEY = new KeyBinding(KEY_TREE_MODE, Keyboard.KEY_SEMICOLON, KEY_CATEGORY);


    @Override
    public void registerClientInfo() {
        MinecraftForge.EVENT_BUS.register(new ClientEvent());
        ClientRegistry.registerKeyBinding(REGISTER_ITEM_KEY);
        ClientRegistry.registerKeyBinding(DIG_UNDER_KEY);
        ClientRegistry.registerKeyBinding(TREE_KEY);
    }

    @Override
    public EntityPlayer getEntityPlayer() {
        return Minecraft.getMinecraft().player;
    }
}