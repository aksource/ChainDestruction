package ak.ChainDestruction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

/**
 * 連鎖破壊のプレイヤー別状態保存クラス
 * Created by A.K. on 2015/09/26.
 */
public class CDStatus {
    private EnumFacing face = EnumFacing.DOWN;
    private EntityPlayer player;
    private boolean digUnder = false;
    private boolean treeMode = false;
    private boolean privateRegisterMode = false;

    public CDStatus() {
        this.setDigUnder(ChainDestruction.digUnder);
        this.setPrivateRegisterMode(ChainDestruction.privateRegisterMode);
        this.setTreeMode(ChainDestruction.treeMode);
    }

    public EnumFacing getFace() {
        return face;
    }

    public void setFace(EnumFacing face) {
        this.face = face;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }

    public boolean isDigUnder() {
        return digUnder;
    }

    public void setDigUnder(boolean digUnder) {
        this.digUnder = digUnder;
    }

    public boolean isTreeMode() {
        return treeMode;
    }

    public void setTreeMode(boolean treeMode) {
        this.treeMode = treeMode;
    }

    public boolean isPrivateRegisterMode() {
        return privateRegisterMode;
    }

    public void setPrivateRegisterMode(boolean privateRegisterMode) {
        this.privateRegisterMode = privateRegisterMode;
    }
}
