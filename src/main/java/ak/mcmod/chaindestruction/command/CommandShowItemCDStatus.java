package ak.mcmod.chaindestruction.command;

import ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler;
import ak.mcmod.chaindestruction.capability.ICDItemStackStatusHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Objects;

import static ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_ITEM;
import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;
import static ak.mcmod.chaindestruction.api.Constants.COMMAND_SHOW_ITEM_CD_STATUS;
import static ak.mcmod.chaindestruction.api.Constants.COMMAND_USAGE_SHOW_ITEMSTATUS;

/**
 * アイテムの現在の設定を表示するコマンド
 * Created by A.K. on 2016/09/28.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandShowItemCDStatus extends CommandBase {
    @Override
    public String getName() {
        return COMMAND_SHOW_ITEM_CD_STATUS;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return COMMAND_USAGE_SHOW_ITEMSTATUS;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            EntityPlayer entityPlayer = getCommandSenderAsPlayer(sender);
            ItemStack itemMainHand = entityPlayer.getHeldItemMainhand();
            if (!itemMainHand.isEmpty() && itemMainHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
                ICDItemStackStatusHandler status = itemMainHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                String statusStr = CapabilityCDItemStackStatusHandler.makeItemsStatusToString(Objects.requireNonNull(status));
                entityPlayer.sendMessage(new TextComponentString(statusStr));
            }
        } else {
            for (String username : args) {
                Entity entity = getEntity(server, sender, username);
                if (entity instanceof EntityPlayer && entity.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null)) {
                    ItemStack itemMainHand = ((EntityPlayer) entity).getHeldItemMainhand();
                    if (!itemMainHand.isEmpty() && itemMainHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
                        ICDItemStackStatusHandler status = itemMainHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                        String statusStr = CapabilityCDItemStackStatusHandler.makeItemsStatusToString(Objects.requireNonNull(status));
                        entity.sendMessage(new TextComponentString(statusStr));
                    }
                }
            }
        }
    }
}
