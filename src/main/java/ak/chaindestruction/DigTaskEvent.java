package ak.chaindestruction;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        if (event.phase == TickEvent.Phase.END && !event.player.getEntityWorld().isRemote) {
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
        if (event.getEntityLiving() instanceof PlayerEntity && !(event.getEntityLiving()).getEntityWorld().isRemote) {
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
        if (!event.getPlayer().getEntityWorld().isRemote) {
            for (DigTask digTask : digTaskSet) {
                if (digTask.getDigger() == event.getPlayer()) {
                    digTaskRemoveSet.add(digTask);
                }
            }
            digTaskSet.removeAll(digTaskRemoveSet);
            digTaskRemoveSet.clear();
        }
    }
}
