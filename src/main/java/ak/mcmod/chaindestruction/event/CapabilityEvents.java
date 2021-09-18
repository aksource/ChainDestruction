package ak.mcmod.chaindestruction.event;

import ak.mcmod.chaindestruction.ChainDestruction;
import ak.mcmod.chaindestruction.capability.CDItemStackStatus;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.ICDPlayerStatusHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

import static ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler.CD_ITEM_STATUS;
import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CAPABILITY_CHAIN_DESTRUCTION_PLAYER;
import static ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler.CD_STATUS;

/**
 * Capability周りのイベントクラス
 * Created by A.K. on 2017/03/25.
 */

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
//@Mod.EventBusSubscriber(modid = MOD_ID)
public class CapabilityEvents {

    /**
     * Capabilityの登録
     * @param event AttachCapabilitiesEvent.Entity
     */
    @SubscribeEvent
    public static void onAttachingEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(CD_STATUS, new CDPlayerStatus());
        }
    }

    /**
     * Capabilityの登録
     * @param event AttachCapabilitiesEvent.Item
     */
    @SubscribeEvent
    public static void onAttachingItemStack(AttachCapabilitiesEvent<ItemStack> event) {
        if (!event.getObject().isEmpty()
                && !ChainDestruction.excludeItemPredicate.test(event.getObject().getItem().getRegistryName())) {
            event.addCapability(CD_ITEM_STATUS, new CDItemStackStatus());
        }
    }


    @SubscribeEvent
    //Dimension移動時や、リスポーン時に呼ばれるイベント。古いインスタンスと新しいインスタンスの両方を参照できる。
    public static void onCloningPlayer(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        //死亡時に呼ばれてるかどうか
        if (event.isWasDeath()) {
            //古いカスタムデータ
            ICDPlayerStatusHandler oldCDS = event.getOriginal().getCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
            //新しいカスタムデータ
            ICDPlayerStatusHandler newCDS = event.getEntityPlayer().getCapability(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, null);
            NBTBase nbt = CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage().writeNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, oldCDS, null);
            CAPABILITY_CHAIN_DESTRUCTION_PLAYER.getStorage().readNBT(CAPABILITY_CHAIN_DESTRUCTION_PLAYER, newCDS, null, nbt);

        }
    }
}
