package ak.mcmod.chaindestruction.event;

import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
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
    if (!event.getWorld().isClientSide && event.getEntity() instanceof PlayerEntity) {
      PlayerEntity player = (PlayerEntity) event.getEntity();
      CDPlayerStatus.get(player).ifPresent(status -> {
        String mode = status.isPrivateRegisterMode() ? "ItemStack" : "Player";
        String s = String
                .format("ChainDestruction Info Registration:%s, Mode:%s, Range:%d", mode,
                        status.getModeType().name(),
                        status.getMaxDestroyedBlock());
        player.sendMessage(new StringTextComponent(s), Util.NIL_UUID);
      });
    }
  }
}
