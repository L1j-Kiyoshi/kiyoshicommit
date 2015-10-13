package jp.l1j.server.controller.timer;

import java.util.logging.Logger;

import jp.l1j.server.codes.ActionCodes;
import jp.l1j.server.datatables.NpcTable;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.inventory.L1Inventory;
import jp.l1j.server.model.map.L1InstanceMap;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.utils.IdFactory;

public class IceQueenCastleController extends Thread {

	private static Logger _log = Logger.getLogger(IceQueenCastleController.class.getName());

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private short _mapId;

	private L1PcInstance _pc;

	private L1NpcInstance _boss;

	private int _mapType;

	public IceQueenCastleController(L1PcInstance pc, int type) {
		_pc = pc;
		_mapType = type;
		init();
	}


	@Override
	public void run() {
		try {
			Thread.sleep(5000);
			while (!_boss.isDead()) {
				if (!isPlayerThere()) { // PCが死亡・帰還
					_boss.deleteMe();
					pcInventoryItemClear();
					removeObjects();
					L1InstanceMap.getInstance().removeInstanceMap(_mapId);
					break;
				}
				Thread.sleep(1000);
			}

			for (int i = 0; i < 300; i++) { // 300秒後にMAP破棄
				if (!isPlayerThere()) { // PCが死亡・帰還
					_boss.deleteMe();
					pcInventoryItemClear();
					removeObjects();
					L1InstanceMap.getInstance().removeInstanceMap(_mapId);
					break;
				}
				Thread.sleep(1000);
			}

		} catch (Exception e) {
			L1InstanceMap.getInstance().removeInstanceMap(_mapId);
		}
	}

	private void init() {
		// インスタンスダンジョンを生成
		if (_mapType == 1) { // IQ
			_mapId = (short) L1InstanceMap.getInstance().addInstanceMap(2101);
		} else if (_mapType == 2) {
			_mapId = (short) L1InstanceMap.getInstance().addInstanceMap(2151);
		}
		// NPCの配置
		spawnNpc();

		// 第一エリア
		// 第二エリア
		// 第三エリア
		// 第四エリア
		// 最終エリア
		// BOSS召喚
		spawnBoss();
	}

	private void pcInventoryItemClear() {
		if (_pc.getInventory().checkItem(41539)) { // フレイムワンド
			_pc.getInventory().consumeItem(41539);
		}
		if (_pc.getInventory().checkItem(41540)) { // 神秘の回復ポーション
			_pc.getInventory().consumeItem(41540);
		}
	}

	private void removeObjects() {
		for (Object obj : L1World.getInstance().getVisibleObjects(_mapId).values()) {
			if (obj instanceof L1NpcInstance) { // モンスター削除
				L1NpcInstance npc = (L1NpcInstance) obj;
				if (!npc.isDead()) {
					npc.setDead(true);
					npc.setStatus(ActionCodes.ACTION_Die);
					npc.setCurrentHpDirect(0);
					npc.deleteMe();
				}
			} else if (obj instanceof L1Inventory) { // アイテム削除
				L1Inventory inventory = (L1Inventory) obj;
				inventory.clearItems();
			}
		}
	}

	private void spawnNpc() {
		L1NpcInstance npc = NpcTable.getInstance().newNpcInstance(71276); // 象牙の塔の諜報員
		npc.setId(IdFactory.getInstance().nextId());
		npc.setMap(_mapId);
		npc.getLocation().set(32734, 32802, _mapId);
		npc.setHomeX(npc.getX());
		npc.setHomeY(npc.getY());
		npc.setHeading(4);
		npc.setreSpawn(false);
		L1World.getInstance().storeObject(npc);
		L1World.getInstance().addVisibleObject(npc);
		npc.updateLight();
	}

	private void spawnBoss() {
		if (_mapType == 1) { // IQ
			_boss = NpcTable.getInstance().newNpcInstance(46319); // 新アイスクイーン
			_boss.setId(IdFactory.getInstance().nextId());
			_boss.setMap(_mapId);
			_boss.getLocation().set(32840, 32921, _mapId);
		} else if (_mapType == 2) { // ID
			_boss = NpcTable.getInstance().newNpcInstance(46320); // アイスデーモン
			_boss.setId(IdFactory.getInstance().nextId());
			_boss.setMap(_mapId);
			_boss.getLocation().set(32825, 32921, _mapId);
		}
		_boss.setHomeX(_boss.getX());
		_boss.setHomeY(_boss.getY());
		_boss.setHeading(6);
		_boss.setreSpawn(false);
		L1World.getInstance().storeObject(_boss);
		L1World.getInstance().addVisibleObject(_boss);
		_boss.updateLight();
	}

	private boolean isPlayerThere() {
		return _pc.getMapId() == _mapId;
	}

	public short getMapId() {
		return _mapId;
	}
}
