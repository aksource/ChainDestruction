package ak.mcmod.chaindestruction.event;

import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.chaindestruction.capability.CapabilityAdditionalItemStackStatus;
import ak.mcmod.chaindestruction.capability.CapabilityAdditionalPlayerStatus;
import ak.mcmod.chaindestruction.util.ChainDestructionLogic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
//@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.DEDICATED_SERVER})
public class BlockEvents {
  @SubscribeEvent
  public static void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock event) {
    var player = event.getPlayer();
    player.getCapability(CapabilityAdditionalPlayerStatus.CAPABILITY).ifPresent(status -> {
      var enableItems = status.getEnableItems();
      var world = player.getCommandSenderWorld();
      var itemStack = player.getMainHandItem();
      if (world.isClientSide || itemStack.isEmpty()) {
        return;
      }
      var uniqueName = StringUtils.getUniqueString(ForgeRegistries.ITEMS.getKey(itemStack.getItem()));
      if (enableItems.contains(uniqueName)) {
        var state = world.getBlockState(event.getPos());
        var canHarvestBlock = ForgeHooks.isCorrectToolForDrops(state, player);
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
        ChainDestructionLogic.setup(event.getState(), event.getPlayer(), (ServerLevel) event.getWorld(), event.getPos());
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
    var player = event.getPlayer();
    player.getCapability(CapabilityAdditionalPlayerStatus.CAPABILITY).ifPresent(status -> {
      var world = player.getCommandSenderWorld();
      var itemStack = player.getMainHandItem();
      if (world.isClientSide || itemStack.isEmpty()) {
        return;
      }
      var uniqueName = StringUtils.getUniqueString(ForgeRegistries.ITEMS.getKey(itemStack.getItem()));
      if (status.getEnableItems().contains(uniqueName)) {
        var state = world.getBlockState(event.getPos());
        if (status.isPrivateRegisterMode()) {
          itemStack.getCapability(CapabilityAdditionalItemStackStatus.CAPABILITY).ifPresent(itemStatus -> {
            Set<String> enableBlocks = status.isTreeMode() ? itemStatus.getEnableLogBlocks()
                    : itemStatus.getEnableBlocks();
            Set<String> forbiddenTags = status.getForbiddenTags();
            ChainDestructionLogic.addAndRemoveBlocks(enableBlocks, forbiddenTags, player, state);
          });
        } else {
          ChainDestructionLogic.addAndRemoveBlocks(
                  status.isTreeMode() ? status.getEnableLogBlocks() : status.getEnableBlocks(), status.getForbiddenTags(), player,
                  state);

        }
      }
    });
  }
}