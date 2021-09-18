package ak.mcmod.chaindestruction.capability;

import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.chaindestruction.util.ConfigUtils;
import ak.mcmod.chaindestruction.util.ModeType;
import com.google.common.collect.Sets;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Set;

import static ak.mcmod.chaindestruction.api.Constants.*;
import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;
import static net.minecraftforge.common.util.Constants.NBT.TAG_STRING;

/**
 * 連鎖破壊のプレイヤー別状態保存クラス Created by A.K. on 2015/09/26.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CDPlayerStatus implements ICDPlayerStatusHandler,
        ICapabilitySerializable<CompoundNBT>, Capability.IStorage<ICDPlayerStatusHandler> {

  private Direction face = Direction.DOWN;
  private Set<String> enableItems = Sets.newHashSet(
          "minecraft:diamond_axe", "minecraft:golden_axe", "minecraft:iron_axe", "minecraft:stone_axe",
          "minecraft:wooden_axe",
          "minecraft:diamond_shovel", "minecraft:golden_shovel", "minecraft:iron_shovel",
          "minecraft:stone_shovel", "minecraft:wooden_shovel",
          "minecraft:diamond_pickaxe", "minecraft:golden_pickaxe", "minecraft:iron_pickaxe",
          "minecraft:stone_pickaxe", "minecraft:wooden_pickaxe");
  private Set<String> enableBlocks = Sets
          .newHashSet(Objects.requireNonNull(Blocks.OBSIDIAN.getRegistryName()).toString(), "glowstone", "forge:ores");
  private Set<String> enableLogBlocks = Sets.newHashSet("minecraft:logs", "minecraft:leaves");
  private boolean digUnder = false;
  private boolean treeMode = false;
  private boolean privateRegisterMode = false;
  private ModeType modeType = ModeType.NORMAL;

  private int maxDestroyedBlock = 5;

  public static LazyOptional<ICDPlayerStatusHandler> get(PlayerEntity player) {
    return player
            .getCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
  }

  @Override
  @Nonnull
  public <T> LazyOptional<T> getCapability(Capability<T> capability,
                                           @Nullable Direction facing) {
    return capability == CAPABILITY_CHAIN_DESTRUCTION_PLAYER
            ? CAPABILITY_CHAIN_DESTRUCTION_PLAYER
            .orEmpty(capability, LazyOptional.of(() -> this)) : LazyOptional.empty();
  }

  @Override
  public CompoundNBT serializeNBT() {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putByte(NBT_CLICK_FACE, (byte) face.get3DDataValue());
    nbt.putBoolean(NBT_STATUS_DIG_UNDER, digUnder);
    nbt.putBoolean(NBT_STATUS_TREE_MODE, treeMode);
    nbt.putString(NBT_STATUS_MODE_TYPE, modeType.name());
    nbt.putBoolean(NBT_STATUS_PRIVATE_MODE, privateRegisterMode);
    nbt.putInt(NBT_STATUS_MAX_DESTROY_BLOCK, maxDestroyedBlock);
    ListNBT listNBTEnableItems = new ListNBT();
    enableItems.forEach(itemsStr -> listNBTEnableItems.add(StringNBT.valueOf(itemsStr)));
    nbt.put(NBT_STATUS_ENABLE_ITEMS, listNBTEnableItems);
    ListNBT listNBTEnableBlocks = new ListNBT();
    enableBlocks.forEach(blockStr -> listNBTEnableBlocks.add(StringNBT.valueOf(blockStr)));
    nbt.put(NBT_STATUS_ENABLE_BLOCKS, listNBTEnableBlocks);
    ListNBT listNBTEnableLogBlocks = new ListNBT();
    enableLogBlocks.forEach(blockStr -> listNBTEnableLogBlocks.add(StringNBT.valueOf(blockStr)));
    nbt.put(NBT_STATUS_ENABLE_LOG_BLOCKS, listNBTEnableLogBlocks);
    return nbt;
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {
    face = Direction.values()[nbt.getByte(NBT_CLICK_FACE) & 0xFF];
    digUnder = nbt.getBoolean(NBT_STATUS_DIG_UNDER);
    treeMode = nbt.getBoolean(NBT_STATUS_TREE_MODE);
    modeType = StringUtils.isNotEmpty(nbt.getString(NBT_STATUS_MODE_TYPE)) ? ModeType.valueOf(nbt.getString(NBT_STATUS_MODE_TYPE)) : ModeType.NORMAL;
    privateRegisterMode = nbt.getBoolean(NBT_STATUS_PRIVATE_MODE);
    maxDestroyedBlock = nbt.getInt(NBT_STATUS_MAX_DESTROY_BLOCK);
    enableItems = Sets.newHashSet();
    ListNBT listNBTEnableItems = nbt
            .getList(NBT_STATUS_ENABLE_ITEMS, TAG_STRING);
    for (int i = 0; i < listNBTEnableItems.size(); i++) {
      enableItems.add(listNBTEnableItems.getString(i));
    }
    enableBlocks = Sets.newHashSet();
    ListNBT listNBTEnableBlocks = nbt
            .getList(NBT_STATUS_ENABLE_BLOCKS, TAG_STRING);
    for (int i = 0; i < listNBTEnableBlocks.size(); i++) {
      enableBlocks.add(listNBTEnableBlocks.getString(i));
    }
    enableLogBlocks = Sets.newHashSet();
    ListNBT listNBTEnableLogBlocks = nbt
            .getList(NBT_STATUS_ENABLE_LOG_BLOCKS, TAG_STRING);
    for (int i = 0; i < listNBTEnableLogBlocks.size(); i++) {
      enableLogBlocks.add(listNBTEnableLogBlocks.getString(i));
    }
  }

  @Override
  public Direction getFace() {
    return face;
  }

  @Override
  public void setFace(Direction face) {
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

  @Deprecated
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
      y = Math.max(MIN_Y, targetPos.getY() - maxDestroyedBlock);
    } else if (Direction.UP != getFace()) {
      y = Math.max(MIN_Y, MathHelper.floor(entity.position().y));
    } else if (maxDestroyedBlock > 0) {
      y = Math.max(MIN_Y, MathHelper.floor(entity.position().y) - 1);
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
    int y =
            (isTreeMode()) ? ConfigUtils.COMMON.maxYforTreeMode : targetPos.getY() + maxDestroyedBlock;
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
  public INBT writeNBT(Capability<ICDPlayerStatusHandler> capability, ICDPlayerStatusHandler instance, Direction side) {
    if (capability == CAPABILITY_CHAIN_DESTRUCTION_PLAYER) {
      return serializeNBT();
    }
    return null;
  }

  @Override
  public void readNBT(Capability<ICDPlayerStatusHandler> capability, ICDPlayerStatusHandler instance, Direction side, INBT nbt) {
    if (capability == CAPABILITY_CHAIN_DESTRUCTION_PLAYER) {
      deserializeNBT((CompoundNBT) nbt);
    }
  }
}
