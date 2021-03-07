package ak.chaindestruction.capability;

import ak.chaindestruction.ConfigUtils;
import com.google.common.collect.Sets;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * 連鎖破壊のプレイヤー別状態保存クラス Created by A.K. on 2015/09/26.
 */
public class CDPlayerStatus implements ICDPlayerStatusHandler,
    ICapabilitySerializable<CompoundNBT> {

  public static final String NBT_STATUS_DIG_UNDER = "cd:digUnder";
  public static final String NBT_CLICK_FACE = "cd:clickFace";
  public static final String NBT_STATUS_TREE_MODE = "cd:treeMode";
  public static final String NBT_STATUS_PRIVATE_MODE = "cd:privateMode";
  public static final String NBT_STATUS_MAX_DESTROY_BLOCK = "cd:maxDestroyedBlock";
  public static final String NBT_STATUS_ENABLE_ITEMS = "cd:enableItems";
  public static final String NBT_STATUS_ENABLE_BLOCKS = "cd:enableBlocks";
  public static final String NBT_STATUS_ENABLE_LOG_BLOCKS = "cd:enableLogBlocks";
  /**
   * デフォルト設定
   */
  public static final ICDPlayerStatusHandler DEFAULT_PLAYER_STATUS = new ICDPlayerStatusHandler() {
    @Override
    public Direction getFace() {
      return Direction.DOWN;
    }

    @Override
    public void setFace(Direction face) {
    }

    @Override
    public boolean isDigUnder() {
      return false;
    }

    @Override
    public void setDigUnder(boolean digUnder) {
    }

    @Override
    public boolean isTreeMode() {
      return false;
    }

    @Override
    public void setTreeMode(boolean treeMode) {
    }

    @Override
    public boolean isPrivateRegisterMode() {
      return false;
    }

    @Override
    public void setPrivateRegisterMode(boolean privateRegisterMode) {
    }

    @Override
    public int getMaxDestroyedBlock() {
      return 5;
    }

    @Override
    public void setMaxDestroyedBlock(int maxDestroyedBlock) {
    }

    @Override
    public Set<String> getEnableItems() {
      return Sets.newHashSet(
          "minecraft:diamond_axe", "minecraft:golden_axe", "minecraft:iron_axe",
          "minecraft:stone_axe", "minecraft:wooden_axe",
          "minecraft:diamond_shovel", "minecraft:golden_shovel", "minecraft:iron_shovel",
          "minecraft:stone_shovel", "minecraft:wooden_shovel",
          "minecraft:diamond_pickaxe", "minecraft:golden_pickaxe", "minecraft:iron_pickaxe",
          "minecraft:stone_pickaxe", "minecraft:wooden_pickaxe");
    }

    @Override
    public void setEnableItems(Set<String> enableItems) {
    }

    @Override
    public Set<String> getEnableBlocks() {
      return Sets.newHashSet(Blocks.OBSIDIAN.getRegistryName().toString(), "glowstone", "forge:ores");
    }

    @Override
    public void setEnableBlocks(Set<String> enableBlocks) {
    }

    @Override
    public Set<String> getEnableLogBlocks() {
      return Sets.newHashSet("minecraft:logs", "minecraft:leaves");
    }

    @Override
    public void setEnableLogBlocks(Set<String> enableLogBlocks) {
    }

    @Override
    public BlockPos getMinPos(Entity entity, BlockPos targetPos) {
      return null;
    }

    @Override
    public BlockPos getMaxPos(BlockPos targetPos) {
      return null;
    }
  };
  private Direction face = Direction.DOWN;
  private Set<String> enableItems = Sets.newHashSet(
      "minecraft:diamond_axe", "minecraft:golden_axe", "minecraft:iron_axe", "minecraft:stone_axe",
      "minecraft:wooden_axe",
      "minecraft:diamond_shovel", "minecraft:golden_shovel", "minecraft:iron_shovel",
      "minecraft:stone_shovel", "minecraft:wooden_shovel",
      "minecraft:diamond_pickaxe", "minecraft:golden_pickaxe", "minecraft:iron_pickaxe",
      "minecraft:stone_pickaxe", "minecraft:wooden_pickaxe");
  private Set<String> enableBlocks = Sets
      .newHashSet(Blocks.OBSIDIAN.getRegistryName().toString(), "glowstone", "forge:ores");
  private Set<String> enableLogBlocks = Sets.newHashSet("minecraft:logs", "minecraft:leaves");
  private boolean digUnder = false;
  private boolean treeMode = false;
  private boolean privateRegisterMode = false;

  private int maxDestroyedBlock = 5;

  public static LazyOptional<ICDPlayerStatusHandler> get(PlayerEntity player) {
    return player
        .getCapability(CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
  }

  @Override
  @Nonnull
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
      @Nullable Direction facing) {
    return capability == CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER
        ? CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER
        .orEmpty(capability, LazyOptional.of(() -> this)) : LazyOptional.empty();
  }

  @Override
  public CompoundNBT serializeNBT() {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putByte(NBT_CLICK_FACE, (byte) face.getIndex());
    nbt.putBoolean(NBT_STATUS_DIG_UNDER, digUnder);
    nbt.putBoolean(NBT_STATUS_TREE_MODE, treeMode);
    nbt.putBoolean(NBT_STATUS_PRIVATE_MODE, privateRegisterMode);
    nbt.putInt(NBT_STATUS_MAX_DESTROY_BLOCK, maxDestroyedBlock);
    ListNBT ListNBTEnableItems = new ListNBT();
    enableItems.forEach(itemsStr -> ListNBTEnableItems.add(StringNBT.valueOf(itemsStr)));
    nbt.put(NBT_STATUS_ENABLE_ITEMS, ListNBTEnableItems);
    ListNBT ListNBTEnableBlocks = new ListNBT();
    enableBlocks.forEach(blockStr -> ListNBTEnableBlocks.add(StringNBT.valueOf(blockStr)));
    nbt.put(NBT_STATUS_ENABLE_BLOCKS, ListNBTEnableBlocks);
    ListNBT ListNBTEnableLogBlocks = new ListNBT();
    enableLogBlocks.forEach(blockStr -> ListNBTEnableLogBlocks.add(StringNBT.valueOf(blockStr)));
    nbt.put(NBT_STATUS_ENABLE_LOG_BLOCKS, ListNBTEnableLogBlocks);
    return nbt;
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {
    face = Direction.values()[nbt.getByte(NBT_CLICK_FACE) & 0xFF];
    digUnder = nbt.getBoolean(NBT_STATUS_DIG_UNDER);
    treeMode = nbt.getBoolean(NBT_STATUS_TREE_MODE);
    privateRegisterMode = nbt.getBoolean(NBT_STATUS_PRIVATE_MODE);
    maxDestroyedBlock = nbt.getInt(NBT_STATUS_MAX_DESTROY_BLOCK);
    enableItems = Sets.newHashSet();
    ListNBT ListNBTEnableItems = nbt
        .getList(NBT_STATUS_ENABLE_ITEMS, Constants.NBT.TAG_STRING);
    for (int i = 0; i < ListNBTEnableItems.size(); i++) {
      enableItems.add(ListNBTEnableItems.getString(i));
    }
    enableBlocks = Sets.newHashSet();
    ListNBT ListNBTEnableBlocks = nbt
        .getList(NBT_STATUS_ENABLE_BLOCKS, Constants.NBT.TAG_STRING);
    for (int i = 0; i < ListNBTEnableBlocks.size(); i++) {
      enableBlocks.add(ListNBTEnableBlocks.getString(i));
    }
    enableLogBlocks = Sets.newHashSet();
    ListNBT ListNBTEnableLogBlocks = nbt
        .getList(NBT_STATUS_ENABLE_LOG_BLOCKS, Constants.NBT.TAG_STRING);
    for (int i = 0; i < ListNBTEnableLogBlocks.size(); i++) {
      enableLogBlocks.add(ListNBTEnableLogBlocks.getString(i));
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
    return treeMode;
  }

  @Override
  public void setTreeMode(boolean treeMode) {
    this.treeMode = treeMode;
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
      y = Math.max(ak.akapi.Constants.MIN_Y, targetPos.getY() - maxDestroyedBlock);
    } else if (Direction.UP != getFace()) {
      y = Math.max(ak.akapi.Constants.MIN_Y, MathHelper.floor(entity.getPositionVec().y));
    } else if (maxDestroyedBlock > 0) {
      y = Math.max(ak.akapi.Constants.MIN_Y, MathHelper.floor(entity.getPositionVec().y) - 1);
    }
    return new BlockPos(
        targetPos.getX() - maxDestroyedBlock,
        y,
        targetPos.getZ() - maxDestroyedBlock);
  }

  @Override
  public BlockPos getMaxPos(BlockPos targetPos) {
    int maxDestroyedBlock = getMaxDestroyedBlock();
    int y =
        (isTreeMode()) ? ConfigUtils.COMMON.maxYforTreeMode : targetPos.getY() + maxDestroyedBlock;
    return new BlockPos(
        targetPos.getX() + maxDestroyedBlock,
        y,
        targetPos.getZ() + maxDestroyedBlock);
  }
}
