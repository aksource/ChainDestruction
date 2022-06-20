package ak.mcmod.chaindestruction.command;

import ak.mcmod.chaindestruction.capability.AdditionalPlayerStatus;
import ak.mcmod.chaindestruction.capability.CapabilityAdditionalPlayerStatus;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

import static ak.mcmod.chaindestruction.api.Constants.*;

/**
 * プレイヤーの現在の設定を表示するコマンド
 * Created by A.K. on 2016/09/27.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandPlayerCDStatus {

  public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher) {
    commandDispatcher.register(Commands.literal(COMMAND_PLAYER_CD_STATUS).requires(e -> e.hasPermission(2))
            .then(Commands.literal("show")
                    .executes(e -> show(e.getSource(), null))
                    .then(Commands.argument("target", EntityArgument.player())
                            .executes(e -> show(e.getSource(), EntityArgument.getPlayer(e, "target")))))
            .then(Commands.literal("reset")
                    .executes(e -> reset(e.getSource(), null))
                    .then(Commands.argument("target", EntityArgument.player())
                            .executes(e -> reset(e.getSource(), EntityArgument.getPlayer(e, "target")))))
            .then(Commands.literal("add_forbidden").then(Commands.argument("tag", ComponentArgument.textComponent())
                            .executes(e -> addForbiddenTag(e.getSource(), null, ComponentArgument.getComponent(e, "tag"))))
                    .then(Commands.argument("target", EntityArgument.player())
                            .executes(e -> addForbiddenTag(e.getSource(), EntityArgument.getPlayer(e, "target"), ComponentArgument.getComponent(e, "tag")))))
            .then(Commands.literal("remove_forbidden").then(Commands.argument("tag", ComponentArgument.textComponent())
                            .executes(e -> removeForbiddenTag(e.getSource(), null, ComponentArgument.getComponent(e, "tag"))))
                    .then(Commands.argument("target", EntityArgument.player())
                            .executes(e -> removeForbiddenTag(e.getSource(), EntityArgument.getPlayer(e, "target"), ComponentArgument.getComponent(e, "tag"))))));
  }

  private static int show(CommandSourceStack commandSource, @Nullable Player playerEntity) {
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
      var sb = new StringBuilder();
      playerEntity.getCapability(CapabilityAdditionalPlayerStatus.CAPABILITY).ifPresent(status -> sb.append(CapabilityAdditionalPlayerStatus.makePlayerStatusToString(status)));
      playerEntity.sendSystemMessage(Component.literal(sb.toString()));
    } else {
      return 1;
    }
    return 0;
  }

  private static int reset(CommandSourceStack commandSource, @Nullable Player playerEntity) {
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
      playerEntity.getCapability(CapabilityAdditionalPlayerStatus.CAPABILITY).ifPresent(status -> CapabilityAdditionalPlayerStatus
              .copyPlayerStatus(new AdditionalPlayerStatus(), status));
      commandSource.sendSuccess(Component.translatable(COMMAND_PLAYER_STATUS_RESET_SUCCESS), true);
    } else {
      return 1;
    }
    return 0;
  }

  private static int addForbiddenTag(CommandSourceStack commandSource, @Nullable Player playerEntity, Component forbiddenTag) {
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
      playerEntity.getCapability(CapabilityAdditionalPlayerStatus.CAPABILITY).ifPresent(status ->
              status.getForbiddenTags().add(forbiddenTag.getString()));
      commandSource.sendSuccess(Component.translatable(COMMAND_PLAYER_STATUS_ADD_FORBIDDEN_TAG_SUCCESS, forbiddenTag), true);
    } else {
      return 1;
    }
    return 0;
  }

  private static int removeForbiddenTag(CommandSourceStack commandSource, @Nullable Player playerEntity, Component forbiddenTag) {
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
      playerEntity.getCapability(CapabilityAdditionalPlayerStatus.CAPABILITY).ifPresent(status -> status.getForbiddenTags().remove(forbiddenTag.getString()));
      commandSource.sendSuccess(Component.translatable(COMMAND_PLAYER_STATUS_REMOVE_FORBIDDEN_TAG_SUCCESS, forbiddenTag), true);
    } else {
      return 1;
    }
    return 0;
  }
}
