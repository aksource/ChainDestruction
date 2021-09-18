package ak.mcmod.chaindestruction.command;

import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler;
import ak.mcmod.chaindestruction.capability.ICDPlayerStatusHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Objects;

import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;
import static ak.mcmod.chaindestruction.api.Constants.COMMAND_RESET_PLAYER_STATUS;
import static ak.mcmod.chaindestruction.api.Constants.COMMAND_USAGE_RESET_PLAYER_STATUS;

/**
 * プレイヤーの連鎖破壊設定を初期化するコマンド
 * Created by A.K. on 2016/09/25.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandResetCDPlayerStatus extends CommandBase {
    @Override
    public String getName() {
        return COMMAND_RESET_PLAYER_STATUS;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return COMMAND_USAGE_RESET_PLAYER_STATUS;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            EntityPlayer entityPlayer = getCommandSenderAsPlayer(sender);
            if (entityPlayer.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null)) {
                ICDPlayerStatusHandler status = entityPlayer.getCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
                CapabilityCDPlayerStatusHandler.copyPlayerStatus(new CDPlayerStatus(), Objects.requireNonNull(status));
            }
        } else {
            for (String username : args) {
                Entity entity = getEntity(server, sender, username);
                if (entity instanceof EntityPlayer && entity.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null)) {
                    ICDPlayerStatusHandler status = entity.getCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
                    CapabilityCDPlayerStatusHandler.copyPlayerStatus(new CDPlayerStatus(), Objects.requireNonNull(status));
                }
            }
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
