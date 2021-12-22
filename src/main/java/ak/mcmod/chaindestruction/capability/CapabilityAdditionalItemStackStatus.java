package ak.mcmod.chaindestruction.capability;

import ak.mcmod.chaindestruction.ChainDestruction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import static ak.mcmod.chaindestruction.capability.CapabilityAdditionalPlayerStatus.COMMA_JOINER;

/**
 * 連鎖破壊ItemStackステータスハンドリングクラス
 * Created by A.K. on 2016/09/25.
 */
@SuppressWarnings("CanBeFinal")
public class CapabilityAdditionalItemStackStatus {
  public static final ResourceLocation CD_ITEM_STATUS = new ResourceLocation(ChainDestruction.MOD_ID, "cd_itemstackstatus");
  public static final Capability<IAdditionalItemStackStatus> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

  /**
   * アイテムステータスコピーメソッド
   *
   * @param copyFrom コピー元
   * @param copyTo   コピー先
   */
  public static void copyItemState(IAdditionalItemStackStatus copyFrom, IAdditionalItemStackStatus copyTo) {
    copyTo.setEnableBlocks(copyFrom.getEnableBlocks());
    copyTo.setEnableLogBlocks(copyFrom.getEnableLogBlocks());
  }

  public static String makeItemsStatusToString(IAdditionalItemStackStatus statusHandler) {
    var sb = new StringBuilder();
    sb.append("enableBlocks:[");
    COMMA_JOINER.appendTo(sb, statusHandler.getEnableBlocks());
    sb.append("]\n");
    sb.append("enableLogBlocks:[");
    COMMA_JOINER.appendTo(sb, statusHandler.getEnableLogBlocks());
    sb.append("]\n");
    return sb.toString();
  }
}
