package jp.l1j.server.model.npc.action.individual;

import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.templates.L1Npc;

public class L1LastavardNpcActions {

	private static L1LastavardNpcActions _instance = null;

	public static synchronized L1LastavardNpcActions getInstance() {
		if (_instance == null) {
			_instance = new L1LastavardNpcActions();
		}
		return _instance;
	}

	public String actions(L1Npc npc, String s, L1PcInstance pc) {
		String htmlid = null;

		if (s.equalsIgnoreCase("a")) { // ラスタバドへ移動
			if (pc.getLevel() <= 70) {
				htmlid = "zigpride2";
			} else {
				L1Teleport.teleport(pc, 32690, 32801, (short) 450, 5, true);
			}
		}

		return htmlid;
	}
}
