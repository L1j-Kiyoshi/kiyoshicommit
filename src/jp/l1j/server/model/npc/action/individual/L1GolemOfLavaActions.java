package jp.l1j.server.model.npc.action.individual;

import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.templates.L1Npc;

public class L1GolemOfLavaActions {

	private static L1GolemOfLavaActions _instance = null;

	public static synchronized L1GolemOfLavaActions getInstance() {
		if (_instance == null) {
			_instance = new L1GolemOfLavaActions();
		}
		return _instance;
	}

	public String actions(L1Npc npc, String s, L1PcInstance pc) {
		String htmlid = null;

		int[] weaponList = { 37, 41, 42, 52, 180, 181, 131 };
		if (s.equals("a")) {
			boolean isEightWeapon = false;
			boolean isNineWeapon = false;

			for (int i = 0; i < weaponList.length; i++) {
				if (pc.getInventory().checkEnchantItem(weaponList[i], 8, 1)) {
					isEightWeapon = true;
				}
				if (pc.getInventory().checkEnchantItem(weaponList[i], 9, 1)) {
					isNineWeapon = true;
				}
			}

			if (isEightWeapon && isNineWeapon) { // 8武器、9武器所持
				if (pc.getInventory().checkItem(40308, 10000000)) {
					htmlid = "rushi01"; // 8武器、9武器両方所持でアデナあり
				} else if (pc.getInventory().checkItem(40308, 5000000)) {
					htmlid = "rushi03"; // 8武器、9武器両方所持でアデナ半分
				} else {
					htmlid = "rushi04"; // アデナがない
				}
			} else if (isNineWeapon) { // 9武器のみ所持
				if (pc.getInventory().checkItem(40308, 10000000)) {
					htmlid = "rushi02"; // アデナあり
				} else {
					htmlid = "rushi04"; // アデナなし
				}
			} else if (isEightWeapon) { // 8武器のみ所持
				if (pc.getInventory().checkItem(40308, 5000000)) {
					htmlid = "rushi03"; // アデナあり
				} else {
					htmlid = "rushi04"; // アデナなし
				}
			} else {
				htmlid = "rushi04";
			}
		} else if (s != null) {
			int createItemId = 0;
			int checkEnchant = 8;
			int adenaCount = 5000000;
			if (s.equals("A")) {
				createItemId = 41485;
			} else if (s.equals("B")) {
				createItemId = 41486;
			} else if (s.equals("C")) {
				createItemId = 41487;
			} else if (s.equals("D")) {
				createItemId = 41488;
			} else if (s.equals("E")) {
				createItemId = 41489;
			} else if (s.equals("F")) {
				createItemId = 41490;
			} else if (s.equals("G")) {
				createItemId = 41491;
			} else if (s.equals("H")) {
				createItemId = 41492;
				checkEnchant = 9;
				adenaCount = 10000000;
			} else if (s.equals("I")) {
				createItemId = 41493;
				checkEnchant = 9;
				adenaCount = 10000000;
			} else if (s.equals("J")) {
				createItemId = 41494;
				checkEnchant = 9;
				adenaCount = 10000000;
			} else if (s.equals("K")) {
				createItemId = 41495;
				checkEnchant = 9;
				adenaCount = 10000000;
			} else if (s.equals("L")) {
				createItemId = 41496;
				checkEnchant = 9;
				adenaCount = 10000000;
			} else if (s.equals("M")) {
				createItemId = 41497;
				checkEnchant = 9;
				adenaCount = 10000000;
			} else if (s.equals("N")) {
				createItemId = 41498;
				checkEnchant = 9;
				adenaCount = 10000000;
			}
			if (createItemId != 0) {
				L1ItemInstance createItem;
				for (int i = 0; i < weaponList.length; i++) {
					if (pc.getInventory().checkEnchantItem(weaponList[i], checkEnchant, 1)) {
						if (pc.getInventory().checkItem(40308, adenaCount)) {
							pc.getInventory().consumeEnchantItem(weaponList[i], checkEnchant, 1);
							pc.getInventory().consumeItem(40308, adenaCount);
							createItem = pc.getInventory().storeItem(createItemId, 1);
							// \f1%0が%1をくれました。
							pc.sendPackets(new S_ServerMessage(143, npc.getName(),
									 createItem.getItem().getUnidentifiedNameId()));
							htmlid = "";
							break;
						}
					}
				}
			}
		}

		if (htmlid == null) {
			htmlid = "rushi04";
		}
		return htmlid;
	}
}
