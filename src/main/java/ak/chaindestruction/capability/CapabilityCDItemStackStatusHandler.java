package ak.chaindestruction.capability;

import ak.chaindestruction.ChainDestruction;
import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;

import java.util.Set;

import static ak.chaindestruction.capability.CapabilityCDPlayerStatusHandler.COMMA_JOINER;

/**
 * 連鎖破壊ItemStackステータスハンドリングクラス
 * Created by A.K. on 2016/09/25.
 */
public class CapabilityCDItemStackStatusHandler {
    public final static ResourceLocation CD_ITEM_STATUS = new ResourceLocation(ChainDestruction.MOD_ID, "cd_itemstackstatus");
    @CapabilityInject(ICDItemStackStatusHandler.class)
    public static Capability<ICDItemStackStatusHandler> CAPABILITY_CHAIN_DESTRUCTION_ITEM = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(ICDItemStackStatusHandler.class, new Capability.IStorage<ICDItemStackStatusHandler>() {
            @Override
            public INBT writeNBT(Capability<ICDItemStackStatusHandler> capability, ICDItemStackStatusHandler instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();
                ListNBT ListNBTEnableBlocks = new ListNBT();
                instance.getEnableBlocks().forEach(blockStr -> ListNBTEnableBlocks.add(StringNBT.valueOf(blockStr)));
                nbt.put(CDPlayerStatus.NBT_STATUS_ENABLE_BLOCKS, ListNBTEnableBlocks);
                ListNBT ListNBTEnableLogBlocks = new ListNBT();
                instance.getEnableLogBlocks().forEach(blockStr -> ListNBTEnableLogBlocks.add(StringNBT.valueOf(blockStr)));
                nbt.put(CDPlayerStatus.NBT_STATUS_ENABLE_LOG_BLOCKS, ListNBTEnableLogBlocks);
                return nbt;
            }

            @Override
            public void readNBT(Capability<ICDItemStackStatusHandler> capability, ICDItemStackStatusHandler instance, Direction side, INBT nbt) {
                if (nbt instanceof CompoundNBT) {
                    CompoundNBT CompoundNBT = (CompoundNBT) nbt;
                    Set<String> enableBlocks = Sets.newHashSet();
                    ListNBT ListNBTEnableBlocks = CompoundNBT.getList(CDPlayerStatus.NBT_STATUS_ENABLE_BLOCKS, Constants.NBT.TAG_STRING);
                    for (int i = 0; i < ListNBTEnableBlocks.size();i++) {
                        enableBlocks.add(ListNBTEnableBlocks.getString(i));
                    }
                    instance.setEnableBlocks(enableBlocks);
                    Set<String> enableLogBlocks = Sets.newHashSet();
                    ListNBT ListNBTEnableLogBlocks = CompoundNBT.getList(CDPlayerStatus.NBT_STATUS_ENABLE_LOG_BLOCKS, Constants.NBT.TAG_STRING);
                    for (int i = 0; i < ListNBTEnableLogBlocks.size();i++) {
                        enableLogBlocks.add(ListNBTEnableLogBlocks.getString(i));
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
