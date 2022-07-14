package ak.mcmod.chaindestruction.event;

import ak.mcmod.chaindestruction.capability.*;
import ak.mcmod.chaindestruction.util.ConfigUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

import static ak.mcmod.chaindestruction.capability.CapabilityAdditionalItemStackStatus.CD_ITEM_STATUS;
import static ak.mcmod.chaindestruction.capability.CapabilityAdditionalPlayerStatus.CAPABILITY;
import static ak.mcmod.chaindestruction.capability.CapabilityAdditionalPlayerStatus.CD_STATUS;

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
   * @param event RegisterCapabilitiesEvent
   */
  @SubscribeEvent
  public static void registerCapability(RegisterCapabilitiesEvent event) {
    event.register(IAdditionalItemStackStatus.class);
    event.register(IAdditionalPlayerStatus.class);
  }

  /**
   * Capabilityの付与
   *
   * @param event AttachCapabilitiesEvent.Entity
   */
  @SubscribeEvent
  public static void onAttachingEntity(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof Player) {
      event.addCapability(CD_STATUS, new AdditionalPlayerStatusCapabilityProvider());
    }
  }

  /**
   * Capabilityの付与
   *
   * @param event AttachCapabilitiesEvent.Item
   */
  @SubscribeEvent
  public static void onAttachingItemStack(AttachCapabilitiesEvent<ItemStack> event) {
    if (!event.getObject().isEmpty()
            && Objects.nonNull(ConfigUtils.COMMON.excludeItemPredicate) && !ConfigUtils.COMMON.excludeItemPredicate
            .test(ForgeRegistries.ITEMS.getKey(event.getObject().getItem()))) {
      event.addCapability(CD_ITEM_STATUS, new AdditionalItemStackStatusCapabilityProvider());
    }
  }

  @SubscribeEvent
  public static void onCloningPlayer(final PlayerEvent.Clone event) {
    //死亡時に呼ばれてるかどうか
    if (event.isWasDeath()) {
      //古いカスタムデータ
      var oldCDS = event.getOriginal()
              .getCapability(CAPABILITY, null);
      //新しいカスタムデータ
      var newCDS = event.getEntity()
              .getCapability(CAPABILITY, null);
      var nbt = oldCDS.orElse(new AdditionalPlayerStatus()).serializeNBT();
      newCDS.orElse(new AdditionalPlayerStatus()).deserializeNBT(nbt);
    }
  }
}
