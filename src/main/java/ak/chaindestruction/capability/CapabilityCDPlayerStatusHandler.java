package ak.chaindestruction.capability;

import ak.chaindestruction.ChainDestruction;
import com.google.common.base.Joiner;
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

/**
 * 連鎖破壊ステータスハンドリングクラス
 * Created by A.K. on 2016/09/16.
 */
public class CapabilityCDPlayerStatusHandler {

    public final static ResourceLocation CD_STATUS = new ResourceLocation(ChainDestruction.MOD_ID, "cd_status");
    @CapabilityInject(ICDPlayerStatusHandler.class)
    public static Capability<ICDPlayerStatusHandler> CAPABILITY_CHAIN_DESTRUCTION_PLAYER = null;
    protected static final Joiner COMMA_JOINER = Joiner.on(',');

    public static void register() {
        CapabilityManager.INSTANCE.register(ICDPlayerStatusHandler.class, new Capability.IStorage<ICDPlayerStatusHandler>() {
            @Override
            public INBT writeNBT(Capability<ICDPlayerStatusHandler> capability, ICDPlayerStatusHandler instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putByte(CDPlayerStatus.NBT_CLICK_FACE, (byte) instance.getFace().getIndex());
                nbt.putBoolean(CDPlayerStatus.NBT_STATUS_DIG_UNDER, instance.isDigUnder());
                nbt.putBoolean(CDPlayerStatus.NBT_STATUS_TREE_MODE, instance.isTreeMode());
                nbt.putBoolean(CDPlayerStatus.NBT_STATUS_PRIVATE_MODE, instance.isPrivateRegisterMode());
                nbt.putInt(CDPlayerStatus.NBT_STATUS_MAX_DESTROY_BLOCK, instance.getMaxDestroyedBlock());
                ListNBT ListNBTEnableItems = new ListNBT();
                instance.getEnableItems().forEach(itemsStr -> ListNBTEnableItems.add(new StringNBT(itemsStr)));
                nbt.put(CDPlayerStatus.NBT_STATUS_ENABLE_ITEMS, ListNBTEnableItems);
                ListNBT ListNBTEnableBlocks = new ListNBT();
                instance.getEnableBlocks().forEach(blockStr -> ListNBTEnableBlocks.add(new StringNBT(blockStr)));
                nbt.put(CDPlayerStatus.NBT_STATUS_ENABLE_BLOCKS, ListNBTEnableBlocks);
                ListNBT ListNBTEnableLogBlocks = new ListNBT();
                instance.getEnableLogBlocks().forEach(blockStr -> ListNBTEnableLogBlocks.add(new StringNBT(blockStr)));
                nbt.put(CDPlayerStatus.NBT_STATUS_ENABLE_LOG_BLOCKS, ListNBTEnableLogBlocks);
                return nbt;
            }

            @Override
            public void readNBT(Capability<ICDPlayerStatusHandler> capability, ICDPlayerStatusHandler instance, Direction side, INBT nbt) {
                if (nbt instanceof CompoundNBT) {
                    CompoundNBT CompoundNBT = (CompoundNBT) nbt;
                    instance.setFace(Direction.values()[CompoundNBT.getByte(CDPlayerStatus.NBT_CLICK_FACE) & 0xFF]);
                    instance.setDigUnder(CompoundNBT.getBoolean(CDPlayerStatus.NBT_STATUS_DIG_UNDER));
                    instance.setTreeMode(CompoundNBT.getBoolean(CDPlayerStatus.NBT_STATUS_TREE_MODE));
                    instance.setPrivateRegisterMode(CompoundNBT.getBoolean(CDPlayerStatus.NBT_STATUS_PRIVATE_MODE));
                    instance.setMaxDestroyedBlock(CompoundNBT.getInt(CDPlayerStatus.NBT_STATUS_MAX_DESTROY_BLOCK));
                    Set<String> enableItems = Sets.newHashSet();
                    ListNBT ListNBTEnableItems = CompoundNBT.getList(CDPlayerStatus.NBT_STATUS_ENABLE_ITEMS, Constants.NBT.TAG_STRING);
                    for (int i = 0; i < ListNBTEnableItems.size(); i++) {
                        enableItems.add(ListNBTEnableItems.getString(i));
                    }
                    instance.setEnableItems(enableItems);
                    Set<String> enableBlocks = Sets.newHashSet();
                    ListNBT ListNBTEnableBlocks = CompoundNBT.getList(CDPlayerStatus.NBT_STATUS_ENABLE_BLOCKS, Constants.NBT.TAG_STRING);
                    for (int i = 0; i < ListNBTEnableBlocks.size(); i++) {
                        enableBlocks.add(ListNBTEnableBlocks.getString(i));
                    }
                    instance.setEnableBlocks(enableBlocks);
                    Set<String> enableLogBlocks = Sets.newHashSet();
                    ListNBT ListNBTEnableLogBlocks = CompoundNBT.getList(CDPlayerStatus.NBT_STATUS_ENABLE_LOG_BLOCKS, Constants.NBT.TAG_STRING);
                    for (int i = 0; i < ListNBTEnableLogBlocks.size(); i++) {
                        enableLogBlocks.add(ListNBTEnableLogBlocks.getString(i));
                    }
                    instance.setEnableLogBlocks(enableLogBlocks);
                }
            }
        }, CDPlayerStatus::new);
    }

    /**
     * 連鎖破壊設定をコピーするメソッド
     *
     * @param copyFrom コピー元
     * @param copyTo   コピー先
     */
    public static void copyPlayerStatus(ICDPlayerStatusHandler copyFrom, ICDPlayerStatusHandler copyTo) {
        copyTo.setFace(copyFrom.getFace());
        copyTo.setTreeMode(copyFrom.isTreeMode());
        copyTo.setDigUnder(copyFrom.isDigUnder());
        copyTo.setPrivateRegisterMode(copyFrom.isPrivateRegisterMode());
        copyTo.setMaxDestroyedBlock(copyFrom.getMaxDestroyedBlock());
        copyTo.setEnableItems(copyFrom.getEnableItems());
        copyTo.setEnableBlocks(copyFrom.getEnableBlocks());
        copyTo.setEnableLogBlocks(copyFrom.getEnableLogBlocks());
    }

    /**
     * プレイヤーの設定を文字列に変換するメソッド
     * @param statusHandler 表示させたい設定
     * @return 変換された文字列
     */
    public static String makePlayerStatusToString(ICDPlayerStatusHandler statusHandler) {
        StringBuilder sb = new StringBuilder();
        sb.append("enableItems:[");
        COMMA_JOINER.appendTo(sb, statusHandler.getEnableItems());
        sb.append("]\n");
        sb.append("enableBlocks:[");
        COMMA_JOINER.appendTo(sb, statusHandler.getEnableBlocks());
        sb.append("]\n");
        sb.append("enableLogBlocks:[");
        COMMA_JOINER.appendTo(sb, statusHandler.getEnableLogBlocks());
        sb.append("]\n");
        return sb.toString();
    }
}
