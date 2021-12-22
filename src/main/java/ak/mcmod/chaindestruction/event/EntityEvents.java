package ak.mcmod.chaindestruction.event;

import ak.mcmod.chaindestruction.capability.CapabilityAdditionalPlayerStatus;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by A.K. on 2021/09/18.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
//@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.DEDICATED_SERVER})
public class EntityEvents {
  @SubscribeEvent
  public static void joinInWorld(final EntityJoinWorldEvent event) {
    if (!event.getWorld().isClientSide && event.getEntity() instanceof Player player) {
      player.getCapability(CapabilityAdditionalPlayerStatus.CAPABILITY).ifPresent(status -> {
        var mode = status.isPrivateRegisterMode() ? "ItemStack" : "Player";
        var s = String
                .format("ChainDestruction Info Registration:%s, Mode:%s, Range:%d", mode,
                        status.getModeType().name(),
                        status.getMaxDestroyedBlock());
        player.sendMessage(new TextComponent(s), Util.NIL_UUID);
      });
    }
  }
}
