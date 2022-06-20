package ak.mcmod.chaindestruction.command;

import ak.mcmod.chaindestruction.capability.CapabilityAdditionalItemStackStatus;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

import static ak.mcmod.chaindestruction.api.Constants.COMMAND_ITEM_CD_STATUS;
import static ak.mcmod.chaindestruction.api.Constants.COMMAND_ITEM_STATUS_COPY_SUCCESS;

/**
 * アイテムの現在の設定を表示するコマンド Created by A.K. on 2016/09/28.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandItemCDStatus {

  public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher) {
    commandDispatcher.register(
            Commands.literal(COMMAND_ITEM_CD_STATUS).requires(e -> e.hasPermission(2))
                    .then(Commands.literal("show")
                            .executes(e -> show(e.getSource(), null))
                            .then(Commands.argument("target", EntityArgument.player())
                                    .executes(e -> show(e.getSource(), EntityArgument.getPlayer(e, "target")))))
                    .then(Commands.literal("copy")
                            .executes(e -> copy(e.getSource(), null))
                            .then(Commands.argument("target", EntityArgument.player())
                                    .executes(e -> copy(e.getSource(), EntityArgument.getPlayer(e, "target"))))));
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
      playerEntity.getMainHandItem().getCapability(CapabilityAdditionalItemStackStatus.CAPABILITY).ifPresent(status ->
              sb.append(CapabilityAdditionalItemStackStatus.makeItemsStatusToString(status)));
      playerEntity.sendSystemMessage(Component.literal(sb.toString()));
    } else {
      return 1;
    }
    return 0;
  }

  private static int copy(CommandSourceStack commandSource, @Nullable Player playerEntity) {
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
      itemMainHand.getCapability(CapabilityAdditionalItemStackStatus.CAPABILITY)
              .ifPresent((copyFrom) ->
                      itemOffHand.getCapability(CapabilityAdditionalItemStackStatus.CAPABILITY).ifPresent(
                              (copyTo) -> CapabilityAdditionalItemStackStatus
                                      .copyItemState(copyFrom, copyTo)));
      commandSource.sendSuccess(Component.translatable(COMMAND_ITEM_STATUS_COPY_SUCCESS), true);
    } else {
      return 1;
    }
    return 0;
  }
}
