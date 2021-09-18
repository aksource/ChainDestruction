package ak.mcmod.chaindestruction.event;

import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.ICDPlayerStatusHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * Created by A.K. on 2021/09/18.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
//@Mod.EventBusSubscriber(modid = MOD_ID)
public class EntityEvents {
  @SubscribeEvent
  public static void joinInWorld(EntityJoinWorldEvent event) {
    if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer) event.getEntity();
      ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
      if (Objects.isNull(status)) {
        return;
      }
      String mode = status.isPrivateRegisterMode() ? "ItemStack" : "Player";
      String s = String.format("ChainDestruction Info Registration:%s, Mode:%s, Range:%d", mode, status.getModeType().name(), status.getMaxDestroyedBlock());
      player.sendMessage(new TextComponentString(s));
    }
  }
}
