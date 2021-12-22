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
public class AdditionalPlayerStatusCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
  private final AdditionalPlayerStatus playerStatus = new AdditionalPlayerStatus();
  private final LazyOptional<IAdditionalPlayerStatus> optional = LazyOptional.of(() -> playerStatus);
  private final Capability<IAdditionalPlayerStatus> capability = CapabilityAdditionalPlayerStatus.CAPABILITY;

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    if (cap == capability) return optional.cast();
    return LazyOptional.empty();
  }

  @Override
  public CompoundTag serializeNBT() {
    return playerStatus.serializeNBT();
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    playerStatus.deserializeNBT(nbt);
  }
}
