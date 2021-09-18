package ak.mcmod.chaindestruction.capability;

import ak.mcmod.chaindestruction.ChainDestruction;
import com.google.common.base.Joiner;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * 連鎖破壊ステータスハンドリングクラス
 * Created by A.K. on 2016/09/16.
 */
public class CapabilityCDPlayerStatusHandler {

  public final static ResourceLocation CD_STATUS = new ResourceLocation(ChainDestruction.MOD_ID, "cd_status");
  @SuppressWarnings("CanBeFinal")
  @CapabilityInject(ICDPlayerStatusHandler.class)
  public static Capability<ICDPlayerStatusHandler> CAPABILITY_CHAIN_DESTRUCTION_PLAYER = null;
  protected static final Joiner COMMA_JOINER = Joiner.on(',');

  public static void register() {
    CapabilityManager.INSTANCE.register(ICDPlayerStatusHandler.class, new CDPlayerStatus(), CDPlayerStatus::new);
  }

  /**
   * 連鎖破壊設定をコピーするメソッド
   *
   * @param copyFrom コピー元
   * @param copyTo   コピー先
   */
  public static void copyPlayerStatus(ICDPlayerStatusHandler copyFrom, ICDPlayerStatusHandler copyTo) {
    copyTo.setFace(copyFrom.getFace());
    copyTo.setModeType(copyFrom.getModeType());
    copyTo.setDigUnder(copyFrom.isDigUnder());
    copyTo.setPrivateRegisterMode(copyFrom.isPrivateRegisterMode());
    copyTo.setMaxDestroyedBlock(copyFrom.getMaxDestroyedBlock());
    copyTo.setEnableItems(copyFrom.getEnableItems());
    copyTo.setEnableBlocks(copyFrom.getEnableBlocks());
    copyTo.setEnableLogBlocks(copyFrom.getEnableLogBlocks());
  }

  /**
   * プレイヤーの設定を文字列に変換するメソッド
   *
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
