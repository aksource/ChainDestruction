package ak.mcmod.chaindestruction.capability;

import com.google.common.collect.Sets;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

import static ak.mcmod.chaindestruction.api.Constants.*;

/**
 * ItemStack用連鎖破壊ステータス実装クラス
 * Created by A.K. on 2016/09/25.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdditionalItemStackStatus implements IAdditionalItemStackStatus {
  private final ItemStack itemStack;
  private Set<String> enableBlocks = Sets.newHashSet();
  private Set<String> enableLogBlocks = Sets.newHashSet();

  public AdditionalItemStackStatus() {
    this.itemStack = ItemStack.EMPTY;
  }

  public AdditionalItemStackStatus(ItemStack itemStack) {
    this.itemStack = itemStack;
  }

  @Override
  public Set<String> getEnableBlocks() {
    return this.enableBlocks;
  }

  @Override
  public void setEnableBlocks(Set<String> enableBlocks) {
    this.enableBlocks = enableBlocks;
  }

  @Override
  public Set<String> getEnableLogBlocks() {
    return this.enableLogBlocks;
  }

  @Override
  public void setEnableLogBlocks(Set<String> enableLogBlocks) {
    this.enableLogBlocks = enableLogBlocks;
  }

  @Override
  public CompoundTag serializeNBT() {
    var nbt = new CompoundTag();
    var listNBTEnableBlocks = new ListTag();
    enableBlocks.forEach(blockStr -> listNBTEnableBlocks.add(StringTag.valueOf(blockStr)));
    if (listNBTEnableBlocks.size() > 0) {
      nbt.put(NBT_STATUS_ENABLE_BLOCKS, listNBTEnableBlocks);
    }
    var listNBTEnableLogBlocks = new ListTag();
    enableLogBlocks.forEach(blockStr -> listNBTEnableLogBlocks.add(StringTag.valueOf(blockStr)));
    if (listNBTEnableLogBlocks.size() > 0) {
      nbt.put(NBT_STATUS_ENABLE_LOG_BLOCKS, listNBTEnableLogBlocks);
    }
    return nbt;
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    enableBlocks = Sets.newHashSet();
    var listNBTEnableBlocks = nbt.getList(NBT_STATUS_ENABLE_BLOCKS, Tag.TAG_STRING);
    for (int i = 0; i < listNBTEnableBlocks.size(); i++) {
      enableBlocks.add(listNBTEnableBlocks.getString(i));
    }
    enableLogBlocks = Sets.newHashSet();
    var listNBTEnableLogBlocks = nbt.getList(NBT_STATUS_ENABLE_LOG_BLOCKS, Tag.TAG_STRING);
    for (int i = 0; i < listNBTEnableLogBlocks.size(); i++) {
      enableLogBlocks.add(listNBTEnableLogBlocks.getString(i));
    }
  }
}
