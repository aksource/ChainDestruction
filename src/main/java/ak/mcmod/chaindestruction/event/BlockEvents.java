package ak.mcmod.chaindestruction.event;

import ak.mcmod.ak_lib.util.StringUtils;
import ak.mcmod.chaindestruction.capability.CDItemStackStatus;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.ICDItemStackStatusHandler;
import ak.mcmod.chaindestruction.capability.ICDPlayerStatusHandler;
import ak.mcmod.chaindestruction.util.ChainDestructionLogic;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Set;

import static ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_ITEM;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
//@Mod.EventBusSubscriber(modid = MOD_ID)
public class BlockEvents {

  /**
   * ブロックを右クリックした際に呼ばれるイベント
   *
   * @param event 右クリックイベント
   */
  @SubscribeEvent
  public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    EntityPlayer player = event.getEntityPlayer();
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    if (Objects.isNull(status)) {
      return;
    }
    World world = player.getEntityWorld();
    ItemStack itemStack = player.getHeldItemMainhand();
    if (world.isRemote || itemStack.isEmpty()) return;
    String uniqueName = StringUtils.getUniqueString(itemStack.getItem().getRegistryName());
    if (status.getEnableItems().contains(uniqueName)) {
      IBlockState state = world.getBlockState(event.getPos());
      if (status.isPrivateRegisterMode() && itemStack.hasCapability(CAPABILITY_CHAIN_DESTRUCTION_ITEM, null)) {
        ICDItemStackStatusHandler itemStatus = CDItemStackStatus.get(itemStack);
        if (Objects.isNull(itemStatus)) {
          return;
        }
        Set<String> enableBlocks = status.isTreeMode() ? itemStatus.getEnableLogBlocks() : itemStatus.getEnableBlocks();
        ChainDestructionLogic.addAndRemoveBlocks(enableBlocks, player, state);
      } else {
        ChainDestructionLogic.addAndRemoveBlocks(status.isTreeMode() ? status.getEnableLogBlocks() : status.getEnableBlocks(), player, state);

      }
    }
  }

  @SubscribeEvent
  public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
    EntityPlayer player = event.getEntityPlayer();
    ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
    if (Objects.isNull(status)) {
      return;
    }
    Set<String> enableItems = status.getEnableItems();
    World world = player.getEntityWorld();
    ItemStack itemStack = player.getHeldItemMainhand();
    if (world.isRemote || itemStack.isEmpty()) return;
    String uniqueName = StringUtils.getUniqueString(itemStack.getItem().getRegistryName());
    if (enableItems.contains(uniqueName)) {
      IBlockState state = world.getBlockState(event.getPos());
      boolean canHarvestBlock = ForgeHooks.canHarvestBlock(state.getBlock(), player, world, event.getPos());
      if (ChainDestructionLogic.checkBlockValidate(player, state, itemStack, canHarvestBlock)
              && enableItems.contains(StringUtils.getUniqueString(itemStack.getItem().getRegistryName()))
              && Objects.nonNull(event.getFace())) {
        status.setFace(event.getFace());
      }
    }
  }

  /*偽装しているブロックへの対応含めこちらで処理したほうが良い*/
  @SubscribeEvent
  public static void blockBreakingEvent(BlockEvent.BreakEvent event) {
    if (!(event.getPlayer() instanceof FakePlayer) && !event.getWorld().isRemote) {
      if (ChainDestructionLogic.isChainDestructionActionable(event.getWorld(), event.getPlayer(), event.getState(), event.getPos(),
              event.getPlayer().getHeldItemMainhand())) {
        ChainDestructionLogic.setup(event.getState(), event.getPlayer(), event.getWorld(), event.getPos());
      }
    }
  }

}