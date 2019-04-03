package ak.chaindestruction;

import ak.chaindestruction.network.MessageDigSound;
import ak.chaindestruction.network.PacketHandler;
import java.util.LinkedHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

/**
 * ブロック破壊のタスククラス Created by A.K. on 15/01/13.
 */
public class DigTask {

  private LinkedHashSet<BlockPos> blockToDestroySet = new LinkedHashSet<>();
  private EntityPlayer digger;
  private ItemStack heldItem;
  private int counter;

  public DigTask(EntityPlayer player, ItemStack itemStack, LinkedHashSet<BlockPos> blockPosSet,
      BlockPos origin) {
    this.digger = player;
    this.heldItem = itemStack;
    this.blockToDestroySet.addAll(blockPosSet);
  }

  //return true : when all block destroyed or heldItem broken
  public boolean increaseCount() {
    counter++;
    if (counter >= ConfigUtils.COMMON.digTaskMaxCounter) {
      counter = 0;
      return destroyBlock();
    }
    return false;
  }

  public boolean destroyBlock() {
    if (blockToDestroySet.isEmpty()) {
      return true;
    }
    BlockPos first = blockToDestroySet.iterator().next();
    blockToDestroySet.remove(first);
    World world = this.digger.getEntityWorld();
    world.playBroadcastSound(2001, first, Block.getStateId(world.getBlockState(first)));
    PacketHandler.INSTANCE.sendTo(new MessageDigSound(first),
        ((EntityPlayerMP) digger).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    return InteractBlockHook.destroyBlockAtPosition(world, digger, first, heldItem);
  }

  public EntityPlayer getDigger() {
    return digger;
  }
}
