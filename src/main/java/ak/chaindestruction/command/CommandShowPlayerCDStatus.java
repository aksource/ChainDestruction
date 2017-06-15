package ak.chaindestruction.command;

import ak.chaindestruction.capability.CapabilityCDPlayerStatusHandler;
import ak.chaindestruction.capability.ICDPlayerStatusHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import static ak.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;
import static ak.akapi.Constants.COMMAND_SHOW_PLAYER_CD_STATUS;
import static ak.akapi.Constants.COMMAND_USAGE_SHOW_PLAYER_STATUS;

/**
 * プレイヤーの現在の設定を表示するコマンド
 * Created by A.K. on 2016/09/27.
 */
public class CommandShowPlayerCDStatus extends CommandBase {
    @Override
    public String getName() {
        return COMMAND_SHOW_PLAYER_CD_STATUS;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return COMMAND_USAGE_SHOW_PLAYER_STATUS;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            EntityPlayer entityPlayer = getCommandSenderAsPlayer(sender);
            if (entityPlayer.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null)) {
                ICDPlayerStatusHandler status = entityPlayer.getCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
                String statusStr = CapabilityCDPlayerStatusHandler.makePlayerStatusToString(status);
                entityPlayer.sendMessage(new TextComponentString(statusStr));
            }
        } else {
            for (String username : args) {
                Entity entity = getEntity(server, sender, username);
                if (entity instanceof EntityPlayer && entity.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null)) {
                    ICDPlayerStatusHandler status = entity.getCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
                    String statusStr = CapabilityCDPlayerStatusHandler.makePlayerStatusToString(status);
                    entity.sendMessage(new TextComponentString(statusStr));
                }
            }
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 1;
    }
}
