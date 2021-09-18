package ak.mcmod.chaindestruction.command;

import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

import static ak.mcmod.chaindestruction.api.Constants.COMMAND_SHOW_PLAYER_CD_STATUS;

/**
 * プレイヤーの現在の設定を表示するコマンド
 * Created by A.K. on 2016/09/27.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandShowPlayerCDStatus {

  public static void register(final CommandDispatcher<CommandSource> commandDispatcher) {
    commandDispatcher.register(
            Commands.literal(COMMAND_SHOW_PLAYER_CD_STATUS).requires(e -> e.hasPermission(2))
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
      StringBuilder sb = new StringBuilder();
      CDPlayerStatus.get(playerEntity).ifPresent(status -> sb.append(CapabilityCDPlayerStatusHandler.makePlayerStatusToString(status)));
      playerEntity.sendMessage(new StringTextComponent(sb.toString()), Util.NIL_UUID);
    } else {
      return 1;
    }
    return 0;
  }
}
