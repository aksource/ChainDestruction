package ak.chaindestruction.capability;

import static ak.chaindestruction.capability.CapabilityCDItemStackStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_ITEM;

import ak.chaindestruction.ConfigUtils;
import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

/**
 * ItemStack用連鎖破壊ステータス実装クラス
 * Created by A.K. on 2016/09/25.
 */
public class CDItemStackStatus implements ICDItemStackStatusHandler, ICapabilitySerializable<NBTTagCompound> {
    private static final String NBT_STATUS_ENABLE_BLOCKS = "cd:enableBlocks";
    private static final String NBT_STATUS_ENABLE_LOG_BLOCKS = "cd:enableLogBlocks";
    private Set<String> enableBlocks = Sets.newHashSet();
    private Set<String> enableLogBlocks = Sets.newHashSet();
    private final ItemStack itemStack;

    CDItemStackStatus() {
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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return !ConfigUtils.COMMON.excludeItemPredicate.test(this.itemStack.getItem().getRegistryName()) ? CAPABILITY_CHAIN_DESTRUCTION_ITEM.orEmpty(capability, LazyOptional.of(() -> this)) : LazyOptional.empty();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList nbtTagListEnableBlocks = new NBTTagList();
        enableBlocks.forEach(blockStr -> nbtTagListEnableBlocks.add(new NBTTagString(blockStr)));
        if (nbtTagListEnableBlocks.size() > 0) {
            nbt.put(NBT_STATUS_ENABLE_BLOCKS, nbtTagListEnableBlocks);
        }
        NBTTagList nbtTagListEnableLogBlocks = new NBTTagList();
        enableLogBlocks.forEach(blockStr -> nbtTagListEnableLogBlocks.add(new NBTTagString(blockStr)));
        if (nbtTagListEnableLogBlocks.size() > 0) {
            nbt.put(NBT_STATUS_ENABLE_LOG_BLOCKS, nbtTagListEnableLogBlocks);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        enableBlocks = Sets.newHashSet();
        NBTTagList nbtTagListEnableBlocks = nbt.getList(NBT_STATUS_ENABLE_BLOCKS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < nbtTagListEnableBlocks.size();i++) {
            enableBlocks.add(nbtTagListEnableBlocks.getString(i));
        }
        enableLogBlocks = Sets.newHashSet();
        NBTTagList nbtTagListEnableLogBlocks = nbt.getList(NBT_STATUS_ENABLE_LOG_BLOCKS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < nbtTagListEnableLogBlocks.size();i++) {
            enableLogBlocks.add(nbtTagListEnableLogBlocks.getString(i));
        }
    }
}
