package ak.chaindestruction.command;

import static ak.akapi.Constants.COMMAND_SHOW_PLAYER_CD_STATUS;

import ak.chaindestruction.capability.CDPlayerStatus;
import ak.chaindestruction.capability.CapabilityCDPlayerStatusHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

/**
 * プレイヤーの現在の設定を表示するコマンド
 * Created by A.K. on 2016/09/27.
 */
public class CommandShowPlayerCDStatus {

  public static void register(CommandDispatcher<CommandSource> commandDispatcher) {
    commandDispatcher.register(
        Commands.literal(COMMAND_SHOW_PLAYER_CD_STATUS).requires(e -> e.hasPermissionLevel(2))
            .executes(e -> execute(e.getSource(), null))
            .then(Commands.argument("target", EntityArgument.player())
                .executes(e -> execute(e.getSource(), EntityArgument.getPlayer(e, "target")))
            ));
  }

  private static int execute(CommandSource commandSource, @Nullable PlayerEntity PlayerEntity) {
    if (Objects.isNull(PlayerEntity)) {
      try {
        PlayerEntity = commandSource.asPlayer();
      } catch (CommandSyntaxException e) {
        e.printStackTrace();
        return 1;
      }
    }
    //noinspection ConstantConditions
    if (Objects.nonNull(PlayerEntity)) {
      StringBuilder sb = new StringBuilder();
      CDPlayerStatus.get(PlayerEntity).ifPresent(status -> {
        sb.append(CapabilityCDPlayerStatusHandler.makePlayerStatusToString(status));
      });
      PlayerEntity.sendMessage(new StringTextComponent(sb.toString()));
    } else {
      return 1;
    }
    return 0;
  }
}
