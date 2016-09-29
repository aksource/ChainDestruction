package ak.ChainDestruction.capability;

import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Set;

import static ak.ChainDestruction.capability.CapabilityCDItemStackStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_ITEM;

/**
 * ItemStack用連鎖破壊ステータス実装クラス
 * Created by A.K. on 2016/09/25.
 */
public class CDItemStackStatus implements ICDItemStackStatusHandler, ICapabilitySerializable<NBTTagCompound> {
    public static final String NBT_STATUS_ENABLE_BLOCKS = "cd:enableBlocks";
    public static final String NBT_STATUS_ENABLE_LOG_BLOCKS = "cd:enableLogBlocks";
    public Set<String> enableBlocks = Sets.newHashSet();
    public Set<String> enableLogBlocks = Sets.newHashSet();

    public static ICDItemStackStatusHandler get(ItemStack itemStack) {
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
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CAPABILITY_CHAIN_DESTRUCTION_ITEM;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CAPABILITY_CHAIN_DESTRUCTION_ITEM ? CAPABILITY_CHAIN_DESTRUCTION_ITEM.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList nbtTagListEnableBlocks = new NBTTagList();
        enableBlocks.forEach(blockStr -> nbtTagListEnableBlocks.appendTag(new NBTTagString(blockStr)));
        nbt.setTag(NBT_STATUS_ENABLE_BLOCKS, nbtTagListEnableBlocks);
        NBTTagList nbtTagListEnableLogBlocks = new NBTTagList();
        enableLogBlocks.forEach(blockStr -> nbtTagListEnableLogBlocks.appendTag(new NBTTagString(blockStr)));
        nbt.setTag(NBT_STATUS_ENABLE_LOG_BLOCKS, nbtTagListEnableLogBlocks);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        enableBlocks = Sets.newHashSet();
        NBTTagList nbtTagListEnableBlocks = nbt.getTagList(NBT_STATUS_ENABLE_BLOCKS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < nbtTagListEnableBlocks.tagCount();i++) {
            enableBlocks.add(nbtTagListEnableBlocks.getStringTagAt(i));
        }
        enableLogBlocks = Sets.newHashSet();
        NBTTagList nbtTagListEnableLogBlocks = nbt.getTagList(NBT_STATUS_ENABLE_LOG_BLOCKS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < nbtTagListEnableLogBlocks.tagCount();i++) {
            enableLogBlocks.add(nbtTagListEnableLogBlocks.getStringTagAt(i));
        }
    }
}
