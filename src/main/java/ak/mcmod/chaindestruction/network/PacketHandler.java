package ak.mcmod.chaindestruction.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * メッセージ登録クラス Created by A.K. on 14/06/01.
 */
public class PacketHandler {

  public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
      .named(new ResourceLocation("chaindestruction", "channel"))
      .networkProtocolVersion(() -> "1").clientAcceptedVersions(e -> true)
      .serverAcceptedVersions(e -> true).simpleChannel();

  public static void init() {
    INSTANCE
        .registerMessage(0, MessageKeyPressed.class, MessageKeyPressed.encoder,
            MessageKeyPressed.decoder, new MessageKeyPressedHandler());
    INSTANCE.registerMessage(1, MessageMousePressed.class, MessageMousePressed.encoder,
        MessageMousePressed.decoder,
        new MessageMousePressedHandler());
    INSTANCE
        .registerMessage(2, MessageDigSound.class, MessageDigSound.encoder, MessageDigSound.decoder,
            new MessageDigSoundHandler());
    INSTANCE
        .registerMessage(3, MessageCDStatusProperties.class, MessageCDStatusProperties.encoder,
            MessageCDStatusProperties.decoder,
            new MessageCDStatusPropertiesHandler());
  }
}
