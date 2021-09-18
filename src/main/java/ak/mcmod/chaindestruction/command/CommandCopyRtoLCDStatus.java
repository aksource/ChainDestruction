package ak.mcmod.chaindestruction.command;

import ak.mcmod.chaindestruction.capability.CDItemStackStatus;
import ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

import static ak.mcmod.chaindestruction.api.Constants.COMMAND_COPY_R_TO_L;

/**
 * 右手のアイテムの設定を左手のアイテムにコピーするコマンド Created by A.K. on 2016/09/26.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandCopyRtoLCDStatus {

  public static void register(final CommandDispatcher<CommandSource> commandDispatcher) {
    commandDispatcher.register(
            Commands.literal(COMMAND_COPY_R_TO_L).requires(e -> e.hasPermission(2))
                    .executes(e -> execute(e.getSource(), null))
                    .then(Commands.argument("target", EntityArgument.player())
                            .executes(e -> execute(e.getSource(), EntityArgument.getPlayer(e, "target")))
                    ));
  }

  private static int execute(CommandSource commandSource, @Nullable PlayerEntity playerEntity) {
    if (Objects.isNull(playerEntity)) {
      try {
        playerEntity = commandSource.getPlayerOrException();
      } catch (CommandSyntaxException e) {
        e.printStackTrace();
        return 1;
      }
    }
    //noinspection ConstantConditions
    if (Objects.nonNull(playerEntity)) {
      ItemStack itemMainHand = playerEntity.getMainHandItem();
      ItemStack itemOffHand = playerEntity.getOffhandItem();
      CDItemStackStatus.get(itemMainHand)
              .ifPresent((copyFrom) ->
                      CDItemStackStatus.get(itemOffHand).ifPresent(
                              (copyTo) -> CapabilityCDItemStackStatusHandler
                                      .copyItemState(copyFrom, copyTo)));
    } else {
      return 1;
    }
    return 0;
  }
}
