package ak.ChainDestruction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * ブロック破壊のタスク処理イベントクラス
 * Created by A.K. on 15/01/13.
 */
public class DigTaskEvent {

    public Set<DigTask> digTaskSet = new HashSet<>();
    private Set<DigTask> digTaskRemoveSet = new HashSet<>();

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void deleteDigTaskOnDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && !((EntityPlayer) event.getEntityLiving()).worldObj.isRemote) {
            for (DigTask digTask : digTaskSet) {
                if (digTask.getDigger() == event.getEntityLiving()) {
                    digTaskRemoveSet.add(digTask);
                }
            }
            digTaskSet.removeAll(digTaskRemoveSet);
            digTaskRemoveSet.clear();
        }
    }

    @SuppressWarnings("unused")
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
