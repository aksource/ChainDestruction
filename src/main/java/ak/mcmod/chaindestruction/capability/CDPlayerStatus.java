package ak.mcmod.chaindestruction.capability;

import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.chaindestruction.ChainDestruction;
import ak.mcmod.chaindestruction.util.ModeType;
import com.google.common.collect.Sets;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Set;

import static ak.mcmod.chaindestruction.api.Constants.*;
import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;
import static net.minecraftforge.common.util.Constants.NBT.TAG_STRING;

/**
 * 連鎖破壊のプレイヤー別状態保存クラス
 * Created by A.K. on 2015/09/26.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CDPlayerStatus implements ICDPlayerStatusHandler, ICapabilitySerializable<NBTTagCompound>, Capability.IStorage<ICDPlayerStatusHandler> {
    public EnumFacing face = EnumFacing.DOWN;
    public Set<String> enableItems = Sets.newHashSet(
            "minecraft:diamond_axe", "minecraft:golden_axe", "minecraft:iron_axe", "minecraft:stone_axe", "minecraft:wooden_axe",
            "minecraft:diamond_shovel", "minecraft:golden_shovel", "minecraft:iron_shovel", "minecraft:stone_shovel", "minecraft:wooden_shovel",
            "minecraft:diamond_pickaxe", "minecraft:golden_pickaxe", "minecraft:iron_pickaxe", "minecraft:stone_pickaxe", "minecraft:wooden_pickaxe");
    public Set<String> enableBlocks = Sets.newHashSet(Objects.requireNonNull(Blocks.OBSIDIAN.getRegistryName()).toString(), "glowstone", "ore");
    public Set<String> enableLogBlocks = Sets.newHashSet("logWood", "treeLeaves");
    private boolean digUnder = false;
    private boolean treeMode = false;
    private boolean privateRegisterMode = false;
    private ModeType modeType = ModeType.NORMAL;

    private int maxDestroyedBlock = 5;

    @Nullable
    public static ICDPlayerStatusHandler get(EntityPlayer player) {
        return player.getCapability(CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER ? CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte(NBT_CLICK_FACE, (byte) face.getIndex());
        nbt.setBoolean(NBT_STATUS_DIG_UNDER, digUnder);
        nbt.setBoolean(NBT_STATUS_TREE_MODE, treeMode);
        nbt.setString(NBT_STATUS_MODE_TYPE, modeType.name());
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
        modeType = StringUtils.isNotEmpty(nbt.getString(NBT_STATUS_MODE_TYPE)) ? ModeType.valueOf(nbt.getString(NBT_STATUS_MODE_TYPE)) : ModeType.NORMAL;
        privateRegisterMode = nbt.getBoolean(NBT_STATUS_PRIVATE_MODE);
        maxDestroyedBlock = nbt.getInteger(NBT_STATUS_MAX_DESTORY_BLOCK);
        enableItems = Sets.newHashSet();
        NBTTagList nbtTagListEnableItems = nbt.getTagList(NBT_STATUS_ENABLE_ITEMS, TAG_STRING);
        for (int i = 0; i < nbtTagListEnableItems.tagCount(); i++) {
            enableItems.add(nbtTagListEnableItems.getStringTagAt(i));
        }
        enableBlocks = Sets.newHashSet();
        NBTTagList nbtTagListEnableBlocks = nbt.getTagList(NBT_STATUS_ENABLE_BLOCKS, TAG_STRING);
        for (int i = 0; i < nbtTagListEnableBlocks.tagCount(); i++) {
            enableBlocks.add(nbtTagListEnableBlocks.getStringTagAt(i));
        }
        enableLogBlocks = Sets.newHashSet();
        NBTTagList nbtTagListEnableLogBlocks = nbt.getTagList(NBT_STATUS_ENABLE_LOG_BLOCKS, TAG_STRING);
        for (int i = 0; i < nbtTagListEnableLogBlocks.tagCount(); i++) {
            enableLogBlocks.add(nbtTagListEnableLogBlocks.getStringTagAt(i));
        }
    }

    @Override
    public EnumFacing getFace() {
        return face;
    }

    @Override
    public void setFace(EnumFacing face) {
        this.face = face;
    }

    @Override
    public boolean isDigUnder() {
        return digUnder;
    }

    @Override
    public void setDigUnder(boolean digUnder) {
        this.digUnder = digUnder;
    }

    @Override
    public boolean isTreeMode() {
        return modeType == ModeType.TREE;
    }

    @Override
    public void setTreeMode(boolean treeMode) {
        this.treeMode = treeMode;
    }

    @Override
    public ModeType getModeType() {
        return this.modeType;
    }

    @Override
    public void setModeType(ModeType modeType) {
        this.modeType = modeType;
    }

    @Override
    public boolean isPrivateRegisterMode() {
        return privateRegisterMode;
    }

    @Override
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

    @Override
    public BlockPos getMinPos(Entity entity, BlockPos targetPos) {
        int y = targetPos.getY();
        int maxDestroyedBlock = getMaxDestroyedBlock();
        if (isDigUnder()) {
            y = Math.max(ak.mcmod.chaindestruction.api.Constants.MIN_Y, targetPos.getY() - maxDestroyedBlock);
        } else if (EnumFacing.UP != getFace()) {
            y = Math.max(ak.mcmod.chaindestruction.api.Constants.MIN_Y, MathHelper.floor(entity.posY));
        } else if (maxDestroyedBlock > 0) {
            y = Math.max(ak.mcmod.chaindestruction.api.Constants.MIN_Y, MathHelper.floor(entity.posY) - 1);
        }
        int x = targetPos.getX() - maxDestroyedBlock;
        int z = targetPos.getZ() - maxDestroyedBlock;
        if (ModeType.BRANCH_MINING == getModeType()) {
            switch (getFace()) {
                case UP:
                    x = targetPos.getX();
                    y = targetPos.getY() - maxDestroyedBlock;
                    z = targetPos.getZ();
                    break;
                case DOWN:
                    x = targetPos.getX();
                    y = targetPos.getY();
                    z = targetPos.getZ();
                    break;
                case NORTH:
                case WEST:
                    x = targetPos.getX();
                    z = targetPos.getZ();
                    break;
                case SOUTH:
                    x = targetPos.getX();
                    break;
                case EAST:
                    z = targetPos.getZ();
                    break;
            }
        } else if (ModeType.WALL_MINING == getModeType()) {
            switch (getFace()) {
                case UP:
                case DOWN:
                    y = targetPos.getY();
                    break;
                case NORTH:
                case SOUTH:
                    z = targetPos.getZ();
                    break;
                case WEST:
                case EAST:
                    x = targetPos.getX();
                    break;
            }
        }
        return new BlockPos(x, y, z);
    }

    @Override
    public BlockPos getMaxPos(BlockPos targetPos) {
        int maxDestroyedBlock = getMaxDestroyedBlock();
        int y = (isTreeMode()) ? ChainDestruction.maxYforTreeMode : targetPos.getY() + maxDestroyedBlock;
        int x = targetPos.getX() + maxDestroyedBlock;
        int z = targetPos.getZ() + maxDestroyedBlock;
        if (ModeType.BRANCH_MINING == getModeType()) {
            switch (getFace()) {
                case UP:
                case SOUTH:
                case EAST:
                    x = targetPos.getX();
                    y = targetPos.getY();
                    z = targetPos.getZ();
                    break;
                case DOWN:
                    x = targetPos.getX();
                    y = targetPos.getY() + maxDestroyedBlock;
                    z = targetPos.getZ();
                    break;
                case NORTH:
                    x = targetPos.getX();
                    y = targetPos.getY();
                    break;
                case WEST:
                    y = targetPos.getY();
                    z = targetPos.getZ();
                    break;
            }
        } else if (ModeType.WALL_MINING == getModeType()) {
            switch (getFace()) {
                case UP:
                case DOWN:
                    y = targetPos.getY();
                    break;
                case NORTH:
                case SOUTH:
                    z = targetPos.getZ();
                    break;
                case WEST:
                case EAST:
                    x = targetPos.getX();
                    break;
            }
        }
        return new BlockPos(x, y, z);
    }

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<ICDPlayerStatusHandler> capability, ICDPlayerStatusHandler instance, EnumFacing side) {
        if (capability == CAPABILITY_CHAIN_DESTRUCTION_PLAYER) {
            return serializeNBT();
        }
        return null;
    }

    @Override
    public void readNBT(Capability<ICDPlayerStatusHandler> capability, ICDPlayerStatusHandler instance, EnumFacing side, NBTBase nbt) {
        if (capability == CAPABILITY_CHAIN_DESTRUCTION_PLAYER) {
            deserializeNBT((NBTTagCompound) nbt);
        }
    }
}
