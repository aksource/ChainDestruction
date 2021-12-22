package ak.mcmod.chaindestruction.capability;

import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.chaindestruction.util.ConfigUtils;
import ak.mcmod.chaindestruction.util.ModeType;
import com.google.common.collect.Sets;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Set;

import static ak.mcmod.chaindestruction.api.Constants.*;

/**
 * 連鎖破壊のプレイヤー別状態保存クラス Created by A.K. on 2015/09/26.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdditionalPlayerStatus implements IAdditionalPlayerStatus {

  private Direction face = Direction.DOWN;
  private Set<String> enableItems = Sets.newHashSet("minecraft:netherite_axe", "minecraft:diamond_axe", "minecraft:golden_axe", "minecraft:iron_axe", "minecraft:stone_axe", "minecraft:wooden_axe", "minecraft:netherite_shovel", "minecraft:diamond_shovel", "minecraft:golden_shovel", "minecraft:iron_shovel", "minecraft:stone_shovel", "minecraft:wooden_shovel", "minecraft:netherite_pickaxe", "minecraft:diamond_pickaxe", "minecraft:golden_pickaxe", "minecraft:iron_pickaxe", "minecraft:stone_pickaxe", "minecraft:wooden_pickaxe");
  private Set<String> enableBlocks = Sets.newHashSet(Objects.requireNonNull(Blocks.OBSIDIAN.getRegistryName()).toString(), "glowstone", "forge:ores/coal", "forge:ores/diamond", "forge:ores/emerald", "forge:ores/gold", "forge:ores/iron", "forge:ores/lapis", "forge:ores/netherite_scrap", "forge:ores/quartz", "forge:ores/redstone");
  private Set<String> enableLogBlocks = Sets.newHashSet("minecraft:logs", "minecraft:leaves");
  private Set<String> forbiddenTags = Sets.newHashSet("mineable", "guarded_by", "hoglin_repellents", "needs", "replaceable", "forge:stone", "base_stone");
  private boolean digUnder = false;
  private boolean treeMode = false;
  private boolean privateRegisterMode = false;
  private ModeType modeType = ModeType.NORMAL;

  private int maxDestroyedBlock = 5;

  @Override
  public CompoundTag serializeNBT() {
    var nbt = new CompoundTag();
    nbt.putByte(NBT_CLICK_FACE, (byte) face.get3DDataValue());
    nbt.putBoolean(NBT_STATUS_DIG_UNDER, digUnder);
    nbt.putBoolean(NBT_STATUS_TREE_MODE, treeMode);
    nbt.putString(NBT_STATUS_MODE_TYPE, modeType.name());
    nbt.putBoolean(NBT_STATUS_PRIVATE_MODE, privateRegisterMode);
    nbt.putInt(NBT_STATUS_MAX_DESTROY_BLOCK, maxDestroyedBlock);
    var listNBTEnableItems = new ListTag();
    enableItems.forEach(itemsStr -> listNBTEnableItems.add(StringTag.valueOf(itemsStr)));
    nbt.put(NBT_STATUS_ENABLE_ITEMS, listNBTEnableItems);
    var listNBTEnableBlocks = new ListTag();
    enableBlocks.forEach(blockStr -> listNBTEnableBlocks.add(StringTag.valueOf(blockStr)));
    nbt.put(NBT_STATUS_ENABLE_BLOCKS, listNBTEnableBlocks);
    var listNBTEnableLogBlocks = new ListTag();
    enableLogBlocks.forEach(blockStr -> listNBTEnableLogBlocks.add(StringTag.valueOf(blockStr)));
    nbt.put(NBT_STATUS_ENABLE_LOG_BLOCKS, listNBTEnableLogBlocks);
    var listNBTForbiddenTags = new ListTag();
    forbiddenTags.forEach(tag -> listNBTForbiddenTags.add(StringTag.valueOf(tag)));
    nbt.put(NBT_STATUS_FORBIDDEN_TAGS, listNBTForbiddenTags);
    return nbt;
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    face = Direction.values()[nbt.getByte(NBT_CLICK_FACE) & 0xFF];
    digUnder = nbt.getBoolean(NBT_STATUS_DIG_UNDER);
    treeMode = nbt.getBoolean(NBT_STATUS_TREE_MODE);
    modeType = StringUtils.isNotEmpty(nbt.getString(NBT_STATUS_MODE_TYPE)) ? ModeType.valueOf(nbt.getString(NBT_STATUS_MODE_TYPE)) : ModeType.NORMAL;
    privateRegisterMode = nbt.getBoolean(NBT_STATUS_PRIVATE_MODE);
    maxDestroyedBlock = nbt.getInt(NBT_STATUS_MAX_DESTROY_BLOCK);
    enableItems = Sets.newHashSet();
    var listNBTEnableItems = nbt.getList(NBT_STATUS_ENABLE_ITEMS, Tag.TAG_STRING);
    for (var i = 0; i < listNBTEnableItems.size(); i++) {
      enableItems.add(listNBTEnableItems.getString(i));
    }
    enableBlocks = Sets.newHashSet();
    var listNBTEnableBlocks = nbt.getList(NBT_STATUS_ENABLE_BLOCKS, Tag.TAG_STRING);
    for (var i = 0; i < listNBTEnableBlocks.size(); i++) {
      enableBlocks.add(listNBTEnableBlocks.getString(i));
    }
    enableLogBlocks = Sets.newHashSet();
    var listNBTEnableLogBlocks = nbt.getList(NBT_STATUS_ENABLE_LOG_BLOCKS, Tag.TAG_STRING);
    for (var i = 0; i < listNBTEnableLogBlocks.size(); i++) {
      enableLogBlocks.add(listNBTEnableLogBlocks.getString(i));
    }
    forbiddenTags = Sets.newHashSet();
    var listNBTForbiddenTags = nbt.getList(NBT_STATUS_FORBIDDEN_TAGS, Tag.TAG_STRING);
    for (var i = 0; i < listNBTForbiddenTags.size(); i++) {
      forbiddenTags.add(listNBTForbiddenTags.getString(i));
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
    if (isDigUnder() && (ModeType.NORMAL == getModeType() || ModeType.TREE == getModeType())) {
      y = Math.max(MIN_Y, targetPos.getY() - maxDestroyedBlock);
    } else if (Direction.UP != getFace()) {
      y = Math.max(MIN_Y, Mth.floor(entity.position().y));
    } else if (maxDestroyedBlock > 0) {
      y = Math.max(MIN_Y, Mth.floor(entity.position().y) - 1);
    }
    int x = targetPos.getX() - maxDestroyedBlock;
    int z = targetPos.getZ() - maxDestroyedBlock;
    if (ModeType.BRANCH_MINING == getModeType()) {
      switch (getFace()) {
        case UP -> {
          x = targetPos.getX();
          y = targetPos.getY() - maxDestroyedBlock;
          z = targetPos.getZ();
        }
        case DOWN -> {
          x = targetPos.getX();
          y = targetPos.getY();
          z = targetPos.getZ();
        }
        case NORTH, WEST -> {
          x = targetPos.getX();
          z = targetPos.getZ();
        }
        case SOUTH -> x = targetPos.getX();
        case EAST -> z = targetPos.getZ();
      }
    } else if (ModeType.WALL_MINING == getModeType()) {
      switch (getFace()) {
        case UP, DOWN -> y = targetPos.getY();
        case NORTH, SOUTH -> z = targetPos.getZ();
        case WEST, EAST -> x = targetPos.getX();
      }
    }
    return new BlockPos(x, y, z);
  }

  @Override
  public BlockPos getMaxPos(BlockPos targetPos) {
    int maxDestroyedBlock = getMaxDestroyedBlock();
    int y = (isTreeMode()) ? ConfigUtils.COMMON.maxYforTreeMode : targetPos.getY() + maxDestroyedBlock;
    int x = targetPos.getX() + maxDestroyedBlock;
    int z = targetPos.getZ() + maxDestroyedBlock;
    if (ModeType.BRANCH_MINING == getModeType()) {
      switch (getFace()) {
        case UP, SOUTH, EAST -> {
          x = targetPos.getX();
          y = targetPos.getY();
          z = targetPos.getZ();
        }
        case DOWN -> {
          x = targetPos.getX();
          y = targetPos.getY() + maxDestroyedBlock;
          z = targetPos.getZ();
        }
        case NORTH -> {
          x = targetPos.getX();
          y = targetPos.getY();
        }
        case WEST -> {
          y = targetPos.getY();
          z = targetPos.getZ();
        }
      }
    } else if (ModeType.WALL_MINING == getModeType()) {
      switch (getFace()) {
        case UP, DOWN -> y = targetPos.getY();
        case NORTH, SOUTH -> z = targetPos.getZ();
        case WEST, EAST -> x = targetPos.getX();
      }
    }
    return new BlockPos(x, y, z);
  }

  @Override
  public Set<String> getForbiddenTags() {
    return this.forbiddenTags;
  }

  @Override
  public void setForbiddenTags(Set<String> forbiddenTags) {
    this.forbiddenTags = forbiddenTags;
  }
}
