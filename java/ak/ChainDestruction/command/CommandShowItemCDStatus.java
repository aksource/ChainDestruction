package ak.ChainDestruction.command;

import ak.ChainDestruction.capability.CapabilityCDItemStackStatusHandler;
import ak.ChainDestruction.capability.ICDItemStackStatusHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import static ak.ChainDestruction.capability.CapabilityCDItemStackStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_ITEM;
import static ak.ChainDestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;
import static ak.akapi.Constants.COMMAND_SHOW_ITEM_CD_STATUS;
import static ak.akapi.Constants.COMMAND_USAGE_SHOW_ITEMSTATUS;

/**
 * アイテムの現在の設定を表示するコマンド
 * Created by A.K. on 2016/09/28.
 */
public class CommandShowItemCDStatus extends CommandBase {
    @Override
    public String getCommandName() {
        return COMMAND_SHOW_ITEM_CD_STATUS;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return COMMAND_USAGE_SHOW_ITEMSTATUS;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            EntityPlayer entityPlayer = getCommandSenderAsPlayer(sender);
            ItemStack itemMainHand = entityPlayer.getHeldItemMainhand();
            if (itemMainHand != null && itemMainHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
                ICDItemStackStatusHandler status = itemMainHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                String statusStr = CapabilityCDItemStackStatusHandler.makeItemsStatusToString(status);
                entityPlayer.addChatMessage(new TextComponentString(statusStr));
            }
        } else {
            for (String username : args) {
                Entity entity = getEntity(server, sender, username);
                if (entity instanceof EntityPlayer && entity.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null)) {
                    ItemStack itemMainHand = ((EntityPlayer) entity).getHeldItemMainhand();
                    if (itemMainHand != null && itemMainHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
                        ICDItemStackStatusHandler status = itemMainHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                        String statusStr = CapabilityCDItemStackStatusHandler.makeItemsStatusToString(status);
                        entity.addChatMessage(new TextComponentString(statusStr));
                    }
                }
            }
        }
    }
}
