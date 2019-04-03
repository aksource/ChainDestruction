package ak.chaindestruction.command;

import static ak.akapi.Constants.COMMAND_SHOW_ITEM_CD_STATUS;

import ak.chaindestruction.capability.CDItemStackStatus;
import ak.chaindestruction.capability.CapabilityCDItemStackStatusHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

/**
 * アイテムの現在の設定を表示するコマンド Created by A.K. on 2016/09/28.
 */
public class CommandShowItemCDStatus {

  public static void register(CommandDispatcher<CommandSource> commandDispatcher) {
    commandDispatcher.register(
        Commands.literal(COMMAND_SHOW_ITEM_CD_STATUS).requires(e -> e.hasPermissionLevel(2))
            .executes(e -> execute(e.getSource(), null))
            .then(Commands.argument("target", EntityArgument.player())
                .executes(e -> execute(e.getSource(), EntityArgument.getPlayer(e, "target")))
            ));
  }

  private static int execute(CommandSource commandSource, @Nullable EntityPlayer entityPlayer) {
    if (Objects.isNull(entityPlayer)) {
      try {
        entityPlayer = commandSource.asPlayer();
      } catch (CommandSyntaxException e) {
        e.printStackTrace();
        return 1;
      }
    }
    //noinspection ConstantConditions
    if (Objects.nonNull(entityPlayer)) {
      StringBuilder sb = new StringBuilder();
      CDItemStackStatus.get(entityPlayer.getHeldItemMainhand()).ifPresent(status -> {
        sb.append(CapabilityCDItemStackStatusHandler.makeItemsStatusToString(status));
      });
      entityPlayer.sendMessage(new TextComponentString(sb.toString()));
    } else {
      return 1;
    }
    return 0;
  }
}
