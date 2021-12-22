package ak.mcmod.chaindestruction.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * メッセージ登録クラス Created by A.K. on 14/06/01.
 */
public class PacketHandler {

  public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
          .named(new ResourceLocation("chaindestruction", "channel"))
          .networkProtocolVersion(() -> "1").clientAcceptedVersions(e -> true)
          .serverAcceptedVersions(e -> true).simpleChannel();

  public static void init() {
    var index = 0;
    INSTANCE
            .registerMessage(index++, MessageKeyPressed.class, MessageKeyPressed.ENCODER,
                    MessageKeyPressed.DECODER, new MessageKeyPressedHandler());
    INSTANCE.registerMessage(index++, MessageMousePressed.class, MessageMousePressed.ENCODER,
            MessageMousePressed.DECODER,
            new MessageMousePressedHandler());
    INSTANCE
            .registerMessage(index++, MessageDigSound.class, MessageDigSound.ENCODER, MessageDigSound.DECODER,
                    new MessageDigSoundHandler());
    INSTANCE
            .registerMessage(index, MessageSyncAdditionalPayerStatus.class, MessageSyncAdditionalPayerStatus.ENCODER,
                    MessageSyncAdditionalPayerStatus.DECODER,
                    new MessageSyncAdditionalStatusHandler());
  }
}
