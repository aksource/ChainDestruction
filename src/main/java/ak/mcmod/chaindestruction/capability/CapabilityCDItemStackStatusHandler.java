package ak.mcmod.chaindestruction.capability;

import ak.mcmod.chaindestruction.ChainDestruction;
import com.google.common.collect.Sets;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;

import java.util.Set;

import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.COMMA_JOINER;

/**
 * 連鎖破壊ItemStackステータスハンドリングクラス
 * Created by A.K. on 2016/09/25.
 */
public class CapabilityCDItemStackStatusHandler {
    public final static ResourceLocation CD_ITEM_STATUS = new ResourceLocation(ChainDestruction.MOD_ID, "cd:itemstackstatus");
    @SuppressWarnings("CanBeFinal")
    @CapabilityInject(ICDItemStackStatusHandler.class)
    public static Capability<ICDItemStackStatusHandler> CAPABILITY_CHAIN_DESTRUCTION_ITEM = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(ICDItemStackStatusHandler.class, new Capability.IStorage<ICDItemStackStatusHandler>() {
            @Override
            public NBTBase writeNBT(Capability<ICDItemStackStatusHandler> capability, ICDItemStackStatusHandler instance, EnumFacing side) {
                NBTTagCompound nbt = new NBTTagCompound();
                NBTTagList nbtTagListEnableBlocks = new NBTTagList();
                instance.getEnableBlocks().forEach(blockStr -> nbtTagListEnableBlocks.appendTag(new NBTTagString(blockStr)));
                nbt.setTag(CDPlayerStatus.NBT_STATUS_ENABLE_BLOCKS, nbtTagListEnableBlocks);
                NBTTagList nbtTagListEnableLogBlocks = new NBTTagList();
                instance.getEnableLogBlocks().forEach(blockStr -> nbtTagListEnableLogBlocks.appendTag(new NBTTagString(blockStr)));
                nbt.setTag(CDPlayerStatus.NBT_STATUS_ENABLE_LOG_BLOCKS, nbtTagListEnableLogBlocks);
                return nbt;
            }

            @Override
            public void readNBT(Capability<ICDItemStackStatusHandler> capability, ICDItemStackStatusHandler instance, EnumFacing side, NBTBase nbt) {
                if (nbt instanceof NBTTagCompound) {
                    NBTTagCompound nbtTagCompound = (NBTTagCompound) nbt;
                    Set<String> enableBlocks = Sets.newHashSet();
                    NBTTagList nbtTagListEnableBlocks = nbtTagCompound.getTagList(CDPlayerStatus.NBT_STATUS_ENABLE_BLOCKS, Constants.NBT.TAG_STRING);
                    for (int i = 0; i < nbtTagListEnableBlocks.tagCount();i++) {
                        enableBlocks.add(nbtTagListEnableBlocks.getStringTagAt(i));
                    }
                    instance.setEnableBlocks(enableBlocks);
                    Set<String> enableLogBlocks = Sets.newHashSet();
                    NBTTagList nbtTagListEnableLogBlocks = nbtTagCompound.getTagList(CDPlayerStatus.NBT_STATUS_ENABLE_LOG_BLOCKS, Constants.NBT.TAG_STRING);
                    for (int i = 0; i < nbtTagListEnableLogBlocks.tagCount();i++) {
                        enableLogBlocks.add(nbtTagListEnableLogBlocks.getStringTagAt(i));
                    }
                    instance.setEnableLogBlocks(enableLogBlocks);
                }
            }
        }, CDItemStackStatus::new);
    }

    /**
     * アイテムステータスコピーメソッド
     * @param copyFrom コピー元
     * @param copyTo コピー先
     */
    public static void copyItemState(ICDItemStackStatusHandler copyFrom, ICDItemStackStatusHandler copyTo) {
        copyTo.setEnableBlocks(copyFrom.getEnableBlocks());
        copyTo.setEnableLogBlocks(copyFrom.getEnableLogBlocks());
    }
    public static String makeItemsStatusToString(ICDItemStackStatusHandler statusHandler) {
        StringBuilder sb = new StringBuilder();
        sb.append("enableBlocks:[");
        COMMA_JOINER.appendTo(sb, statusHandler.getEnableBlocks());
        sb.append("]\n");
        sb.append("enableLogBlocks:[");
        COMMA_JOINER.appendTo(sb, statusHandler.getEnableLogBlocks());
        sb.append("]\n");
        return sb.toString();
    }
}
