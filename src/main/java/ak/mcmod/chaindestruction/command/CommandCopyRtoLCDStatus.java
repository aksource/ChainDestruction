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

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Objects;

import static ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_ITEM;
import static ak.mcmod.chaindestruction.api.Constants.COMMAND_COPY_R_TO_L;
import static ak.mcmod.chaindestruction.api.Constants.COOMAND_USAGE_COPY_R_TO_L;

/**
 * 右手のアイテムの設定を左手のアイテムにコピーするコマンド
 * Created by A.K. on 2016/09/26.
 */

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
            if (!itemMainHand.isEmpty() && !itemOffHand.isEmpty()
                    && itemMainHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)
                    && itemOffHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
                ICDItemStackStatusHandler copyFrom = itemMainHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                ICDItemStackStatusHandler copyTo = itemOffHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                CapabilityCDItemStackStatusHandler.copyItemState(Objects.requireNonNull(copyFrom), Objects.requireNonNull(copyTo));
            }
        } else {
            for (String username : args) {
                Entity entity = getEntity(server, sender, username);
                if (entity instanceof EntityPlayer) {
                    EntityPlayer entityPlayer = (EntityPlayer) entity;
                    itemMainHand = entityPlayer.getHeldItemMainhand();
                    itemOffHand = entityPlayer.getHeldItemOffhand();
                    if (!itemMainHand.isEmpty() && itemOffHand.isEmpty()
                            && itemMainHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)
                            && itemOffHand.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
                        ICDItemStackStatusHandler copyFrom = itemMainHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                        ICDItemStackStatusHandler copyTo = itemOffHand.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
                        CapabilityCDItemStackStatusHandler.copyItemState(Objects.requireNonNull(copyFrom), Objects.requireNonNull(copyTo));
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
