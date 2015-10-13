package jp.l1j.server.command.executor;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import jp.l1j.server.GameServer;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_SystemMessage;


public class L1ServerAutoRestart implements L1CommandExecutor {
	private static Logger _log = Logger.getLogger(L1ServerAutoRestart.class.getName());

	private L1ServerAutoRestart() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1ServerAutoRestart();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st = new StringTokenizer(arg);
			String flag = st.nextToken();
			if (flag.equalsIgnoreCase("true")) {
				GameServer.getInstance().setAutoRestart(true);
				pc.sendPackets(new S_SystemMessage("ゲームサーバーの自動再起動をONにしました。"));
			} else {
				GameServer.getInstance().setAutoRestart(false);
				pc.sendPackets(new S_SystemMessage("ゲームサーバーの自動再起動をOFFにしました。"));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("." + cmdName + " true | false　の形式で入力してください。"));
			// .%s %s の形式で入力してください。
		}
	}

}
