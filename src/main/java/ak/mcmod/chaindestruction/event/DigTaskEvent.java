package ak.mcmod.chaindestruction.event;

import ak.mcmod.chaindestruction.util.DigTask;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        if (event.phase == TickEvent.Phase.END && !event.player.getCommandSenderWorld().isClientSide) {
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
        if (event.getEntityLiving() instanceof PlayerEntity && !(event.getEntityLiving()).getCommandSenderWorld().isClientSide) {
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
        if (!event.getPlayer().getCommandSenderWorld().isClientSide) {
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
