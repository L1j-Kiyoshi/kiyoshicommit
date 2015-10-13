package jp.l1j.server.model.npc.action.individual;

import jp.l1j.server.datatables.ItemTable;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_NpcTalkReturn;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.templates.L1Item;
import jp.l1j.server.templates.L1Npc;

/**
 * アイテム生成系NPCのActionの上位クラス
 * アイテム生成に必要なメソッドやフィールドが用意されている。
 */
public class L1NpcMakeItemUtils {

	protected int _objid;

	protected int[] _materials = null;
	protected int[] _counts = null;
	protected int[] _createitem = null;
	protected int[] _createcount = null;
	protected String _success_htmlid = null;
	protected String _failure_htmlid = null;

	protected String[] _htmldata = null;

	protected boolean isCreate(L1PcInstance pc) {
		if (_createitem == null) { // アイテム精製
			return false;
		}
		boolean isCreate = true;
		if (_materials != null) {
			for (int j = 0; j < _materials.length; j++) {
				if (!pc.getInventory().checkItemNotEquipped(_materials[j], _counts[j])) {
					L1Item temp = ItemTable.getInstance().getTemplate(_materials[j]);
					pc.sendPackets(new S_ServerMessage(337, temp.getName()));
					// \f1%0が不足しています。
					isCreate = false;
				}
			}
		}
		return isCreate;
	}

	protected boolean createItems(L1Npc npc, L1PcInstance pc) {
		// 容量と重量の計算
		int create_count = 0; // アイテムの個数（纏まる物は1個）
		int create_weight = 0;
		for (int k = 0; k < _createitem.length; k++) {
			L1Item temp = ItemTable.getInstance().getTemplate(_createitem[k]);
			if (temp.isStackable()) {
				if (!pc.getInventory().checkItem(_createitem[k])) {
					create_count += 1;
				}
			} else {
				create_count += _createcount[k];
			}
			create_weight += temp.getWeight() * _createcount[k] / 1000;
		}
		// 容量確認
		if (pc.getInventory().getSize() + create_count > 180) {
			pc.sendPackets(new S_ServerMessage(263));
			// \f1一人のキャラクターが持って歩けるアイテムは最大180個までです。
			return false;
		}
		// 重量確認
		if (pc.getMaxWeight() < pc.getInventory().getWeight() + create_weight) {
			pc.sendPackets(new S_ServerMessage(82));
			// アイテムが重すぎて、これ以上持てません。
			return false;
		}

		if (_materials != null) {
			for (int j = 0; j < _materials.length; j++) {
				// 材料消費
				pc.getInventory().consumeItem(_materials[j], _counts[j]);
			}
		}
		for (int k = 0; k < _createitem.length; k++) {
			L1ItemInstance item = pc.getInventory().storeItem(_createitem[k], _createcount[k]);
			if (item != null) {
				String itemName = ItemTable.getInstance().getTemplate(_createitem[k]).getName();
				String createrName = "";
				if (npc != null) {
					createrName = npc.getName();
				}
				if (_createcount[k] > 1) {
					pc.sendPackets(new S_ServerMessage(143, // \f1%0が%1をくれました 。
							createrName, itemName + " (" + _createcount[k] + ")"));
				} else {
					pc.sendPackets(new S_ServerMessage(143, createrName, itemName));
					// \f1%0が%1をくれました。
				}
			}
		}
		if (_success_htmlid != null) { // html指定がある場合は表示
			pc.sendPackets(new S_NpcTalkReturn(_objid, _success_htmlid, _htmldata));
		}
		return true;
	}
}
