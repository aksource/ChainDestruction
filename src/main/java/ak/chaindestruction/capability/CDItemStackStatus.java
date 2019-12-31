package ak.chaindestruction.capability;

import ak.chaindestruction.ConfigUtils;
import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

import static ak.chaindestruction.capability.CapabilityCDItemStackStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_ITEM;

/**
 * ItemStack用連鎖破壊ステータス実装クラス
 * Created by A.K. on 2016/09/25.
 */
public class CDItemStackStatus implements ICDItemStackStatusHandler, ICapabilitySerializable<CompoundNBT> {
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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        return !ConfigUtils.COMMON.excludeItemPredicate.test(this.itemStack.getItem().getRegistryName()) ? CAPABILITY_CHAIN_DESTRUCTION_ITEM.orEmpty(capability, LazyOptional.of(() -> this)) : LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT ListNBTEnableBlocks = new ListNBT();
        enableBlocks.forEach(blockStr -> ListNBTEnableBlocks.add(new StringNBT(blockStr)));
        if (ListNBTEnableBlocks.size() > 0) {
            nbt.put(NBT_STATUS_ENABLE_BLOCKS, ListNBTEnableBlocks);
        }
        ListNBT ListNBTEnableLogBlocks = new ListNBT();
        enableLogBlocks.forEach(blockStr -> ListNBTEnableLogBlocks.add(new StringNBT(blockStr)));
        if (ListNBTEnableLogBlocks.size() > 0) {
            nbt.put(NBT_STATUS_ENABLE_LOG_BLOCKS, ListNBTEnableLogBlocks);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        enableBlocks = Sets.newHashSet();
        ListNBT ListNBTEnableBlocks = nbt.getList(NBT_STATUS_ENABLE_BLOCKS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < ListNBTEnableBlocks.size();i++) {
            enableBlocks.add(ListNBTEnableBlocks.getString(i));
        }
        enableLogBlocks = Sets.newHashSet();
        ListNBT ListNBTEnableLogBlocks = nbt.getList(NBT_STATUS_ENABLE_LOG_BLOCKS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < ListNBTEnableLogBlocks.size();i++) {
            enableLogBlocks.add(ListNBTEnableLogBlocks.getString(i));
        }
    }
}
