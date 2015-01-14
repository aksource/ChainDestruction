package ak.ChainDestruction.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by A.K. on 14/06/01.
 */
public class PacketHandler {

    public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("chaindestruction");

    public static void init() {
        INSTANCE.registerMessage(MessageKeyPressedHandler.class, MessageKeyPressed.class, 0, Side.SERVER);
        INSTANCE.registerMessage(MessageMousePressedHandler.class, MessageMousePressed.class, 1, Side.SERVER);
        INSTANCE.registerMessage(MessageDigSoundHandler.class, MessageDigSound.class, 2, Side.CLIENT);
    }
}
