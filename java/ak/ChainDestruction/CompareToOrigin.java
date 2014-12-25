package ak.ChainDestruction;

import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.util.Comparator;

/**
 * Created by A.K. on 14/10/15.
 */
public class CompareToOrigin implements Comparator<BlockPos> {
    private BlockPos origin;
    public CompareToOrigin(BlockPos origin) {
        this.origin = origin;
    }
    @Override
    public int compare(BlockPos o1, BlockPos o2) {
        int distance1 = MathHelper.ceiling_double_int(origin.distanceSq(o1));
        int distance2 = MathHelper.ceiling_double_int(origin.distanceSq(o2));
        return distance1 - distance2;
    }
}
