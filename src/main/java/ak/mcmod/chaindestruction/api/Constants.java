package ak.mcmod.chaindestruction.api;

/**
 * 連鎖破壊MODの定数クラス
 * Created by A.K. on 2015/09/26.
 */
public class Constants {
    public static final byte RegKEY = 0;
    public static final byte DigKEY = 1;
    public static final byte ModeKEY = 2;
    public static final byte MIDDLE_CLICK = 2;
    public static final int MIN_Y = 0;
    public static final String KEY_CATEGORY = "ChainDestruction";

    public static final String COMMAND_COPY_R_TO_L = "cdcopyrtol";
    public static final String COMMAND_RESET_PLAYER_STATUS = "cdresetplayerstatus";
    public static final String COMMAND_SHOW_PLAYER_CD_STATUS = "cdshowplayerstatus";
    public static final String COMMAND_SHOW_ITEM_CD_STATUS = "cdshowitemstatus";

    public static final String KEY_REGISTER_ITEM = "chaindestruction.key.register_item";
    public static final String KEY_DIG_UNDER = "chaindestruction.key.enable_digging_under";
    public static final String KEY_TREE_MODE = "chaindestruction.key.change_mode";
    public static final String COMMAND_USAGE_COPY_R_TO_L = "chaindestruction.commands.copy_r_to_l";
    public static final String COMMAND_USAGE_RESET_PLAYER_STATUS = "chaindestruction.commands.reset_player_status";
    public static final String COMMAND_USAGE_SHOW_PLAYER_STATUS = "chaindestruction.commands.show_player_status";
    public static final String COMMAND_USAGE_SHOW_ITEMSTATUS = "chaindestruction.commands.show_item_status";
  public static final String NBT_STATUS_DIG_UNDER = "cd:digUnder";
  public static final String NBT_CLICK_FACE = "cd:clickFace";
  public static final String NBT_STATUS_TREE_MODE = "cd:treeMode";
  public static final String NBT_STATUS_MODE_TYPE = "cd:modeType";
  public static final String NBT_STATUS_PRIVATE_MODE = "cd:privateMode";
  public static final String NBT_STATUS_MAX_DESTORY_BLOCK = "cd:maxDestroyedBlock";
  public static final String NBT_STATUS_ENABLE_ITEMS = "cd:enableItems";
  public static final String NBT_STATUS_ENABLE_BLOCKS = "cd:enableBlocks";
  public static final String NBT_STATUS_ENABLE_LOG_BLOCKS = "cd:enableLogBlocks";
}
