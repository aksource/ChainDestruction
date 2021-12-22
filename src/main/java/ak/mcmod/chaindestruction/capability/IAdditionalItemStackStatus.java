package ak.mcmod.chaindestruction.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Set;

/**
 * プライベートモード時のItemStackに保持する情報のインターフェース
 * Created by A.K. on 2016/09/25.
 */
public interface IAdditionalItemStackStatus extends INBTSerializable<CompoundTag> {
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
}
