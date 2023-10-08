package ak.mcmod.chaindestruction.api;

/**
 * 連鎖破壊MODの定数クラス
 * Created by A.K. on 2015/09/26.
 */
public class Constants {
  public static final byte REG_KEY = 0;
  public static final byte DIG_KEY = 1;
  public static final byte MODE_KEY = 2;
  public static final byte MIDDLE_CLICK = 2;
  public static final int MIN_Y = -64;
  public static final String KEY_CATEGORY = "ChainDestruction";

  public static final String COMMAND_PLAYER_CD_STATUS = "cdplayerstatus";
  public static final String COMMAND_ITEM_CD_STATUS = "cditemstatus";

  public static final String KEY_REGISTER_ITEM = "chaindestruction.key.register_item";
  public static final String KEY_DIG_UNDER = "chaindestruction.key.enable_digging_under";
  public static final String KEY_CHANGE_MODE = "chaindestruction.key.change_mode";
  public static final String COMMAND_ITEM_STATUS_COPY_SUCCESS = "chaindestruction.commands.item_status.copy.success";
  public static final String COMMAND_PLAYER_STATUS_ADD_FORBIDDEN_TAG_SUCCESS = "chaindestruction.commands.player_status.add.forbidden_tag.success";
  public static final String COMMAND_PLAYER_STATUS_REMOVE_FORBIDDEN_TAG_SUCCESS = "chaindestruction.commands.player_status.remove.forbidden_tag.success";
  public static final String COMMAND_PLAYER_STATUS_RESET_SUCCESS = "chaindestruction.commands.player_status.reset.success";
  public static final String NBT_STATUS_DIG_UNDER = "cd:digUnder";
  public static final String NBT_CLICK_FACE = "cd:clickFace";
  public static final String NBT_STATUS_TREE_MODE = "cd:treeMode";
  public static final String NBT_STATUS_MODE_TYPE = "cd:modeType";
  public static final String NBT_STATUS_PRIVATE_MODE = "cd:privateMode";
  public static final String NBT_STATUS_MAX_DESTROY_BLOCK = "cd:maxDestroyedBlock";
  public static final String NBT_STATUS_ENABLE_ITEMS = "cd:enableItems";
  public static final String NBT_STATUS_ENABLE_BLOCKS = "cd:enableBlocks";
  public static final String NBT_STATUS_ENABLE_LOG_BLOCKS = "cd:enableLogBlocks";
  public static final String NBT_STATUS_FORBIDDEN_TAGS = "cd:forbiddenTags";
}
