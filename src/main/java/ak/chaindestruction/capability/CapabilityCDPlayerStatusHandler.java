package ak.chaindestruction.capability;

import ak.chaindestruction.ChainDestruction;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;

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
            public INBTBase writeNBT(Capability<ICDPlayerStatusHandler> capability, ICDPlayerStatusHandler instance, EnumFacing side) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.putByte(CDPlayerStatus.NBT_CLICK_FACE, (byte) instance.getFace().getIndex());
                nbt.putBoolean(CDPlayerStatus.NBT_STATUS_DIG_UNDER, instance.isDigUnder());
                nbt.putBoolean(CDPlayerStatus.NBT_STATUS_TREE_MODE, instance.isTreeMode());
                nbt.putBoolean(CDPlayerStatus.NBT_STATUS_PRIVATE_MODE, instance.isPrivateRegisterMode());
                nbt.putInt(CDPlayerStatus.NBT_STATUS_MAX_DESTROY_BLOCK, instance.getMaxDestroyedBlock());
                NBTTagList nbtTagListEnableItems = new NBTTagList();
                instance.getEnableItems().forEach(itemsStr -> nbtTagListEnableItems.add(new NBTTagString(itemsStr)));
                nbt.put(CDPlayerStatus.NBT_STATUS_ENABLE_ITEMS, nbtTagListEnableItems);
                NBTTagList nbtTagListEnableBlocks = new NBTTagList();
                instance.getEnableBlocks().forEach(blockStr -> nbtTagListEnableBlocks.add(new NBTTagString(blockStr)));
                nbt.put(CDPlayerStatus.NBT_STATUS_ENABLE_BLOCKS, nbtTagListEnableBlocks);
                NBTTagList nbtTagListEnableLogBlocks = new NBTTagList();
                instance.getEnableLogBlocks().forEach(blockStr -> nbtTagListEnableLogBlocks.add(new NBTTagString(blockStr)));
                nbt.put(CDPlayerStatus.NBT_STATUS_ENABLE_LOG_BLOCKS, nbtTagListEnableLogBlocks);
                return nbt;
            }

            @Override
            public void readNBT(Capability<ICDPlayerStatusHandler> capability, ICDPlayerStatusHandler instance, EnumFacing side, INBTBase nbt) {
                if (nbt instanceof NBTTagCompound) {
                    NBTTagCompound nbtTagCompound = (NBTTagCompound) nbt;
                    instance.setFace(EnumFacing.values()[nbtTagCompound.getByte(CDPlayerStatus.NBT_CLICK_FACE) & 0xFF]);
                    instance.setDigUnder(nbtTagCompound.getBoolean(CDPlayerStatus.NBT_STATUS_DIG_UNDER));
                    instance.setTreeMode(nbtTagCompound.getBoolean(CDPlayerStatus.NBT_STATUS_TREE_MODE));
                    instance.setPrivateRegisterMode(nbtTagCompound.getBoolean(CDPlayerStatus.NBT_STATUS_PRIVATE_MODE));
                    instance.setMaxDestroyedBlock(nbtTagCompound.getInt(CDPlayerStatus.NBT_STATUS_MAX_DESTROY_BLOCK));
                    Set<String> enableItems = Sets.newHashSet();
                    NBTTagList nbtTagListEnableItems = nbtTagCompound.getList(CDPlayerStatus.NBT_STATUS_ENABLE_ITEMS, Constants.NBT.TAG_STRING);
                    for (int i = 0; i < nbtTagListEnableItems.size(); i++) {
                        enableItems.add(nbtTagListEnableItems.getString(i));
                    }
                    instance.setEnableItems(enableItems);
                    Set<String> enableBlocks = Sets.newHashSet();
                    NBTTagList nbtTagListEnableBlocks = nbtTagCompound.getList(CDPlayerStatus.NBT_STATUS_ENABLE_BLOCKS, Constants.NBT.TAG_STRING);
                    for (int i = 0; i < nbtTagListEnableBlocks.size(); i++) {
                        enableBlocks.add(nbtTagListEnableBlocks.getString(i));
                    }
                    instance.setEnableBlocks(enableBlocks);
                    Set<String> enableLogBlocks = Sets.newHashSet();
                    NBTTagList nbtTagListEnableLogBlocks = nbtTagCompound.getList(CDPlayerStatus.NBT_STATUS_ENABLE_LOG_BLOCKS, Constants.NBT.TAG_STRING);
                    for (int i = 0; i < nbtTagListEnableLogBlocks.size(); i++) {
                        enableLogBlocks.add(nbtTagListEnableLogBlocks.getString(i));
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
