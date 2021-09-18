package ak.mcmod.chaindestruction.capability;

import ak.mcmod.chaindestruction.util.ConfigUtils;
import com.google.common.collect.Sets;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

import static ak.mcmod.chaindestruction.api.Constants.NBT_STATUS_ENABLE_BLOCKS;
import static ak.mcmod.chaindestruction.api.Constants.NBT_STATUS_ENABLE_LOG_BLOCKS;
import static ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_ITEM;
import static net.minecraftforge.common.util.Constants.NBT.TAG_STRING;

/**
 * ItemStack用連鎖破壊ステータス実装クラス
 * Created by A.K. on 2016/09/25.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CDItemStackStatus implements ICDItemStackStatusHandler, ICapabilitySerializable<CompoundNBT>, Capability.IStorage<ICDItemStackStatusHandler> {
  private Set<String> enableBlocks = Sets.newHashSet();
  private Set<String> enableLogBlocks = Sets.newHashSet();
  private final ItemStack itemStack;

  public CDItemStackStatus() {
    this.itemStack = ItemStack.EMPTY;
  }

  public CDItemStackStatus(ItemStack itemStack) {
    this.itemStack = itemStack;
  }

  public static LazyOptional<ICDItemStackStatusHandler> get(ItemStack itemStack) {
    return itemStack.getCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null);
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
  @Nonnull
  public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
    return !ConfigUtils.COMMON.excludeItemPredicate.test(this.itemStack.getItem().getRegistryName()) ? CAPABILITY_CHAIN_DESTRUCTION_ITEM.orEmpty(capability, LazyOptional.of(() -> this)) : LazyOptional.empty();
  }

  @Override
  public CompoundNBT serializeNBT() {
    CompoundNBT nbt = new CompoundNBT();
    ListNBT listNBTEnableBlocks = new ListNBT();
    enableBlocks.forEach(blockStr -> listNBTEnableBlocks.add(StringNBT.valueOf(blockStr)));
    if (listNBTEnableBlocks.size() > 0) {
      nbt.put(NBT_STATUS_ENABLE_BLOCKS, listNBTEnableBlocks);
    }
    ListNBT listNBTEnableLogBlocks = new ListNBT();
    enableLogBlocks.forEach(blockStr -> listNBTEnableLogBlocks.add(StringNBT.valueOf(blockStr)));
    if (listNBTEnableLogBlocks.size() > 0) {
      nbt.put(NBT_STATUS_ENABLE_LOG_BLOCKS, listNBTEnableLogBlocks);
    }
    return nbt;
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {
    enableBlocks = Sets.newHashSet();
    ListNBT listNBTEnableBlocks = nbt.getList(NBT_STATUS_ENABLE_BLOCKS, TAG_STRING);
    for (int i = 0; i < listNBTEnableBlocks.size(); i++) {
      enableBlocks.add(listNBTEnableBlocks.getString(i));
    }
    enableLogBlocks = Sets.newHashSet();
    ListNBT listNBTEnableLogBlocks = nbt.getList(NBT_STATUS_ENABLE_LOG_BLOCKS, TAG_STRING);
    for (int i = 0; i < listNBTEnableLogBlocks.size(); i++) {
      enableLogBlocks.add(listNBTEnableLogBlocks.getString(i));
    }
  }

  @Nullable
  @Override
  public INBT writeNBT(Capability<ICDItemStackStatusHandler> capability, ICDItemStackStatusHandler instance, Direction side) {
    if (capability == CAPABILITY_CHAIN_DESTRUCTION_ITEM) {
      return serializeNBT();
    }
    return null;
  }

  @Override
  public void readNBT(Capability<ICDItemStackStatusHandler> capability, ICDItemStackStatusHandler instance, Direction side, INBT nbt) {
    if (capability == CAPABILITY_CHAIN_DESTRUCTION_ITEM) {
      deserializeNBT((CompoundNBT) nbt);
    }
  }
}
