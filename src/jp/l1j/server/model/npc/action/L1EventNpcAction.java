package jp.l1j.server.model.npc.action;

import jp.l1j.server.datatables.ItemTable;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_NpcTalkReturn;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.packets.server.S_SystemMessage;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1Item;
import jp.l1j.server.templates.L1Npc;

public class L1EventNpcAction {

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private static L1EventNpcAction _instance = null;

	public static synchronized L1EventNpcAction getInstance() {
		if (_instance == null) {
			_instance = new L1EventNpcAction();
		}
		return _instance;
	}

	public String actions(L1Npc npc, String s, L1PcInstance pc) {
		String htmlid = null;
		int newX;
		int newY;

		int[] materials = null;
		int[] counts = null;
		int[] createitem = null;
		int[] createcount = null;
		String success_htmlid = null;
		String failure_htmlid = null;
		String[] htmldata = null;

		if (npc.getNpcId() == 75000) { // 次元の歪み (12周年 Elemental Gyre)
			if (s.equalsIgnoreCase("a")) { // 思い切って歪みに入る
				newX = 32698 + _random.nextInt(6);
				newY = 32841 + _random.nextInt(6);
				L1Teleport.teleport(pc, newX, newY, (short) 7053, pc.getHeading(), true);
				htmlid = "";
			}
		} else if (npc.getNpcId() == 75003) { // 旧き風の精霊
			if (s.equalsIgnoreCase("a")) { // 絶界の孤島(1)へ行く
				newX = 32787 + _random.nextInt(5);
				newY = 32767 + _random.nextInt(5);
				L1Teleport.teleport(pc, newX, newY, (short) 7781, pc.getHeading(), true);
				htmlid = "";
			} else if (s.equalsIgnoreCase("b")) { // 絶界の孤島(2)へ行く
				newX = 32787 + _random.nextInt(5);
				newY = 32767 + _random.nextInt(5);
				L1Teleport.teleport(pc, newX, newY, (short) 7782, pc.getHeading(), true);
				htmlid = "";
			} else if (s.equalsIgnoreCase("c")) { // 絶界の孤島(3)へ行く
				pc.sendPackets(new S_SystemMessage("MAPが不安定の為、移動できません。"));
			} else if (s.equalsIgnoreCase("d")) { // 絶界の孤島の奥地(1)へ行く
				if (pc.getInventory().checkItem(60004, 100)) {
					pc.getInventory().consumeItem(60004, 100);
					newX = 32806 + _random.nextInt(5);
					newY = 32922 + _random.nextInt(5);
					L1Teleport.teleport(pc, newX, newY, (short) 7781, pc.getHeading(), true);
					htmlid = "";
				} else {
					htmlid = "jp_12d2";
				}
			} else if (s.equalsIgnoreCase("e")) { // 絶界の孤島の奥地(2)へ行く
				if (pc.getInventory().checkItem(60004, 100)) {
					pc.getInventory().consumeItem(60004, 100);
					newX = 32806 + _random.nextInt(5);
					newY = 32922 + _random.nextInt(5);
					L1Teleport.teleport(pc, newX, newY, (short) 7782, pc.getHeading(), true);
					htmlid = "";
				} else {
					htmlid = "jp_12d2";
				}
			} else if (s.equalsIgnoreCase("f")) { // 絶界の孤島の奥地(3)へ行く
				pc.sendPackets(new S_SystemMessage("MAPが不安定の為、移動できません。"));
			}
		} else if (npc.getNpcId() == 75004) { // 旧き火の精霊
			if (s.equalsIgnoreCase("a")) { // ガーダーをもらう
				materials = new int[] { 60004, 60003 };
				counts = new int[] { 200, 2 };
				createitem = new int[] { 21350 };
				createcount = new int[] { 1 };
				success_htmlid = "jp_12f4";
				failure_htmlid = "jp_12f2";
			} else if (s.equalsIgnoreCase("b")) { // ベルトをもらう
				materials = new int[] { 60004, 60003 };
				counts = new int[] { 100, 1 };
				createitem = new int[] { 21351 };
				createcount = new int[] { 1 };
				success_htmlid = "jp_12f4";
				failure_htmlid = "jp_12f2";
			}
		}
		if (createitem != null) { // アイテム精製
			boolean isCreate = true;
			if (materials != null) {
				for (int j = 0; j < materials.length; j++) {
					if (!pc.getInventory().checkItemNotEquipped(materials[j], counts[j])) {
						L1Item temp = ItemTable.getInstance().getTemplate(materials[j]);
						int cnt = counts[j] - pc.getInventory().countItems(counts[j]);
						pc.sendPackets(new S_ServerMessage(337, temp.getName() + " (" + cnt + ") "));
						// \f1%0が不足しています。
						isCreate = false;
					}
				}
			}

			if (isCreate) {
				// 容量と重量の計算
				int create_count = 0; // アイテムの個数（纏まる物は1個）
				int create_weight = 0;
				for (int k = 0; k < createitem.length; k++) {
					L1Item temp = ItemTable.getInstance().getTemplate(
							createitem[k]);
					if (temp.isStackable()) {
						if (!pc.getInventory().checkItem(createitem[k])) {
							create_count += 1;
						}
					} else {
						create_count += createcount[k];
					}
					create_weight += temp.getWeight() * createcount[k] / 1000;
				}
				// 容量確認
				if (pc.getInventory().getSize() + create_count > 180) {
					pc.sendPackets(new S_ServerMessage(263));
					// \f1一人のキャラクターが持って歩けるアイテムは最大180個までです。
					return null;
				}
				// 重量確認
				if (pc.getMaxWeight() < pc.getInventory().getWeight() + create_weight) {
					pc.sendPackets(new S_ServerMessage(82));
					// アイテムが重すぎて、これ以上持てません。
					return null;
				}

				if (materials != null) {
					for (int j = 0; j < materials.length; j++) { // 材料消費
						pc.getInventory().consumeItem(materials[j], counts[j]);
					}
				}
				for (int k = 0; k < createitem.length; k++) {
					L1ItemInstance item = pc.getInventory().storeItem(
							createitem[k], createcount[k]);
					if (item != null) {
						String itemName = ItemTable.getInstance().getTemplate(createitem[k]).getName();
						if (createcount[k] > 1) {
							pc.sendPackets(new S_ServerMessage(143,
									npc.getName(), itemName + " (" + createcount[k] + ")"));
							// \f1%0が%1をくれました 。
						} else {
							pc.sendPackets(new S_ServerMessage(143, npc.getName(), itemName));
							 // \f1%0が%1をくれました。
						}
					}
				}
				if (success_htmlid != null) { // html指定がある場合は表示
					pc.sendPackets(new S_NpcTalkReturn(npc.getId(), success_htmlid, htmldata));
				}
			} else { // 精製失敗
				if (failure_htmlid != null) { // html指定がある場合は表示
					pc.sendPackets(new S_NpcTalkReturn(npc.getId(), failure_htmlid, htmldata));
				}
			}
		}

		return htmlid;
	}
}
