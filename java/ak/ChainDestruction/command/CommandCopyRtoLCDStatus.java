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

import static ak.ChainDestruction.capability.CapabilityCDItemStackStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_ITEM;
import static ak.akapi.Constants.COMMAND_COPY_R_TO_L;
import static ak.akapi.Constants.COOMAND_USAGE_COPY_R_TO_L;

/**
 * 右手のアイテムの設定を左手のアイテムにコピーするコマンド
 * Created by A.K. on 2016/09/26.
 */
public class CommandCopyRtoLCDStatus extends CommandBase {
    @Override
    public String getName() {
        return COMMAND_COPY_R_TO_L;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return COOMAND_USAGE_COPY_R_TO_L;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        ItemStack itemMainHand;
        ItemStack itemOffHand;
        if (args.length == 0) {
            EntityPlayer entityPlayer = getCommandSenderAsPlayer(sender);
            itemMainHand = entityPlayer.getHeldItemMainhand();
            itemOffHand = entityPlayer.getHeldItemOffhand();
            if (itemMainHand != ItemStack.EMPTY && itemOffHand != ItemStack.EMPTY
                    && itemMainHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)
                    && itemOffHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
                ICDItemStackStatusHandler copyFrom = itemMainHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                ICDItemStackStatusHandler copyTo = itemOffHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                CapabilityCDItemStackStatusHandler.copyItemState(copyFrom, copyTo);
            }
        } else {
            for (String username : args) {
                Entity entity = getEntity(server, sender, username);
                if (entity instanceof EntityPlayer) {
                    EntityPlayer entityPlayer = (EntityPlayer) entity;
                    itemMainHand = entityPlayer.getHeldItemMainhand();
                    itemOffHand = entityPlayer.getHeldItemOffhand();
                    if (itemMainHand != null && itemOffHand != null
                            && itemMainHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)
                            && itemOffHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
                        ICDItemStackStatusHandler copyFrom = itemMainHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                        ICDItemStackStatusHandler copyTo = itemOffHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                        CapabilityCDItemStackStatusHandler.copyItemState(copyFrom, copyTo);
                    }
                }
            }
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
