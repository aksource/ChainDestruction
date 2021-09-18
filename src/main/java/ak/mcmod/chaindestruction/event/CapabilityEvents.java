package ak.mcmod.chaindestruction.event;

import ak.mcmod.chaindestruction.capability.CDItemStackStatus;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.ICDPlayerStatusHandler;
import ak.mcmod.chaindestruction.util.ConfigUtils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

import static ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler.CD_ITEM_STATUS;
import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;
import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CD_STATUS;

/**
 * Capability周りのイベントクラス Created by A.K. on 2017/03/25.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
//@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.DEDICATED_SERVER})
public class CapabilityEvents {

  /**
   * Capabilityの登録
   *
   * @param event AttachCapabilitiesEvent.Entity
   */
  @SubscribeEvent
  public static void onAttachingEntity(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof PlayerEntity) {
      event.addCapability(CD_STATUS, new CDPlayerStatus());
    }
  }

  /**
   * Capabilityの登録
   *
   * @param event AttachCapabilitiesEvent.Item
   */
  @SubscribeEvent
  public static void onAttachingItemStack(AttachCapabilitiesEvent<ItemStack> event) {
    if (!event.getObject().isEmpty()
            && Objects.nonNull(ConfigUtils.COMMON.excludeItemPredicate) && !ConfigUtils.COMMON.excludeItemPredicate
            .test(event.getObject().getItem().getRegistryName())) {
      event.addCapability(CD_ITEM_STATUS, new CDItemStackStatus());
    }
  }

  @SubscribeEvent
  public static void onCloningPlayer(final PlayerEvent.Clone event) {
    //死亡時に呼ばれてるかどうか
    if (event.isWasDeath()) {
      //古いカスタムデータ
      LazyOptional<ICDPlayerStatusHandler> oldCDS = event.getOriginal()
              .getCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
      //新しいカスタムデータ
      LazyOptional<ICDPlayerStatusHandler> newCDS = event.getPlayer()
              .getCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
      INBT nbt = CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage()
              .writeNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, oldCDS.orElse(new CDPlayerStatus()), null);
      CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage()
              .readNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, newCDS.orElse(new CDPlayerStatus()), null,
                      nbt);

    }
  }
}
