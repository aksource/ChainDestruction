package ak.mcmod.chaindestruction.capability;

import ak.mcmod.chaindestruction.ChainDestruction;
import com.google.common.collect.Sets;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

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
public class CDItemStackStatus implements ICDItemStackStatusHandler, ICapabilitySerializable<NBTTagCompound>, Capability.IStorage<ICDItemStackStatusHandler> {
    public Set<String> enableBlocks = Sets.newHashSet();
    public Set<String> enableLogBlocks = Sets.newHashSet();
    private final ItemStack itemStack;

    public CDItemStackStatus() {
        this.itemStack = ItemStack.EMPTY;
    }
    CDItemStackStatus(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Nullable
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
        return !ChainDestruction.excludeItemPredicate.test(this.itemStack.getItem().getRegistryName()) &&
                capability == CAPABILITY_CHAIN_DESTRUCTION_ITEM;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return hasCapability(capability, facing) ? CAPABILITY_CHAIN_DESTRUCTION_ITEM.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList nbtTagListEnableBlocks = new NBTTagList();
        enableBlocks.forEach(blockStr -> nbtTagListEnableBlocks.appendTag(new NBTTagString(blockStr)));
        if (nbtTagListEnableBlocks.tagCount() > 0) {
            nbt.setTag(NBT_STATUS_ENABLE_BLOCKS, nbtTagListEnableBlocks);
        }
        NBTTagList nbtTagListEnableLogBlocks = new NBTTagList();
        enableLogBlocks.forEach(blockStr -> nbtTagListEnableLogBlocks.appendTag(new NBTTagString(blockStr)));
        if (nbtTagListEnableLogBlocks.tagCount() > 0) {
            nbt.setTag(NBT_STATUS_ENABLE_LOG_BLOCKS, nbtTagListEnableLogBlocks);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        enableBlocks = Sets.newHashSet();
        NBTTagList nbtTagListEnableBlocks = nbt.getTagList(NBT_STATUS_ENABLE_BLOCKS, TAG_STRING);
        for (int i = 0; i < nbtTagListEnableBlocks.tagCount();i++) {
            enableBlocks.add(nbtTagListEnableBlocks.getStringTagAt(i));
        }
        enableLogBlocks = Sets.newHashSet();
        NBTTagList nbtTagListEnableLogBlocks = nbt.getTagList(NBT_STATUS_ENABLE_LOG_BLOCKS, TAG_STRING);
        for (int i = 0; i < nbtTagListEnableLogBlocks.tagCount();i++) {
            enableLogBlocks.add(nbtTagListEnableLogBlocks.getStringTagAt(i));
        }
    }

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<ICDItemStackStatusHandler> capability, ICDItemStackStatusHandler instance, EnumFacing side) {
        if (capability == CAPABILITY_CHAIN_DESTRUCTION_ITEM) {
            return serializeNBT();
        }
        return null;
    }

    @Override
    public void readNBT(Capability<ICDItemStackStatusHandler> capability, ICDItemStackStatusHandler instance, EnumFacing side, NBTBase nbt) {
        if (capability == CAPABILITY_CHAIN_DESTRUCTION_ITEM) {
            deserializeNBT((NBTTagCompound) nbt);
        }
    }
}
