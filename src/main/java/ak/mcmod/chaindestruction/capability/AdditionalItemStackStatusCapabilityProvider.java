package ak.mcmod.chaindestruction.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by A.K. on 2021/12/17.
 */
public class AdditionalItemStackStatusCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
  private final AdditionalItemStackStatus itemStackStatus = new AdditionalItemStackStatus();
  private final LazyOptional<IAdditionalItemStackStatus> optional = LazyOptional.of(() -> itemStackStatus);
  private final Capability<IAdditionalItemStackStatus> capability = CapabilityAdditionalItemStackStatus.CAPABILITY;

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    if (cap == capability) return optional.cast();
    return LazyOptional.empty();
  }

  @Override
  public CompoundTag serializeNBT() {
    return itemStackStatus.serializeNBT();
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    itemStackStatus.deserializeNBT(nbt);
  }
}
