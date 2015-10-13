package jp.l1j.server.model.npc.action.individual;

import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.templates.L1Npc;

public class L1PiersNpcActions {

	private static L1PiersNpcActions _instance = null;

	public static synchronized L1PiersNpcActions getInstance() {
		if (_instance == null) {
			_instance = new L1PiersNpcActions();
		}
		return _instance;
	}

	public String actions(L1Npc npc, String s, L1PcInstance pc) {
		String htmlid = null;
		int[] weaponList = { 13, 81, 162, 177, 194 };
		if (s.equals("a")) { // 闇の力がほしいです。
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

			if (isEightWeapon && isNineWeapon) {
				if (pc.getInventory().checkItem(40308, 10000000)) {
					htmlid = "piers01";
				} else if (pc.getInventory().checkItem(40308, 5000000)) {
					htmlid = "piers03";
				} else {
					htmlid = "piers04";
				}
			} else if (isNineWeapon) {
				if (pc.getInventory().checkItem(40308, 10000000)) {
					htmlid = "piers02";
				} else {
					htmlid = "piers04";
				}
			} else if (isEightWeapon) {
				if (pc.getInventory().checkItem(40308, 5000000)) {
					htmlid = "piers03";
				} else {
					htmlid = "piers04";
				}
			} else {
				htmlid = "piers04";
			}
		} else if (s != null) {
			int createItemId = 0;
			int checkEnchant = 8;
			int adenaCount = 5000000;
			if (s.equals("A")) { // +7破壊のクロウ
				createItemId = 41499;
			} else if (s.equals("B")) { // +7破壊のデュアルブレード
				createItemId = 41500;
			} else if (s.equals("C")) { // +8破壊のクロウ
				createItemId = 41501;
				checkEnchant = 9;
				adenaCount = 10000000;
			} else if (s.equals("D")) { // +8破壊のデュアルブレード
				createItemId = 41502;
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

		return htmlid;
	}
}
