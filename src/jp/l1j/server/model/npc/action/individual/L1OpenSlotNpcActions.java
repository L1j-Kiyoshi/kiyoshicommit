package jp.l1j.server.model.npc.action.individual;

import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_MessageYN;
import jp.l1j.server.templates.L1Npc;

public class L1OpenSlotNpcActions {

	private static L1OpenSlotNpcActions _instance = null;

	public static synchronized L1OpenSlotNpcActions getInstance() {
		if (_instance == null) {
			_instance = new L1OpenSlotNpcActions();
		}
		return _instance;
	}

	public String actions(L1Npc npc, String s, L1PcInstance pc) {
		String htmlid = null;

		if (s.equalsIgnoreCase("A")) { // 76リングスロット解放
			if (pc.isRingSlotOpen76()) { // 解放済
				htmlid = "slot5";
			} else if (pc.getInventory().checkItem(40308, 10000000) &&
						pc.getLevel() >= 76) { // アデナかレベルが足りない場合
				pc.setRingSlotOpen76(true);
				pc.getInventory().consumeItem(40308, 10000000);
				pc.saveConfig();
				htmlid = "slot9";
			} else {
				htmlid = "slot6";
			}
		} else if (s.equalsIgnoreCase("B")) { // 81リングスロット解放
			if (pc.isRingSlotOpen81()) { // 解放済
				htmlid = "slot5";
			} else if (!pc.isRingSlotOpen76()) { // 76未開放
				htmlid = "slot6";
			} else if (pc.getInventory().checkItem(40308, 30000000) &&
						pc.getLevel() >= 81) {
				pc.setRingSlotOpen81(true);
				pc.getInventory().consumeItem(40308, 30000000);
				pc.saveConfig();
				htmlid = "slot9";
			} else {
				htmlid = "slot6";
			}
		}
		return htmlid;
	}
}
