package ak.mcmod.chaindestruction.capability;

import ak.mcmod.chaindestruction.util.ModeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Set;

/**
 * Playerに保持する連鎖破壊用ステータスのインターフェース
 * Created by A.K. on 2016/09/19.
 */
public interface IAdditionalPlayerStatus extends INBTSerializable<CompoundTag> {

  /**
   * 破壊時の接触面取得
   *
   * @return 接触面
   */
  Direction getFace();

  /**
   * 破壊時の接触面設定
   *
   * @param face 接触面
   */
  void setFace(Direction face);

  /**
   * 足元より下まで掘るかどうか
   *
   * @return 足元より下まで掘るかどうか
   */
  boolean isDigUnder();

  /**
   * 足元まで掘るかどうか設定・解除
   *
   * @param digUnder 足元まで掘るかどうか
   */
  void setDigUnder(boolean digUnder);

  /**
   * 木こりモードかどうか
   *
   * @return 木こりモードかどうか
   */
  boolean isTreeMode();

  /**
   * 木こりモードの設定・解除
   *
   * @param treeMode 設定・解除
   */
  @Deprecated
  void setTreeMode(boolean treeMode);

  /**
   * Get ModeType
   *
   * @return ModeType
   */
  ModeType getModeType();

  /**
   * Set ModeType
   *
   * @param modeType ModeType
   */
  void setModeType(ModeType modeType);

  /**
   * アイテムごとの設定を有効にするかどうか
   *
   * @return アイテムごとの設定を有効にするかどうか
   */
  boolean isPrivateRegisterMode();

  /**
   * アイテムごとの設定の有効・無効
   *
   * @param privateRegisterMode 有効・無効
   */
  void setPrivateRegisterMode(boolean privateRegisterMode);

  /**
   * 連鎖破壊ブロック数取得
   *
   * @return 連鎖破壊ブロック数
   */
  int getMaxDestroyedBlock();

  /**
   * 連鎖破壊ブロック数設定
   *
   * @param maxDestroyedBlock 連鎖破壊ブロック数
   */
  void setMaxDestroyedBlock(int maxDestroyedBlock);

  /**
   * 連鎖破壊可能アイテムの集合
   *
   * @return Set&lt;String&gt;
   */
  Set<String> getEnableItems();

  /**
   * 連鎖破壊可能アイテムの集合の設定
   *
   * @param enableItems enableItems
   */
  void setEnableItems(Set<String> enableItems);

  /**
   * 連鎖破壊対象ブロックの集合
   *
   * @return Set&lt;String&gt;
   */
  Set<String> getEnableBlocks();

  /**
   * 連鎖破壊対象ブロックの集合の設定
   *
   * @param enableBlocks enableBlocks
   */
  void setEnableBlocks(Set<String> enableBlocks);

  /**
   * 木こりモード用連鎖破壊対象ブロックの集合
   *
   * @return Set&lt;String&gt;
   */
  Set<String> getEnableLogBlocks();

  /**
   * 木こりモード用連鎖破壊対象ブロックの集合の設定
   *
   * @param enableLogBlocks enableLogBlocks
   */
  void setEnableLogBlocks(Set<String> enableLogBlocks);

  /**
   * 破壊範囲の座標が小さい方の端点クラスを取得
   *
   * @param entity    プレイヤーEntity
   * @param targetPos 最初に破壊したブロック
   * @return 端点クラス
   */
  BlockPos getMinPos(Entity entity, BlockPos targetPos);

  /**
   * 破壊範囲の座標が大きい方の端点クラスを取得
   *
   * @param targetPos 最初に破壊したブロック
   * @return 端点クラス
   */
  BlockPos getMaxPos(BlockPos targetPos);

  /**
   * 登録禁止文字列集合の取得
   * @return Set&lt;String&gt;
   */
  Set<String> getForbiddenTags();

  /**
   * 登録禁止文字列集合の設定
   * @param forbiddenTags forbiddenTags
   */
  void setForbiddenTags(Set<String> forbiddenTags);
}
