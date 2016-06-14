package ak.ChainDestruction;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;

import java.util.Comparator;

/**
 * Created by A.K. on 14/10/15.
 */
public class CompareToOrigin implements Comparator<ChunkCoordinates> {
    private ChunkCoordinates origin;
    public CompareToOrigin(ChunkCoordinates origin) {
        this.origin = origin;
    }
    @Override
    public int compare(ChunkCoordinates o1, ChunkCoordinates o2) {
        int distance1 = MathHelper.ceiling_float_int(origin.getDistanceSquaredToChunkCoordinates(o1));
        int distance2 = MathHelper.ceiling_float_int(origin.getDistanceSquaredToChunkCoordinates(o2));
        return distance1 - distance2;
    }
}
