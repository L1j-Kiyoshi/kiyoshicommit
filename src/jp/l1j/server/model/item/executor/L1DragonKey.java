package jp.l1j.server.model.item.executor;

import jp.l1j.server.controller.DragonPortalController;
import jp.l1j.server.model.L1CastleLocation;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_DragonGate;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.utils.L1SpawnUtil;

import static jp.l1j.server.controller.raid.L1RaidId.*;

public class L1DragonKey {

	private static L1DragonKey _instance = null;

	public static synchronized L1DragonKey getInstance() {
		if (_instance == null) {
			_instance = new L1DragonKey();
		}
		return _instance;
	}

	public void useKey(L1PcInstance pc, L1ItemInstance item) {

		if (L1CastleLocation.checkInAllWarArea(pc.getLocation())) { // 戦争中のエリア内の場合
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return;
		}

		if (item.getItemId() == 50501) { // 旧ドラゴンキー
			boolean[] portal = { false, false, false, false };
			if (DragonPortalController.getInstance().getOpenRaid(ANTHARAS_RAID) != 0) {
				portal[0] = true;
			}
			if (DragonPortalController.getInstance().getOpenRaid(FAFURION_RAID) != 0) {
				portal[1] = true;
			}
			if (DragonPortalController.getInstance().getOpenRaid(LINDVIOR_RAID) != 0) {
				portal[2] = true;
			}
/*			else if (DragonPortalController.getInstance().getOpenRaid(VALAKAS_RAID) != 0) {
				portal[3] = true;
			}
*/
			pc.sendPackets(new S_DragonGate(pc, portal));
			pc.getInventory().removeItem(item, 1);
		} else if (41520 <= item.getItemId() && item.getItemId() <= 41523) { // ドラゴンキー （各竜名）
			int openMapId = 0;
			int portalId = 0;
			if (item.getItemId() == 41520) { // アンタラス
				portalId = 91051;
				openMapId = DragonPortalController.getInstance().getOpenRaid(ANTHARAS_RAID);
			} else if (item.getItemId() == 41521) { // パプリオン
				portalId = 91052;
				openMapId = DragonPortalController.getInstance().getOpenRaid(FAFURION_RAID);
			} else if (item.getItemId() == 41522) { // リンドビオル
				portalId = 91053;
				openMapId = DragonPortalController.getInstance().getOpenRaid(LINDVIOR_RAID);
			} else if (item.getItemId() == 41523) { // ヴァラカス
				portalId = 91054;
				openMapId = DragonPortalController.getInstance().getOpenRaid(VALAKAS_RAID);
			}

			if (openMapId != 0) {
				L1SpawnUtil.dragonPortalSpawn(pc, portalId, openMapId, 60 * 60 * 2 * 1000); // 2時間
				for (L1PcInstance listner : L1World.getInstance().getAllPlayers()) {
					if (!listner.getExcludingList().contains(pc.getName())) {
						if (listner.isShowTradeChat() || listner.isShowWorldChat()) {
							listner.sendPackets(new S_ServerMessage(2921));
							// 鋼鉄ギルド ドワーフ：うむ…ドラゴンの鳴き声がここまで聞こえてくる。
							// きっと誰かがドラゴンポータルを開いたに違いない！
							// 準備ができたドラゴン スレイヤーに栄光と祝福を！
						}
					}
				}
				pc.getInventory().removeItem(item, 1);
			} else { // MAPに空きがない場合（あり得ないが一応）
				pc.sendPackets(new S_ServerMessage(79));
				// \f1何も起きませんでした。
			}
		}
	}
}
