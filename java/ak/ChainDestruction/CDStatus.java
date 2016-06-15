package ak.ChainDestruction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

/**
 * 連鎖破壊のプレイヤー別状態保存クラス
 * Created by A.K. on 2015/09/26.
 */
public class CDStatus implements IExtendedEntityProperties{
    public final static String EXT_PROP_NAME = "cd:status";
    private static final String NBT_STATUS_DIG_UNDER = "cd:digUnder";
    private static final String NBT_CLICK_FACE = "cd:clickFace";
    private static final String NBT_STATUS_TREE_MODE = "cd:treeMode";
    private static final String NBT_STATUS_PRIVATE_MODE = "cd:privateMode";
    private EnumFacing face = EnumFacing.DOWN;
    private boolean digUnder = false;
    private boolean treeMode = false;
    private boolean privateRegisterMode = false;

    public static void register(EntityPlayer player) {
        player.registerExtendedProperties(EXT_PROP_NAME, new CDStatus());
    }

    public static CDStatus get(EntityPlayer player) {
        return (CDStatus) player.getExtendedProperties(EXT_PROP_NAME);
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte(NBT_CLICK_FACE, (byte) face.getIndex());
        nbt.setBoolean(NBT_STATUS_DIG_UNDER, digUnder);
        nbt.setBoolean(NBT_STATUS_TREE_MODE, treeMode);
        nbt.setBoolean(NBT_STATUS_PRIVATE_MODE, privateRegisterMode);
        compound.setTag(EXT_PROP_NAME, nbt);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        NBTTagCompound nbt = (NBTTagCompound) compound.getTag(EXT_PROP_NAME);
        face = EnumFacing.VALUES[nbt.getByte(NBT_CLICK_FACE) & 0xFF];
        digUnder = nbt.getBoolean(NBT_STATUS_DIG_UNDER);
        treeMode = nbt.getBoolean(NBT_STATUS_TREE_MODE);
        privateRegisterMode = nbt.getBoolean(NBT_STATUS_PRIVATE_MODE);
    }

    @Override
    public void init(Entity entity, World world) {}

    public EnumFacing getFace() {
        return face;
    }

    public void setFace(EnumFacing face) {
        this.face = face;
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
