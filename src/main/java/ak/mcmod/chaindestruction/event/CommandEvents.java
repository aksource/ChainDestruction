package ak.mcmod.chaindestruction.event;

import ak.mcmod.chaindestruction.command.CommandCopyRtoLCDStatus;
import ak.mcmod.chaindestruction.command.CommandResetCDPlayerStatus;
import ak.mcmod.chaindestruction.command.CommandShowItemCDStatus;
import ak.mcmod.chaindestruction.command.CommandShowPlayerCDStatus;
import com.mojang.brigadier.CommandDispatcher;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandSource;
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
  public static void registerCommand(final RegisterCommandsEvent event) {
    CommandDispatcher<CommandSource> commandDispatcher = event.getDispatcher();
    CommandCopyRtoLCDStatus.register(commandDispatcher);
    CommandResetCDPlayerStatus.register(commandDispatcher);
    CommandShowItemCDStatus.register(commandDispatcher);
    CommandShowPlayerCDStatus.register(commandDispatcher);
  }
}
