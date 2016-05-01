package jp.l1j.server.model.npc.action.individual;

import jp.l1j.server.datatables.ExpTable;
import jp.l1j.server.datatables.ItemTable;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.inventory.L1Inventory;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.templates.L1Npc;
import jp.l1j.server.utils.CalcExp;

public class L1IceQueenCastleNpcActions {

	private static L1IceQueenCastleNpcActions _instance = null;

	public static synchronized L1IceQueenCastleNpcActions getInstance() {
		if (_instance == null) {
			_instance = new L1IceQueenCastleNpcActions();
		}
		return _instance;
	}

	public String actions(L1Npc npc, String s, L1PcInstance pc) {
		String htmlid = null;

		if (npc.getNpcId() == 81376) { // マービン
			if (s.equalsIgnoreCase("b")) { // 手伝うと言う
				if (pc.getLevel() >= 52) {
					L1ItemInstance item = pc.getInventory().storeItem(41530, 1);
					pc.sendPackets(new S_ServerMessage(143, npc.getName(), item.getLogName()));
					// %0が%1を手に入れました。
					pc.setMarbinQuest(true);
					htmlid = "marbinquest2";
				} else {
					htmlid = "marbinquest8";
				}
			} else if (s.equalsIgnoreCase("a")) { // 調査結果を取り出す
				if (pc.getInventory().checkItem(41531, 1) && // 不完全な魔法玉の欠片
						pc.getInventory().checkItem(41533, 1) && // 象牙の塔の魔法の精粋
						pc.getInventory().checkItem(41534, 100)) { // 凍りついた女の涙
					L1ItemInstance item = ItemTable.getInstance().createItem(41536);
					item.setEnchantLevel(0);
					item.setCount(5);
					if (pc.getInventory().checkAddItem(item, 5) == L1Inventory.OK) {
						int marbinQuestExp = CalcExp.calcPercentageExp(49, 2); // 49換算2%
						double levelPenalty = ExpTable.getExpRate(pc.getLevel());
						pc.addExp((int) (marbinQuestExp / levelPenalty));
						pc.getInventory().consumeItem(41533, 1);
						pc.getInventory().consumeItem(41531, 1);
						pc.getInventory().consumeItem(41534);
						pc.getInventory().storeItem(item);
						pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0を手に入れました。
						pc.sendPackets(new S_SkillSound(pc.getId(), 3944));
						pc.broadcastPacket(new S_SkillSound(pc.getId(), 3944));
					} else { // 重量オーバー
						pc.sendPackets(new S_ServerMessage(84)); // アイテムが重すぎて、これ以上持てません。
					}
				} else { // 素材なし
					htmlid = "marbinquest5";
				}
			} else if (s.equalsIgnoreCase("c")) { // 100日間の調査が終わったと言う
				if (pc.getInventory().checkItem(41532, 1) && // 不完全な魔法玉
						pc.getInventory().checkItem(41533, 1) && // 象牙の塔の魔法の精粋
						pc.getInventory().checkItem(41534, 100)) { // 凍りついた女の涙
					L1ItemInstance item = ItemTable.getInstance().createItem(41538);
					item.setEnchantLevel(0);
					item.setCount(1);
					if (pc.getInventory().checkAddItem(item, 1) == L1Inventory.OK) {
						int marbinQuestExp = CalcExp.calcPercentageExp(49, 2); // 49換算2%
						double levelPenalty = ExpTable.getPenaltyRate(pc.getLevel());
						pc.addExp((int) (marbinQuestExp / levelPenalty));
						pc.getInventory().consumeItem(41532, 1);
						pc.getInventory().consumeItem(41533, 1);
						pc.getInventory().consumeItem(41534);
						pc.getInventory().storeItem(item);
						pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0を手に入れました。
						pc.sendPackets(new S_SkillSound(pc.getId(), 3944));
						pc.broadcastPacket(new S_SkillSound(pc.getId(), 3944));
						htmlid = "marbinquest6";
					} else { // 重量オーバー
						pc.sendPackets(new S_ServerMessage(82)); // アイテムが重すぎて、これ以上持てません。
					}
				}
			}
		} else if (npc.getNpcId() == 70751) { // テレポーター・ブラッド
			if (s.equalsIgnoreCase("a")) { // 氷水晶洞窟に送ってください
				if (pc.isCrown()) { // 君主
					L1Teleport.teleport(pc, 32734, 32852, (short) 277, 5, true);
				} else if (pc.isKnight()) { // ナイト
					L1Teleport.teleport(pc, 32737, 32810, (short) 276, 5, true);
				} else if (pc.isElf()) { // エルフ
					L1Teleport.teleport(pc, 32735, 32867, (short) 275, 5, true);
				} else if (pc.isWizard()) { // ウィザード
					L1Teleport.teleport(pc, 32739, 32856, (short) 271, 5, true);
				} else if (pc.isDarkelf()) { // ダークエルフ
					L1Teleport.teleport(pc, 32736, 32809, (short) 273, 5, true);
				} else if (pc.isIllusionist()) { // 幻術師
					L1Teleport.teleport(pc, 32809, 32830, (short) 272, 5, true);
				} else if (pc.isDragonKnight()) { // 竜騎士
					L1Teleport.teleport(pc, 32734, 32852, (short) 274, 5, true);
				}
				htmlid = "";
			} else if (s.equalsIgnoreCase("b")) { // アイスクイーンの城に移動する
				if (pc.getInventory().checkItem(41535, 1)) { // 火炎の気
					pc.getInventory().consumeItem(41535, 1);
					L1Teleport.teleport(pc, 32786, 32799, (short) 2100, 5, true);
					htmlid = "";
				} else {
					htmlid = "newbrad3";
				}
			}
		} else if (npc.getNpcId() == 71276) { // 象牙の塔の諜報員
			if (s.equalsIgnoreCase("a")) { // フレイム ワンドを要求する
/*				if (pc.getInventory().checkItem(41539)) { // フレイムワンド所持
					htmlid = "icqwand4";
				} else {
					L1ItemInstance item = ItemTable.getInstance().createItem(41539);
					item.setChargeCount(120);
					if (pc.getInventory().checkAddItem(item, 120) == L1Inventory.OK) {
						pc.getInventory().storeItem(item);
						htmlid = "icqwand2";
					} else {
						pc.sendPackets(new S_ServerMessage(82)); // アイテムが重すぎて、これ以上持てません。
						htmlid = "";
					}
				}
*/			} else if (s.equalsIgnoreCase("b")) { // 象牙の塔の回復ポーションを要求する
				if (pc.getInventory().checkItem(41540)) { // 神秘の回復ポーション所持
					htmlid = "icqwand4";
				} else {
					L1ItemInstance item = ItemTable.getInstance().createItem(41540);
					item.setCount(100);
					if (pc.getInventory().checkAddItem(item, 100) == L1Inventory.OK) {
						pc.getInventory().storeItem(item);
						htmlid = "icqwand3";
					} else {
						pc.sendPackets(new S_ServerMessage(82)); // アイテムが重すぎて、これ以上持てません。
						htmlid = "";
					}
				}
			}
		}
		return htmlid;
	}
}
