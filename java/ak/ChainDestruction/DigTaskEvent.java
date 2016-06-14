package ak.ChainDestruction;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * DigTaskをTick処理するためのイベントハンドラクラス
 * Created by A.K. on 15/01/13.
 */
public class DigTaskEvent {

    public Set<DigTask> digTaskSet = new HashSet<>();
    private Set<DigTask> digTaskRemoveSet = new HashSet<>();

    @SubscribeEvent
    public void digTaskTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.worldObj.isRemote) {
            for (DigTask digTask : digTaskSet) {
                if (digTask.increaseCount()) {
                    digTaskRemoveSet.add(digTask);
                }
            }
            digTaskSet.removeAll(digTaskRemoveSet);
            digTaskRemoveSet.clear();
        }
    }

    @SubscribeEvent
    public void deleteDigTaskOnDeath(LivingDeathEvent event) {
        if (event.entityLiving instanceof EntityPlayer && !((EntityPlayer) event.entityLiving).worldObj.isRemote) {
            for (DigTask digTask : digTaskSet) {
                if (digTask.getDigger() == event.entityLiving) {
                    digTaskRemoveSet.add(digTask);
                }
            }
            digTaskSet.removeAll(digTaskRemoveSet);
            digTaskRemoveSet.clear();
        }
    }

    @SubscribeEvent
    public void deleteDigTaskOnLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.player.worldObj.isRemote) {
            for (DigTask digTask : digTaskSet) {
                if (digTask.getDigger() == event.player) {
                    digTaskRemoveSet.add(digTask);
                }
            }
            digTaskSet.removeAll(digTaskRemoveSet);
            digTaskRemoveSet.clear();
        }
    }
}
