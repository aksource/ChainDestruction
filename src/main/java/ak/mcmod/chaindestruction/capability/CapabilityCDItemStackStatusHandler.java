package ak.mcmod.chaindestruction.capability;

import ak.mcmod.chaindestruction.ChainDestruction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

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
        CapabilityManager.INSTANCE.register(ICDItemStackStatusHandler.class, new CDItemStackStatus(), CDItemStackStatus::new);
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
