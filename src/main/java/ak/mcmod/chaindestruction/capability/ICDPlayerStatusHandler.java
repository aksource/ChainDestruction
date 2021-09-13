package ak.mcmod.chaindestruction.capability;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

/**
 * EntityPlayerに保持する連鎖破壊用ステータスのインターフェース
 * Created by A.K. on 2016/09/19.
 */
public interface ICDPlayerStatusHandler {

    /**
     * 破壊時の接触面取得
     *
     * @return 接触面
     */
    EnumFacing getFace();

    /**
     * 破壊時の接触面設定
     *
     * @param face 接触面
     */
    void setFace(EnumFacing face);

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
    void setTreeMode(boolean treeMode);

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
     * @return Set<String>
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
     * @return Set<String>
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
     * @return Set<String>
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
}
