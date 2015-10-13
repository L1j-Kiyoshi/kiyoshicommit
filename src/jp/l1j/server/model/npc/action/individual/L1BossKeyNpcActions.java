package jp.l1j.server.model.npc.action.individual;

import java.sql.Timestamp;

import jp.l1j.server.datatables.InnKeyTable;
import jp.l1j.server.datatables.ItemTable;
import jp.l1j.server.datatables.NpcTable;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.map.L1InstanceMap;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1Npc;
import jp.l1j.server.utils.IdFactory;

public class L1BossKeyNpcActions {

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private static L1BossKeyNpcActions _instance = null;

	public static synchronized L1BossKeyNpcActions getInstance() {
		if (_instance == null) {
			_instance = new L1BossKeyNpcActions();
		}
		return _instance;
	}

	public String actions(L1Npc npc, String s, L1PcInstance pc) {
		String htmlid = null;

		if (npc.getNpcId() == 71281) { // 褒賞倉庫管理人^ミアネス

		} else if (npc.getNpcId() == 71282) { // 訓練隊長^カイザー
			if (s.equalsIgnoreCase("1")) { // 訓練所を借りる
				if (pc.getInventory().checkItem(50678)) { // 訓練場のキーを所持
					htmlid = "bosskey6";
				} else {
					int openMapId = 0;
					for (int mapId = 1400; mapId <= 1498; mapId++) {
						if (!InnKeyTable.isUsedMap(mapId)) {
							openMapId = mapId;
							break;
						}
					}
					if (openMapId == 0) { // 開いてる訓練場がない
						htmlid = "bosskey3";
					} else { // 開いている
						htmlid = "bosskey4";
					}
				}
			} else if (s.equalsIgnoreCase("2") || s.equalsIgnoreCase("3") || // 4個・8個・16個
						s.equalsIgnoreCase("4")) {
				int place = 1200;
				int keyCount = 4;
				if (s.equalsIgnoreCase("3")) {
					place = 2400;
					keyCount = 8;
				} else if (s.equalsIgnoreCase("4")) {
					place = 4800;
					keyCount = 16;
				}
				if (pc.getInventory().checkItem(40308, place)) {
					int openMapId = 0;
					for (int mapId = 1400; mapId <= 1498; mapId++) {
						if (!InnKeyTable.isUsedMap(mapId)) {
							openMapId = mapId;
							break;
						}
					}
					if (openMapId != 0) { // 念のためここでもチェック
						for (int i = 0; i < keyCount; i++) {
							L1ItemInstance item = ItemTable.getInstance().createItem(50678);
							item.setKeyId(openMapId);
							item.setInnNpcId(71282);
							Timestamp ts = new Timestamp(System.currentTimeMillis() + (60 * 60 * 2 * 1000));
							item.setDueTime(ts);
							item.setExpirationTime(ts);
							InnKeyTable.storeKey(item);
							pc.getInventory().storeItem(item);
							pc.getInventory().consumeItem(40308, place);
							if (openMapId > 1400) {
								L1InstanceMap.getInstance().addInstanceMap(1400, openMapId);
							}
							htmlid = "";
						}
					} else {
						htmlid = "bosskey3";
					}
				} else {
					htmlid = "bosskey5"; // 利用料金不足
				}
			} else if (s.equalsIgnoreCase("6")) { // 訓練所に入る
				if (!pc.getInventory().checkItem(50678)) { // 訓練場のキー
					htmlid = "bosskey2"; // キー未所持
				} else {
					int x = 32898 + _random.nextInt(4);
					int y = 32815 + _random.nextInt(6);
					L1ItemInstance item = pc.getInventory().findItemId(50678);
					short mapId = (short) item.getKeyId();
					spawnNpc(mapId);
					L1Teleport.teleport(pc, x, y, mapId, pc.getHeading(), true);
				}
			}
		} else if (npc.getNpcId() == 71283) { // 訓練副官^セシリア
			if (s.equalsIgnoreCase("A")) { // ハーディンの分身
				if (pc.getInventory().checkItem(50679, 1)) {
					pc.getInventory().consumeItem(50679, 1);
					spawnBoss(46196, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("B")) { // ダークウィザード
				if (pc.getInventory().checkItem(50680, 1)) {
					pc.getInventory().consumeItem(50680, 1);
					spawnBoss(46182, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("C")) { // デーモン
				if (pc.getInventory().checkItem(50681, 1)) {
					pc.getInventory().consumeItem(50681, 1);
					spawnBoss(45649, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("D")) { // 堕落(象牙の塔)
			} else if (s.equalsIgnoreCase("E")) { // ケイナ
				if (pc.getInventory().checkItem(50683, 1)) {
					pc.getInventory().consumeItem(50683, 1);
					spawnBoss(46498, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("F")) { // イデア
				if (pc.getInventory().checkItem(50684, 1)) {
					pc.getInventory().consumeItem(50684, 1);
					spawnBoss(46502, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("G")) { // ビアタス
				if (pc.getInventory().checkItem(50685, 1)) {
					pc.getInventory().consumeItem(50685, 1);
					spawnBoss(46499, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("H")) { // バロメス
				if (pc.getInventory().checkItem(50686, 1)) {
					pc.getInventory().consumeItem(50686, 1);
					spawnBoss(46500, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("I")) { // ティアメス
				if (pc.getInventory().checkItem(50687, 1)) {
					pc.getInventory().consumeItem(50687, 1);
					spawnBoss(46503, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("J")) { // エンディアス
				if (pc.getInventory().checkItem(50688, 1)) {
					pc.getInventory().consumeItem(50688, 1);
					spawnBoss(46501, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("K")) { // ラミアス
				if (pc.getInventory().checkItem(50689, 1)) {
					pc.getInventory().consumeItem(50689, 1);
					spawnBoss(46504, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("L")) { // バロード
				if (pc.getInventory().checkItem(50690, 1)) {
					pc.getInventory().consumeItem(50690, 1);
					spawnBoss(46505, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("M")) { // ヘルバイン
				if (pc.getInventory().checkItem(50691, 1)) {
					pc.getInventory().consumeItem(50691, 1);
					spawnBoss(46497, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("N")) { // ライア
				if (pc.getInventory().checkItem(50692, 1)) {
					pc.getInventory().consumeItem(50692, 1);
					spawnBoss(46494, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("O")) { // バランカ
				if (pc.getInventory().checkItem(50693, 1)) {
					pc.getInventory().consumeItem(50693, 1);
					spawnBoss(46491, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("P")) { // スレイブ
				if (pc.getInventory().checkItem(50694, 1)) {
					pc.getInventory().consumeItem(50694, 1);
					spawnBoss(46488, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("Q")) { // ネクロマンサー
				if (pc.getInventory().checkItem(50695, 1)) {
					pc.getInventory().consumeItem(50695, 1);
					spawnBoss(45456, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			} else if (s.equalsIgnoreCase("S")) { // デスナイト
				if (pc.getInventory().checkItem(50696, 1)) {
					pc.getInventory().consumeItem(50696, 1);
					spawnBoss(45601, pc.getMapId());
					htmlid = "";
				} else {
					htmlid = "bosskey10";
				}
			}
		}

		return htmlid;
	}

	private void spawnBoss(int bossId, short mapId) {
		L1NpcInstance boss = NpcTable.getInstance().newNpcInstance(bossId);
		boss.setId(IdFactory.getInstance().nextId());
		boss.setMap(mapId);
		boss.getLocation().set(32878, 32816, mapId);
		boss.setHomeX(boss.getX());
		boss.setHomeY(boss.getY());
		boss.setHeading(6);
		boss.setreSpawn(false);
		L1World.getInstance().storeObject(boss);
		L1World.getInstance().addVisibleObject(boss);
		boss.updateLight();
	}

	private void spawnNpc(short mapId) {
		L1NpcInstance npc = NpcTable.getInstance().newNpcInstance(71283); // 訓練副官^セシリア
		npc.setId(IdFactory.getInstance().nextId());
		npc.setMap(mapId);
		npc.getLocation().set(32902, 32818, mapId);
		npc.setHomeX(npc.getX());
		npc.setHomeY(npc.getY());
		npc.setHeading(6);
		npc.setreSpawn(false);
		L1World.getInstance().storeObject(npc);
		L1World.getInstance().addVisibleObject(npc);
		npc.updateLight();
	}
}
