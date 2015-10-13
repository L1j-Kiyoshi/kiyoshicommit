package jp.l1j.server.model.npc.action.individual;

import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_NpcTalkReturn;
import jp.l1j.server.templates.L1Npc;

public class L1HosterityWeaponNpcActions extends L1NpcMakeItemUtils {

	private static L1HosterityWeaponNpcActions _instance = null;

	public static synchronized L1HosterityWeaponNpcActions getInstance() {
		if (_instance == null) {
			_instance = new L1HosterityWeaponNpcActions();
		}
		return _instance;
	}

	public String actions(int objid, L1Npc npc, String s, L1PcInstance pc) {
		String htmlid = null;

		_objid = objid;
		if (s.equalsIgnoreCase("a")) { // ソード変換石を購入する
			_materials = new int[] { 40308 };
			_counts = new int[] { 8000000 };
			_createitem = new int[] { 41504 };
			_createcount = new int[] { 1 };
			_success_htmlid = "jp_wp_swap2";
			_failure_htmlid = "jp_wp_swap3";
		} else if (s.equalsIgnoreCase("b")) { // ボウ変換石を購入する
			_materials = new int[] { 40308 };
			_counts = new int[] { 8000000 };
			_createitem = new int[] { 41505 };
			_createcount = new int[] { 1 };
			_success_htmlid = "jp_wp_swap2";
			_failure_htmlid = "jp_wp_swap3";
		} else if (s.equalsIgnoreCase("c")) { // スタッフ変換石を購入する
			_materials = new int[] { 40308 };
			_counts = new int[] { 8000000 };
			_createitem = new int[] { 41506 };
			_createcount = new int[] { 1 };
			_success_htmlid = "jp_wp_swap2";
			_failure_htmlid = "jp_wp_swap3";
		} else if (s.equalsIgnoreCase("d")) { // クロウ変換石を購入する
			_materials = new int[] { 40308 };
			_counts = new int[] { 8000000 };
			_createitem = new int[] { 41507 };
			_createcount = new int[] { 1 };
			_success_htmlid = "jp_wp_swap2";
			_failure_htmlid = "jp_wp_swap3";
		} else if (s.equalsIgnoreCase("e")) { // チェーンソード変換石を購入する
			_materials = new int[] { 40308 };
			_counts = new int[] { 8000000 };
			_createitem = new int[] { 41508 };
			_createcount = new int[] { 1 };
			_success_htmlid = "jp_wp_swap2";
			_failure_htmlid = "jp_wp_swap3";
		} else if (s.equalsIgnoreCase("f")) { // キーリンク変換石を購入する
			_materials = new int[] { 40308 };
			_counts = new int[] { 8000000 };
			_createitem = new int[] { 41509 };
			_createcount = new int[] { 1 };
			_success_htmlid = "jp_wp_swap2";
			_failure_htmlid = "jp_wp_swap3";
		}

		if (isCreate(pc)) {
			createItems(npc, pc);
		} else { // 生成失敗
			if (_failure_htmlid != null) { // html指定がある場合は表示
				pc.sendPackets(new S_NpcTalkReturn(objid, _failure_htmlid, _htmldata));
			}
		}

		return htmlid;
	}

}
