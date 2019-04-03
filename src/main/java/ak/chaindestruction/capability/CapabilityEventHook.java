package ak.chaindestruction.capability;

import static ak.chaindestruction.capability.CapabilityCDItemStackStatusHandler.CD_ITEM_STATUS;
import static ak.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CD_STATUS;

import ak.chaindestruction.ConfigUtils;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Capability周りのイベントクラス Created by A.K. on 2017/03/25.
 */
public class CapabilityEventHook {

  /**
   * Capabilityの登録
   *
   * @param event AttachCapabilitiesEvent.Entity
   */
  @SubscribeEvent
  @SuppressWarnings("unused")
  public void onAttachingEntity(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof EntityPlayer) {
      event.addCapability(CD_STATUS, new CDPlayerStatus());
    }
  }

  /**
   * Capabilityの登録
   *
   * @param event AttachCapabilitiesEvent.Item
   */
  @SubscribeEvent
  @SuppressWarnings("unused")
  public void onAttachingItemStack(AttachCapabilitiesEvent<ItemStack> event) {
    if (!event.getObject().isEmpty()
        && Objects.nonNull(ConfigUtils.COMMON.excludeItemPredicate) && !ConfigUtils.COMMON.excludeItemPredicate
        .test(event.getObject().getItem().getRegistryName())) {
      event.addCapability(CD_ITEM_STATUS, new CDItemStackStatus());
    }
  }

}
