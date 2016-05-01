package jp.l1j.server.model.npc.action.individual;

import static jp.l1j.server.model.skill.L1SkillId.*;
import jp.l1j.configure.Config;
import jp.l1j.server.datatables.ExpTable;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.skill.L1BuffUtil;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1Npc;
import jp.l1j.server.utils.CalcExp;

public class L1HinomotoNpcActions {

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private static L1HinomotoNpcActions _instance = null;

	public static synchronized L1HinomotoNpcActions getInstance() {
		if (_instance == null) {
			_instance = new L1HinomotoNpcActions();
		}
		return _instance;
	}

	public String actions(L1Npc npc, String s, L1PcInstance pc, L1Object obj) {
		String htmlid = null;
		int npcid = npc.getNpcId();

		if (npcid == 46293) { // 佐ノ吉(ギラン → 惣構え)
			if (s.equalsIgnoreCase("teleport jp yamato p1")) {
				L1Teleport.teleport(pc, 32814, 32806, (short) 8000, 5, true);
				htmlid = "";
			}
		} else if (npcid == 46295) { // 虎助
			if (s.equalsIgnoreCase("a")) {
				if (pc.getLevel() < 45) {
					htmlid = "jp_takesif1";
				} else if (pc.getLevel() > 54) {
					htmlid = "jp_takesif2";
				} else {
					if (pc.getInventory().checkItem(41528, 1)
							|| pc.getAdditionalWarehouseInventory().checkItem(41528, 1)) { // 武士の心得
						htmlid = "jp_takesiff";
					} else {
						if (pc.getInventory().checkItem(41527, 100)) { //封印された妖怪の魂
							pc.getInventory().consumeItem(41527, 100);
							L1BuffUtil.effectBlessOfComa(pc, BLESS_OF_SAMURAI, 3600, 7612); // 武士の心得
							L1NpcInstance npc2 = (L1NpcInstance) obj;
							L1ItemInstance item = pc.getInventory().storeItem(41528, 1); // 武士の心得
							String npcName = npc2.getNpcTemplate().getName();
							String itemName = item.getItem().getName();
							pc.sendPackets(new S_ServerMessage(143, npcName, itemName));
							htmlid = "jp_takesi2";
						} else {
							htmlid = "jp_takesif3";
						}
					}
				}
			}
		}
		// 小六郎
		else if (npcid == 46296) {
			if (s.equalsIgnoreCase("a")) {
				if (pc.getLevel() < 49) {
					htmlid = "jp_kazukif1";
				} else if (pc.getLevel() > 54) {
					htmlid = "jp_kazukif2";
				} else {
					if (pc.getInventory().checkItem(41529, 1)
							|| pc.getAdditionalWarehouseInventory().checkItem(41529, 1)) { // 癒毒の巻物
						htmlid = "jp_kazukiff";
					} else {
						if (pc.getInventory().checkItem(41526, 1)) { //牛鬼の毒
							pc.getInventory().consumeItem(41526, 1);
							int addExp = CalcExp.calcPercentageExp(49, 5);
							pc.addExp(addExp); // 49換算で5%
							L1NpcInstance npc2 = (L1NpcInstance) obj;
							L1ItemInstance item = pc.getInventory().storeItem(41529, 1); // 癒毒の巻物
							String npcName = npc2.getNpcTemplate().getName();
							String itemName = item.getItem().getName();
							pc.sendPackets(new S_ServerMessage(143, npcName, itemName));
							htmlid = "jp_kazuki2";
						} else {
							htmlid = "jp_kazukif3";
						}
					}
				}
			}
		}
		// 万吉(惣構え → ギラン)
		else if (npcid == 46297) {
			if (s.equalsIgnoreCase("a")) {
				L1Teleport.teleport(pc, 33438, 32796, (short) 4, 5, true);
				htmlid = "";
			}
		}
		// 日ノ本の特典女将
		else if (npcid == 46299) {
			if (s.equalsIgnoreCase("a")) {
				if (pc.getInventory().checkItem(41525, 1)) { // 熱い勇士の汗血
					pc.getInventory().consumeItem(41525, 1);
					pc.addExp(ExpTable.getNeedExpNextLevel(Config.GIVE_EXP_LEVEL)
							/ ExpTable.getExpRate(pc.getLevel()));
					htmlid = "jp_hinowms";
				} else {
					htmlid = "jp_hinowmf";
				}
			}
		}if (npcid == 71280) {
			int newX = 32932 + _random.nextInt(6);
			int newY = 32864 + _random.nextInt(6);
			if (s.equalsIgnoreCase("a")) { // 西の丸へ進む
				L1Teleport.teleport(pc, newX, newY, (short) 8001, 5, true);
			} else if (s.equalsIgnoreCase("b")) { // 東の丸へ進む
				L1Teleport.teleport(pc, newX, newY, (short) 8002, 5, true);
			} else if (s.equalsIgnoreCase("c")) { // 北の丸へ進む
				L1Teleport.teleport(pc, newX, newY, (short) 8003, 5, true);
			}
		} else if (npcid == 46524) { // 左之助(二の丸入口)
			if (s.equalsIgnoreCase("a")) { // こんな割れた器でよければ、どうぞ
				if (pc.getInventory().checkItem(41551, 10)) {
					pc.getInventory().consumeItem(41551, 10);
					htmlid = "jp_sanojo1_1";
				} else {
					htmlid = "jp_sanojo1_2";
				}
			} else if (s.equalsIgnoreCase("b")) { // 割れた器の何がいいのですか？
				htmlid = "jp_sanojo1_3";
			}
		} else if (npcid == 46525) { // 名も無き魂(二の丸)
			if (s.equalsIgnoreCase("a")) { // がしゃどくろの魂の欠片を渡す
				if (60 <= pc.getLevel() && pc.getLevel() <= 64) {
					if (pc.getInventory().checkItem(41552)) { // がしゃどくろの魂の欠片
						pc.getInventory().consumeItem(41552, 1);
						int addExp = CalcExp.calcPercentageExp(49, 3); // 49換算3%
						double levelPenalty = ExpTable.getExpRate(pc.getLevel());
						pc.addExp((int) (addExp / levelPenalty));
						htmlid = "jp_noname1a";
					} else {
						htmlid = "jp_noname1b";
					}
				}
			} else if (s.equalsIgnoreCase("b")) { //士魂とはなんですか？
				htmlid = "jp_noname1c";
			}
		} else if (npcid == 46526) { // 弥九郎(日ノ本入口)
			if (s.equalsIgnoreCase("a")) { // 持っている砂金をすべて渡す
				if (pc.getInventory().checkItem(41547)) {
					L1ItemInstance item = pc.getInventory().getItemByItemId(41547);
					if (item != null) {
						int adena = item.getCount() * 1000;
						pc.getInventory().removeItem(item);
						pc.getInventory().storeItem(40308, adena);
						pc.sendPackets(new S_ServerMessage(403, "アデナ(" + adena + ")"));
						htmlid = "jp_yakuro6";
					}
				} else {
					htmlid = "jp_yakuro5";
				}
			} else if (s.equalsIgnoreCase("b")) { // 持っている棒金をすべて渡す
				if (pc.getInventory().checkItem(41548)) {
					L1ItemInstance item = pc.getInventory().getItemByItemId(41548);
					if (item != null) {
						int adena = item.getCount() * 10000;
						pc.getInventory().removeItem(item);
						pc.getInventory().storeItem(40308, adena);
						pc.sendPackets(new S_ServerMessage(403, "アデナ(" + adena + ")"));
						htmlid = "jp_yakuro6";
					}
				} else {
					htmlid = "jp_yakuro5";
				}
			} else if (s.equalsIgnoreCase("c")) { // 持っている古織焼きをすべて渡す
				if (pc.getInventory().checkItem(41550)) {
					L1ItemInstance item = pc.getInventory().getItemByItemId(41550);
					if (item != null) {
						int adena = item.getCount() * 150000;
						pc.getInventory().removeItem(item);
						pc.getInventory().storeItem(40308, adena);
						pc.sendPackets(new S_ServerMessage(403, "アデナ(" + adena + ")"));
						htmlid = "jp_yakuro6";
					}
				} else {
					htmlid = "jp_yakuro5";
				}
			}
		}

		return htmlid;
	}
}
