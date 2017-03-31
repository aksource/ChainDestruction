package ak.ChainDestruction.capability;

import ak.ChainDestruction.ChainDestruction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static ak.ChainDestruction.capability.CapabilityCDItemStackStatusHandler.CD_ITEM_STATUS;
import static ak.ChainDestruction.capability.CapabilityCDPlayerStatusHandler.CD_STATUS;

/**
 * Capability周りのイベントクラス
 * Created by A.K. on 2017/03/25.
 */
public class CapabilityEventHook {

    /**
     * Capabilityの登録
     * @param event AttachCapabilitiesEvent.Entity
     */
    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onAttachingEntity(AttachCapabilitiesEvent.Entity event) {
        if (event.getEntity() instanceof EntityPlayer) {
            event.addCapability(CD_STATUS, new CDPlayerStatus());
        }
    }

    /**
     * Capabilityの登録
     * @param event AttachCapabilitiesEvent.Item
     */
    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onAttachingItemStack(AttachCapabilitiesEvent.Item event) {
        if (!event.getItemStack().isEmpty()
                && !ChainDestruction.excludeItemPredicate.test(event.getItem().getRegistryName())) {
            event.addCapability(CD_ITEM_STATUS, new CDItemStackStatus());
        }
    }

}
