package ak.mcmod.chaindestruction.util;

import ak.mcmod.chaindestruction.network.MessageDigSound;
import ak.mcmod.chaindestruction.network.PacketHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkDirection;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

/**
 * ブロック破壊のタスククラス Created by A.K. on 15/01/13.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DigTask {

  private final Queue<BlockPos> queue = new ArrayDeque<>();
  private final Player digger;
  private final ItemStack heldItem;
  private int counter;

  public DigTask(Player player, ItemStack itemStack, Collection<BlockPos> blockPosSet) {
    this.digger = player;
    this.heldItem = itemStack;
    this.queue.addAll(blockPosSet);
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
    if (queue.isEmpty()) {
      return true;
    }
    var first = queue.poll();
    if (!(this.digger.getCommandSenderWorld() instanceof ServerLevel world)) {
      return true;
    }
    world.globalLevelEvent(2001, first, Block.getId(world.getBlockState(first)));
    PacketHandler.INSTANCE.sendTo(new MessageDigSound(first),
            ((ServerPlayer) digger).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    return ChainDestructionLogic.destroyBlockAtPosition(world, digger, first, heldItem);
  }

  public Player getDigger() {
    return digger;
  }
}
