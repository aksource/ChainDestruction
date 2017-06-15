package ak.chaindestruction.capability;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * 連鎖破壊のプレイヤー別状態保存クラス
 * Created by A.K. on 2015/09/26.
 */
public class CDPlayerStatus implements ICDPlayerStatusHandler, ICapabilitySerializable<NBTTagCompound> {
    public static final String NBT_STATUS_DIG_UNDER = "cd:digUnder";
    public static final String NBT_CLICK_FACE = "cd:clickFace";
    public static final String NBT_STATUS_TREE_MODE = "cd:treeMode";
    public static final String NBT_STATUS_PRIVATE_MODE = "cd:privateMode";
    public static final String NBT_STATUS_MAX_DESTORY_BLOCK = "cd:maxDestroyedBlock";
    public static final String NBT_STATUS_ENABLE_ITEMS = "cd:enableItems";
    public static final String NBT_STATUS_ENABLE_BLOCKS = "cd:enableBlocks";
    public static final String NBT_STATUS_ENABLE_LOG_BLOCKS = "cd:enableLogBlocks";
    public EnumFacing face = EnumFacing.DOWN;
    private boolean digUnder = false;
    private boolean treeMode = false;
    private boolean privateRegisterMode = false;
    private int maxDestroyedBlock = 5;
    public Set<String> enableItems = Sets.newHashSet(
            "minecraft:diamond_axe", "minecraft:golden_axe", "minecraft:iron_axe", "minecraft:stone_axe", "minecraft:wooden_axe",
            "minecraft:diamond_shovel", "minecraft:golden_shovel", "minecraft:iron_shovel", "minecraft:stone_shovel", "minecraft:wooden_shovel",
            "minecraft:diamond_pickaxe", "minecraft:golden_pickaxe", "minecraft:iron_pickaxe", "minecraft:stone_pickaxe", "minecraft:wooden_pickaxe");
    public Set<String> enableBlocks = Sets.newHashSet(Blocks.OBSIDIAN.getRegistryName().toString(), "glowstone", "ore");
    public Set<String> enableLogBlocks = Sets.newHashSet("logWood", "treeLeaves");

//    public static void register(EntityPlayer player) {
//        player.registerExtendedProperties(CD_STATUS, new CDPlayerStatus());
//    }

    public static ICDPlayerStatusHandler get(EntityPlayer player) {
        return player.getCapability(CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER ? CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte(NBT_CLICK_FACE, (byte) face.getIndex());
        nbt.setBoolean(NBT_STATUS_DIG_UNDER, digUnder);
        nbt.setBoolean(NBT_STATUS_TREE_MODE, treeMode);
        nbt.setBoolean(NBT_STATUS_PRIVATE_MODE, privateRegisterMode);
        nbt.setInteger(NBT_STATUS_MAX_DESTORY_BLOCK, maxDestroyedBlock);
        NBTTagList nbtTagListEnableItems = new NBTTagList();
        enableItems.forEach(itemsStr -> nbtTagListEnableItems.appendTag(new NBTTagString(itemsStr)));
        nbt.setTag(NBT_STATUS_ENABLE_ITEMS, nbtTagListEnableItems);
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
        face = EnumFacing.VALUES[nbt.getByte(NBT_CLICK_FACE) & 0xFF];
        digUnder = nbt.getBoolean(NBT_STATUS_DIG_UNDER);
        treeMode = nbt.getBoolean(NBT_STATUS_TREE_MODE);
        privateRegisterMode = nbt.getBoolean(NBT_STATUS_PRIVATE_MODE);
        maxDestroyedBlock = nbt.getInteger(NBT_STATUS_MAX_DESTORY_BLOCK);
        enableItems = Sets.newHashSet();
        NBTTagList nbtTagListEnableItems = nbt.getTagList(NBT_STATUS_ENABLE_ITEMS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < nbtTagListEnableItems.tagCount();i++) {
            enableItems.add(nbtTagListEnableItems.getStringTagAt(i));
        }
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

    public EnumFacing getFace() {
        return face;
    }

    public void setFace(EnumFacing face) {
        this.face = face;
    }

    public boolean isDigUnder() {
        return digUnder;
    }

    public void setDigUnder(boolean digUnder) {
        this.digUnder = digUnder;
    }

    public boolean isTreeMode() {
        return treeMode;
    }

    public void setTreeMode(boolean treeMode) {
        this.treeMode = treeMode;
    }

    public boolean isPrivateRegisterMode() {
        return privateRegisterMode;
    }

    public void setPrivateRegisterMode(boolean privateRegisterMode) {
        this.privateRegisterMode = privateRegisterMode;
    }

    @Override
    public int getMaxDestroyedBlock() {
        return this.maxDestroyedBlock;
    }

    @Override
    public void setMaxDestroyedBlock(int maxDestroyedBlock) {
        this.maxDestroyedBlock = maxDestroyedBlock;
    }

    @Override
    public Set<String> getEnableItems() {
        return this.enableItems;
    }

    @Override
    public void setEnableItems(Set<String> enableItems) {
        this.enableItems = enableItems;
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
    /** デフォルト設定 */
    public static final ICDPlayerStatusHandler DEFAULT_PLAYER_STATUS = new ICDPlayerStatusHandler() {
        @Override
        public EnumFacing getFace() {
            return EnumFacing.DOWN;
        }

        @Override
        public void setFace(EnumFacing face) {}

        @Override
        public boolean isDigUnder() {
            return false;
        }

        @Override
        public void setDigUnder(boolean digUnder) {}

        @Override
        public boolean isTreeMode() {
            return false;
        }

        @Override
        public void setTreeMode(boolean treeMode) {}

        @Override
        public boolean isPrivateRegisterMode() {
            return false;
        }

        @Override
        public void setPrivateRegisterMode(boolean privateRegisterMode) {}

        @Override
        public int getMaxDestroyedBlock() {
            return 5;
        }

        @Override
        public void setMaxDestroyedBlock(int maxDestroyedBlock) {}

        @Override
        public Set<String> getEnableItems() {
            return Sets.newHashSet(
                    "minecraft:diamond_axe", "minecraft:golden_axe", "minecraft:iron_axe", "minecraft:stone_axe", "minecraft:wooden_axe",
                    "minecraft:diamond_shovel", "minecraft:golden_shovel", "minecraft:iron_shovel", "minecraft:stone_shovel", "minecraft:wooden_shovel",
                    "minecraft:diamond_pickaxe", "minecraft:golden_pickaxe", "minecraft:iron_pickaxe", "minecraft:stone_pickaxe", "minecraft:wooden_pickaxe");
        }

        @Override
        public void setEnableItems(Set<String> enableItems) {}

        @Override
        public Set<String> getEnableBlocks() {
            return Sets.newHashSet(Blocks.OBSIDIAN.getRegistryName().toString(), "glowstone", "ore");
        }

        @Override
        public void setEnableBlocks(Set<String> enableBlocks) {}

        @Override
        public Set<String> getEnableLogBlocks() {
            return Sets.newHashSet("logWood", "treeLeaves");
        }

        @Override
        public void setEnableLogBlocks(Set<String> enableLogBlocks) {}
    };
}
