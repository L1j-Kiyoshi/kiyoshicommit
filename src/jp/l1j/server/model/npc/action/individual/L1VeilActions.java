package jp.l1j.server.model.npc.action.individual;

import jp.l1j.configure.Config;
import jp.l1j.server.controller.DragonPortalController;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.templates.L1Npc;

import static jp.l1j.server.controller.raid.L1RaidId.*;

public class L1VeilActions {

	private static L1VeilActions _instance = null;

	public static synchronized L1VeilActions getInstance() {
		if (_instance == null) {
			_instance = new L1VeilActions();
		}
		return _instance;
	}

	public String actions(L1Npc npc, String s, L1PcInstance pc) {
		String htmlid = null;

		if (s.equalsIgnoreCase("0")) {
			if (pc.getInventory().checkItem(50500, 1)) {
				htmlid = "veil3";
			} else if (pc.getInventory().checkItem(50501)) {
				htmlid = "veil8";
			} else if (pc.getInventory().checkItem(40308, Config.DRUGA_BAG_PRICE)) {
				pc.getInventory().consumeItem(40308, Config.DRUGA_BAG_PRICE);
				L1ItemInstance item = pc.getInventory().storeItem(50500, 1);
				pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
				htmlid = "veil7"; // 売れた
			} else {
				htmlid = "veil4"; // 売れてない
			}
			return htmlid;
		}

		if (pc.getInventory().checkItem(41520) || pc.getInventory().checkItem(41521)
				|| pc.getInventory().checkItem(41522) || pc.getInventory().checkItem(41523)) { // キーを所有中
			pc.sendPackets(new S_ServerMessage(3413)); // ドラゴンレイド：ドラゴンキーを所有しており、購入はできません。
			return htmlid;
		}
		if (!pc.getInventory().checkItem(40308, Config.DRAGON_KEY_PRICE)) {
			pc.sendPackets(new S_ServerMessage(189)); // アデナが不足しています。
			return htmlid;
		}

		boolean isMapOpen = false; // MAPに空きがなければtrueになる。
		int keyId = 0;

		if (s.equalsIgnoreCase("a")) { // ドラゴンキー(地竜)購入
			isMapOpen = (DragonPortalController.getInstance().getOpenRaid(ANTHARAS_RAID) == 0);
			keyId = 41520;
		} else if (s.equalsIgnoreCase("b")) { // ドラゴンキー(水竜)購入
			isMapOpen = (DragonPortalController.getInstance().getOpenRaid(FAFURION_RAID) == 0);
			keyId = 41521;
		} else if (s.equalsIgnoreCase("c")) { // ドラゴンキー(風竜)購入
			isMapOpen = (DragonPortalController.getInstance().getOpenRaid(LINDVIOR_RAID) == 0);
			keyId = 41522;
		}

		if (isMapOpen) { // MAPに空きがない
			pc.sendPackets(new S_ServerMessage(3417)); // ドラゴンレイド：ドラゴンキーがすべて販売されて購入できません。
			return htmlid;
		}

		if (keyId != 0) {
			pc.getInventory().consumeItem(40308, Config.DRAGON_KEY_PRICE);
			L1ItemInstance item = pc.getInventory().storeItem(keyId, 1);
			pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
			htmlid = "";
		}

		return htmlid;
	}

}
