package ak.ChainDestruction;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
//クライアント側でキー入力によって変化したboolean変数をサーバー側に伝達するパケット。AbstractPacketを継承
public class KeyHandlingPacket extends AbstractPacket
{
	//保持しておくboolean型変数
	boolean toggle;
	boolean digUnderToggle;
    boolean treeToggle;
	//引数を持つコンストラクタを追加する場合は、空のコンストラクタを用意してくれとのこと。
	public KeyHandlingPacket() {
	}
	//パケット生成を簡略化するために、boolean型変数を引数に取るコンストラクタを追加。
	public KeyHandlingPacket(boolean var1, boolean var2, boolean var3) {
		toggle = var1;
		digUnderToggle = var2;
        treeToggle = var3;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		//ByteBufに変数を代入。基本的にsetメソッドではなく、writeメソッドを使う。
		buffer.writeBoolean(toggle);
		buffer.writeBoolean(digUnderToggle);
        buffer.writeBoolean(treeToggle);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		//ByteBufから変数を取得。こちらもgetメソッドではなく、readメソッドを使う。
		toggle = buffer.readBoolean();
		digUnderToggle = buffer.readBoolean();
        treeToggle = buffer.readBoolean();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		//今回はクライアントの情報をサーバーに送るので、こちらはなにもしない。
		//NO OP

	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		//代入したいクラスの変数に代入。Worldインスタンスはplayerから取得できる。
		ChainDestruction.interactblockhook.toggle = toggle;
		ChainDestruction.interactblockhook.digUnderToggle = digUnderToggle;
        ChainDestruction.interactblockhook.treeToggle = treeToggle;
	}

}