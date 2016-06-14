package ak.akapi;

/**
 * Created by A.K. on 14/10/16.
 */
/*26方向走査できるようにForgeDirectionを拡張。*/
public enum ChainDirection {
    /**
     * -Y
     */
    DOWN(0, -1, 0),

    /**
     * +Y
     */
    UP(0, 1, 0),

    /**
     * -Z
     */
    NORTH(0, 0, -1),

    /**
     * +Z
     */
    SOUTH(0, 0, 1),

    /**
     * -X
     */
    WEST(-1, 0, 0),

    /**
     * +X
     */
    EAST(1, 0, 0),

    /**
     * -X-Z
     */
    NORTHWEST(-1, 0, -1),

    /**
     * +X+Z
     */
    SOUTHEAST(1, 0, 1),

    /**
     * -X+Z
     */
    NORTHEAST(-1, 0, 1),

    /**
     * +X-Z
     */
    SOUTHWEST(1, 0, -1),

    /**
     * -Y-Z
     */
    DOWNNORTH(0, -1, -1),

    /**
     * +Y+Z
     */
    UPSOUTH(0, 1, 1),

    /**
     * -X-Y-Z
     */
    DOWNNORTHWEST(-1, -1, -1),

    /**
     * +X+Y+Z
     */
    UPSOUTHEAST(1, 1, 1),

    /**
     * -X-Y
     */
    DOWNWEST(-1, -1, 0),

    /**
     * +X+Y
     */
    UPEAST(1, 1, 0),

    /**
     * -X-Y+Z
     */
    DOWNSOUTHWEST(-1, -1, 1),

    /**
     * +X+Y-Z
     */
    UPNORTHEAST(1, 1, -1),

    /**
     * -Y+Z
     */
    DOWNSOUTH(0, -1, 1),

    /**
     * +Y-Z
     */
    UPNORTH(0, 1, -1),

    /**
     * +X-Y+Z
     */
    DOWNSOUTHEAST(1, -1, 1),

    /**
     * -X+Y-Z
     */
    UPNORTHWEST(-1, 1, -1),

    /**
     * +X-Y
     */
    DOWNEAST(1, -1, 0),

    /**
     * -X+Y
     */
    UPWEST(-1, 1, 0),

    /**
     * +X-Y-Z
     */
    DOWNNORTHEAST(1, -1, -1),

    /**
     * -X+Y+Z
     */
    UPSOUTHWEST(-1, 1, 1),

    /**
     * Used only by getOrientation, for invalid inputs
     */
    UNKNOWN(0, 0, 0);

    public final int offsetX;
    public final int offsetY;
    public final int offsetZ;
    public final int flag;
    public static final ChainDirection[] VALID_DIRECTIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST,
            NORTHWEST, SOUTHEAST, NORTHEAST, SOUTHWEST,
            DOWNNORTH, UPSOUTH, DOWNNORTHWEST, UPSOUTHEAST, DOWNWEST, UPEAST, DOWNSOUTHWEST, UPNORTHEAST, DOWNSOUTH, UPNORTH, DOWNSOUTHEAST, UPNORTHWEST, DOWNEAST, UPWEST, DOWNNORTHEAST, UPSOUTHWEST};
    public static final int[] OPPOSITES = {1, 0, 3, 2, 5, 4, 7, 6, 9, 8, 11, 10, 13, 12, 15, 14, 17, 16, 19, 18, 21, 20, 23, 22, 25, 24, 26};

    private ChainDirection(int x, int y, int z) {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        flag = 1 << ordinal();
    }

    public static ChainDirection getOrientation(int id) {
        if (id >= 0 && id < VALID_DIRECTIONS.length) {
            return VALID_DIRECTIONS[id];
        }
        return UNKNOWN;
    }

    public ChainDirection getOpposite() {
        return getOrientation(OPPOSITES[ordinal()]);
    }

}
