package ak.mcmod.chaindestruction.event;

import ak.mcmod.chaindestruction.command.CommandItemCDStatus;
import ak.mcmod.chaindestruction.command.CommandPlayerCDStatus;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by A.K. on 2021/09/18.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
//@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.DEDICATED_SERVER})
public class CommandEvents {
  @SubscribeEvent
  public static void registerCommands(final RegisterCommandsEvent event) {
    var commandDispatcher = event.getDispatcher();
    CommandItemCDStatus.register(commandDispatcher);
    CommandPlayerCDStatus.register(commandDispatcher);
  }
}
