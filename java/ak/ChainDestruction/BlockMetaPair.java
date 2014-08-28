package ak.ChainDestruction;

import net.minecraft.block.Block;

/**
 * Created by A.K. on 14/08/28.
 */
public class BlockMetaPair {
    private Block block;
    private int meta;

    public BlockMetaPair(Block block, int meta) {
        this.block = block;
        this.meta = meta;
    }

    public static BlockMetaPair getPair(Block block, int meta) {
        return new BlockMetaPair(block, meta);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + block.hashCode();
        result = prime * result + meta;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        BlockMetaPair otherIdMetaPair = (BlockMetaPair) obj;

        return this.block == otherIdMetaPair.block && this.meta == otherIdMetaPair.meta;
    }

    @Override
    public String toString() {
        return String.format("BlockMetaPair [Block:%s, META:%d]", this.block.toString(), this.meta);
    }

    public Block getBlock() {
        return block;
    }

    public int getMeta() {
        return meta;
    }
}
