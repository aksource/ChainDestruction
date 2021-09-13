package ak.mcmod.chaindestruction.event;

import ak.mcmod.chaindestruction.util.DigTask;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

/**
 * ブロック破壊のタスク処理イベントクラス
 * Created by A.K. on 15/01/13.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigTaskEvent {

    public final Set<DigTask> digTaskSet = new HashSet<>();
    private final Set<DigTask> digTaskRemoveSet = new HashSet<>();

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

    @SubscribeEvent
    public void deleteDigTaskOnDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && !(event.getEntityLiving()).getEntityWorld().isRemote) {
            for (DigTask digTask : digTaskSet) {
                if (digTask.getDigger() == event.getEntityLiving()) {
                    digTaskRemoveSet.add(digTask);
                }
            }
            digTaskSet.removeAll(digTaskRemoveSet);
            digTaskRemoveSet.clear();
        }
    }

    @SubscribeEvent
    public void deleteDigTaskOnLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.player.getEntityWorld().isRemote) {
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
