package ak.mcmod.chaindestruction.event;

import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.chaindestruction.capability.CDItemStackStatus;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.util.ChainDestructionLogic;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
//@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.DEDICATED_SERVER})
public class BlockEvents {
  @SubscribeEvent
  public static void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock event) {
    PlayerEntity player = event.getPlayer();
    CDPlayerStatus.get(player).ifPresent(status -> {
      Set<String> enableItems = status.getEnableItems();
      World world = player.getCommandSenderWorld();
      ItemStack itemStack = player.getMainHandItem();
      if (world.isClientSide || itemStack.isEmpty()) {
        return;
      }
      String uniqueName = StringUtils.getUniqueString(itemStack.getItem().getRegistryName());
      if (enableItems.contains(uniqueName)) {
        BlockState state = world.getBlockState(event.getPos());
        boolean canHarvestBlock = ForgeHooks.canHarvestBlock(state, player, world, event.getPos());
        if (ChainDestructionLogic.checkBlockValidate(player, state, itemStack, canHarvestBlock)) {
          status.setFace(event.getFace());
        }
      }
    });
  }

  /*偽装しているブロックへの対応含めこちらで処理したほうが良い*/
  @SubscribeEvent
  public static void blockBreakingEvent(final BlockEvent.BreakEvent event) {
    if (!(event.getPlayer() instanceof FakePlayer) && !event.getWorld().isClientSide()) {
      if (ChainDestructionLogic.isChainDestructionActionable(event.getWorld(), event.getPlayer(), event.getState(), event.getPos(),
              event.getPlayer().getMainHandItem())) {
        ChainDestructionLogic.setup(event.getState(), event.getPlayer(), (ServerWorld) event.getWorld(), event.getPos());
      }
    }
  }

  /**
   * ブロックを右クリックした際に呼ばれるイベント
   *
   * @param event 右クリックイベント
   */
  @SubscribeEvent
  public static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
    PlayerEntity player = event.getPlayer();
    CDPlayerStatus.get(player).ifPresent(status -> {
      World world = player.getCommandSenderWorld();
      ItemStack itemStack = player.getMainHandItem();
      if (world.isClientSide || itemStack.isEmpty()) {
        return;
      }
      String uniqueName = StringUtils.getUniqueString(itemStack.getItem().getRegistryName());
      if (status.getEnableItems().contains(uniqueName)) {
        BlockState state = world.getBlockState(event.getPos());
        if (status.isPrivateRegisterMode()) {
          CDItemStackStatus.get(itemStack).ifPresent(itemStatus -> {
            Set<String> enableBlocks = status.isTreeMode() ? itemStatus.getEnableLogBlocks()
                    : itemStatus.getEnableBlocks();
            ChainDestructionLogic.addAndRemoveBlocks(enableBlocks, player, state);
          });
        } else {
          ChainDestructionLogic.addAndRemoveBlocks(
                  status.isTreeMode() ? status.getEnableLogBlocks() : status.getEnableBlocks(), player,
                  state);

        }
      }
    });
  }
}