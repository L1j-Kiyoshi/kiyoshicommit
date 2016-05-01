package jp.l1j.server.model.npc.action.individual;

import static jp.l1j.server.model.skill.L1SkillId.*;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.skill.L1BuffUtil;
import jp.l1j.server.packets.server.S_NpcTalkReturn;
import jp.l1j.server.templates.L1Npc;

public class L1EnchantOfComaActions {

	private static L1EnchantOfComaActions _instance = null;

	public static synchronized L1EnchantOfComaActions getInstance() {
		if (_instance == null) {
			_instance = new L1EnchantOfComaActions();
		}
		return _instance;
	}

	public String actions(int objid, L1Npc npc, String s, L1PcInstance pc) {
		String htmlid = null;
		String[] htmldata = null;
		int[] counts;
		if (s.equalsIgnoreCase("1")) { // 欠片を3つ持ってきました
			counts = new int[5];
			int checkcount = 0;
			int consumecount = 0;
			for (int i = 0; i < 5; i++) {
				if (pc.getInventory().checkItem(50515 + i, 1)) {
					counts[i] = 1;
					checkcount++;
				} else {
					counts[i] = 0;
				}
			}
			if (checkcount > 2) {
				L1BuffUtil.effectBlessOfComa(pc, BLESS_OF_COMA1, 3600, 7382);
				// コマの祝福A
				for (int i=0; i < 5; i++) {
					if (counts[i] == 1) {
						pc.getInventory().consumeItem(50515 + i, counts[i]);
						consumecount++;
					}
					if (consumecount > 2) {
						break;
					}
				}
				htmlid = "";
			} else {
				htmlid = "coma3";
			}
		} else if (s.equalsIgnoreCase("2")) { // 欠片を5つ持ってきました
			counts = new int[5];
			int checkcount = 0;
			for (int i = 0; i < 5; i++) {
				if (pc.getInventory().checkItem(50515 + i, 1)) {
					counts[i] = 1;
					checkcount++;
				}
			}
			if (checkcount > 4) {
				L1BuffUtil.effectBlessOfComa(pc, BLESS_OF_COMA2, 7200, 7383);
				// コマの祝福B
				for (int i = 0; i < 5; i++) {
					pc.getInventory().consumeItem(50515 + i, 1);
				}
				htmlid = "";
			} else {
				htmlid = "coma3";
			}
		} else if (s.equalsIgnoreCase("3")) { // 最初からもう一度選びます
			pc.resetComaMaterialAmount();
			htmlid = "coma4";
		} else if (s.equalsIgnoreCase("4")) { // すべて選びました
			int count = pc.getTotalComaMaterialAmount();
			if (count < 3) {
				htmlid = "coma3_2";
			} if (count == 3 || count == 5) {
				int amount = 0;
				boolean isError = false;
				for (int i = 0; i < 5; i++) {
					amount = pc.getComaMaterialAmount(i);
					if (amount > 0 && !pc.getInventory().checkItem(50515 + i, amount)) {
						isError = true;
						break;
					}
				}
				if (isError) {
					htmlid = "coma3_3";
				} else {
					if (count == 3) { // コマの祝福A
						L1BuffUtil.effectBlessOfComa(pc, BLESS_OF_COMA1, 3600, 7382);
					} else if (count == 5) { // コマの祝福B
						L1BuffUtil.effectBlessOfComa(pc, BLESS_OF_COMA2, 7200, 7383);
					}
					for (int i = 0; i < 5; i++) {
						amount = pc.getComaMaterialAmount(i);
						if (amount > 0 && pc.getInventory().checkItem(50515 + i, amount)) {
							pc.getInventory().consumeItem(50515 + i, amount);
						}
					}
					pc.resetComaMaterialAmount();
					htmlid = "";
				}
			} else if (count > 5) {
				htmlid = "coma3_1";
			} else {
				htmlid = "coma3_3";
			}
		} else if (s.equalsIgnoreCase("a")) { // デスマッチ 1個
			if (pc.getInventory().checkItem(50515, 1)) {
				pc.setComaMaterialAmount(0, 1);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("b")) { // デスマッチ 2個
			if (pc.getInventory().checkItem(50515, 2)) {
				pc.setComaMaterialAmount(0, 2);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("c")) { // デスマッチ 3個
			if (pc.getInventory().checkItem(50515, 3)) {
				pc.setComaMaterialAmount(0, 3);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("d")) { // デスマッチ 4個
			if (pc.getInventory().checkItem(50515, 4)) {
				pc.setComaMaterialAmount(0, 4);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("e")) { // デスマッチ 5個
			if (pc.getInventory().checkItem(50515, 5)) {
				pc.setComaMaterialAmount(0, 5);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("f")) { // お化け屋敷 1個
			if (pc.getInventory().checkItem(50516, 1)) {
				pc.setComaMaterialAmount(1, 1);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("g")) { // お化け屋敷 2個
			if (pc.getInventory().checkItem(50516, 2)) {
				pc.setComaMaterialAmount(1, 2);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("h")) { // お化け屋敷 3個
			if (pc.getInventory().checkItem(50516, 3)) {
				pc.setComaMaterialAmount(1, 3);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("i")) { // お化け屋敷 4個
			if (pc.getInventory().checkItem(50516, 4)) {
				pc.setComaMaterialAmount(1, 4);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("j")) { // お化け屋敷 5個
			if (pc.getInventory().checkItem(50516, 5)) {
				pc.setComaMaterialAmount(1, 5);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("k")) { // ペットレース 1個
			if (pc.getInventory().checkItem(50517, 1)) {
				pc.setComaMaterialAmount(2, 1);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("l")) { // ペットレース 2個
			if (pc.getInventory().checkItem(50517, 2)) {
				pc.setComaMaterialAmount(2, 2);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("m")) { // ペットレース 3個
			if (pc.getInventory().checkItem(50517, 3)) {
				pc.setComaMaterialAmount(2, 3);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("n")) { // ペットレース 4個
			if (pc.getInventory().checkItem(50517, 4)) {
				pc.setComaMaterialAmount(2, 4);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("o")) { // ペットレース 5個
			if (pc.getInventory().checkItem(50517, 5)) {
				pc.setComaMaterialAmount(2, 5);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("p")) { // ペットマッチ 1個
			if (pc.getInventory().checkItem(50518, 1)) {
				pc.setComaMaterialAmount(3, 1);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("q")) { // ペットマッチ 2個
			if (pc.getInventory().checkItem(50518, 2)) {
				pc.setComaMaterialAmount(3, 2);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("s")) { // ペットマッチ 3個
			if (pc.getInventory().checkItem(50518, 3)) {
				pc.setComaMaterialAmount(3, 3);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("t")) { // ペットマッチ 4個
			if (pc.getInventory().checkItem(50518, 4)) {
				pc.setComaMaterialAmount(3, 4);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("u")) { // ペットマッチ 5個
			if (pc.getInventory().checkItem(50518, 5)) {
				pc.setComaMaterialAmount(3, 5);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("v")) { // アルティメットバトル 1個
			if (pc.getInventory().checkItem(50519, 1)) {
				pc.setComaMaterialAmount(4, 1);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("w")) { // アルティメットバトル 2個
			if (pc.getInventory().checkItem(50519, 2)) {
				pc.setComaMaterialAmount(4, 2);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("x")) { // アルティメットバトル 3個
			if (pc.getInventory().checkItem(50519, 3)) {
				pc.setComaMaterialAmount(4, 3);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("y")) { // アルティメットバトル 4個
			if (pc.getInventory().checkItem(50519, 4)) {
				pc.setComaMaterialAmount(4, 4);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		} else if (s.equalsIgnoreCase("z")) { // アルティメットバトル 5個
			if (pc.getInventory().checkItem(50519, 5)) {
				pc.setComaMaterialAmount(4, 5);
			}
			htmlid = "coma5";
			htmldata = pc.getAllComaMaterialAmount();
		}

		if (htmlid != null) { // html指定がある場合は表示
			pc.sendPackets(new S_NpcTalkReturn(objid, htmlid, htmldata));
		}

		return null;
	}
}
