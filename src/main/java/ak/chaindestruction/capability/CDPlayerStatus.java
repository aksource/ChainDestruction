package ak.chaindestruction.capability;

import ak.chaindestruction.ConfigUtils;
import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

/**
 * 連鎖破壊のプレイヤー別状態保存クラス Created by A.K. on 2015/09/26.
 */
public class CDPlayerStatus implements ICDPlayerStatusHandler,
    ICapabilitySerializable<NBTTagCompound> {

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
    public EnumFacing getFace() {
      return EnumFacing.DOWN;
    }

    @Override
    public void setFace(EnumFacing face) {
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
      return Sets.newHashSet(Blocks.OBSIDIAN.getRegistryName().toString(), "glowstone", "ore");
    }

    @Override
    public void setEnableBlocks(Set<String> enableBlocks) {
    }

    @Override
    public Set<String> getEnableLogBlocks() {
      return Sets.newHashSet("logWood", "treeLeaves");
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
  private EnumFacing face = EnumFacing.DOWN;
  private Set<String> enableItems = Sets.newHashSet(
      "minecraft:diamond_axe", "minecraft:golden_axe", "minecraft:iron_axe", "minecraft:stone_axe",
      "minecraft:wooden_axe",
      "minecraft:diamond_shovel", "minecraft:golden_shovel", "minecraft:iron_shovel",
      "minecraft:stone_shovel", "minecraft:wooden_shovel",
      "minecraft:diamond_pickaxe", "minecraft:golden_pickaxe", "minecraft:iron_pickaxe",
      "minecraft:stone_pickaxe", "minecraft:wooden_pickaxe");
  private Set<String> enableBlocks = Sets
      .newHashSet(Blocks.OBSIDIAN.getRegistryName().toString(), "glowstone", "ore");
  private Set<String> enableLogBlocks = Sets.newHashSet("logWood", "treeLeaves");
  private boolean digUnder = false;
  private boolean treeMode = false;
  private boolean privateRegisterMode = false;

  private int maxDestroyedBlock = 5;

  public static LazyOptional<ICDPlayerStatusHandler> get(EntityPlayer player) {
    return player
        .getCapability(CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
  }

  @Override
  @Nonnull
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
      @Nullable EnumFacing facing) {
    return capability == CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER
        ? CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER
        .orEmpty(capability, LazyOptional.of(() -> this)) : LazyOptional.empty();
  }

  @Override
  public NBTTagCompound serializeNBT() {
    NBTTagCompound nbt = new NBTTagCompound();
    nbt.putByte(NBT_CLICK_FACE, (byte) face.getIndex());
    nbt.putBoolean(NBT_STATUS_DIG_UNDER, digUnder);
    nbt.putBoolean(NBT_STATUS_TREE_MODE, treeMode);
    nbt.putBoolean(NBT_STATUS_PRIVATE_MODE, privateRegisterMode);
    nbt.putInt(NBT_STATUS_MAX_DESTROY_BLOCK, maxDestroyedBlock);
    NBTTagList nbtTagListEnableItems = new NBTTagList();
    enableItems.forEach(itemsStr -> nbtTagListEnableItems.add(new NBTTagString(itemsStr)));
    nbt.put(NBT_STATUS_ENABLE_ITEMS, nbtTagListEnableItems);
    NBTTagList nbtTagListEnableBlocks = new NBTTagList();
    enableBlocks.forEach(blockStr -> nbtTagListEnableBlocks.add(new NBTTagString(blockStr)));
    nbt.put(NBT_STATUS_ENABLE_BLOCKS, nbtTagListEnableBlocks);
    NBTTagList nbtTagListEnableLogBlocks = new NBTTagList();
    enableLogBlocks.forEach(blockStr -> nbtTagListEnableLogBlocks.add(new NBTTagString(blockStr)));
    nbt.put(NBT_STATUS_ENABLE_LOG_BLOCKS, nbtTagListEnableLogBlocks);
    return nbt;
  }

  @Override
  public void deserializeNBT(NBTTagCompound nbt) {
    face = EnumFacing.values()[nbt.getByte(NBT_CLICK_FACE) & 0xFF];
    digUnder = nbt.getBoolean(NBT_STATUS_DIG_UNDER);
    treeMode = nbt.getBoolean(NBT_STATUS_TREE_MODE);
    privateRegisterMode = nbt.getBoolean(NBT_STATUS_PRIVATE_MODE);
    maxDestroyedBlock = nbt.getInt(NBT_STATUS_MAX_DESTROY_BLOCK);
    enableItems = Sets.newHashSet();
    NBTTagList nbtTagListEnableItems = nbt
        .getList(NBT_STATUS_ENABLE_ITEMS, Constants.NBT.TAG_STRING);
    for (int i = 0; i < nbtTagListEnableItems.size(); i++) {
      enableItems.add(nbtTagListEnableItems.getString(i));
    }
    enableBlocks = Sets.newHashSet();
    NBTTagList nbtTagListEnableBlocks = nbt
        .getList(NBT_STATUS_ENABLE_BLOCKS, Constants.NBT.TAG_STRING);
    for (int i = 0; i < nbtTagListEnableBlocks.size(); i++) {
      enableBlocks.add(nbtTagListEnableBlocks.getString(i));
    }
    enableLogBlocks = Sets.newHashSet();
    NBTTagList nbtTagListEnableLogBlocks = nbt
        .getList(NBT_STATUS_ENABLE_LOG_BLOCKS, Constants.NBT.TAG_STRING);
    for (int i = 0; i < nbtTagListEnableLogBlocks.size(); i++) {
      enableLogBlocks.add(nbtTagListEnableLogBlocks.getString(i));
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
    } else if (EnumFacing.UP != getFace()) {
      y = Math.max(ak.akapi.Constants.MIN_Y, MathHelper.floor(entity.posY));
    } else if (maxDestroyedBlock > 0) {
      y = Math.max(ak.akapi.Constants.MIN_Y, MathHelper.floor(entity.posY) - 1);
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
