package jp.l1j.server.controller.raid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.configure.Config;
import jp.l1j.server.controller.timer.DistortionOfTimeController;
import jp.l1j.server.datatables.DistortionTable;
import jp.l1j.server.datatables.ItemTable;
import jp.l1j.server.datatables.NpcTable;
import jp.l1j.server.datatables.RaidConfigTable;
import jp.l1j.server.datatables.RaidDropTable;
import jp.l1j.server.model.L1Location;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.inventory.L1Inventory;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.packets.server.S_SystemMessage;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1Item;
import jp.l1j.server.templates.L1RaidConfig;
import jp.l1j.server.templates.L1RaidDrop;
import jp.l1j.server.utils.IdFactory;

import static jp.l1j.server.controller.raid.L1RaidId.*;

public class KukulcanRaidController extends Thread {

	private static Logger _log = Logger.getLogger(KukulcanRaidController.class.getName());

	private static KukulcanRaidController _instance = null;

	public static synchronized KukulcanRaidController getInstance() {
		if (_instance == null) {
			_instance = new KukulcanRaidController();
		}
		return _instance;
	}

	private RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private L1RaidConfig _raidConfig = null;
	private boolean _isActive = false;
	private boolean _isEnterTime = false;
	private final int RAID_OPEN_TIME = Config.DISTORTION_RAID_OPEN_TIME; // 歪みが開いてから祭壇入場までの時間(分)
	private final int RAID_BOSS_LIMIT = Config.DISTORTION_RAID_BOSS_LIMIT; // 祭壇入場からボスを討伐するまでの猶予時間(分)
	private final int DISTORTION_EXTEND_TIME = Config.DISTORTION_EXTEND_TIME; // レイド討伐成功時歪みを延長する時間(分)

	private int MAX_DROP_PATTERN = 1;

	private List<L1PcInstance> _playerList = Collections.synchronizedList(new ArrayList<L1PcInstance>());
	private ArrayList<L1RaidDrop> _allDropList = new ArrayList<L1RaidDrop>();
	private ArrayList<L1RaidDrop> _dropList; // 今回確定したパターンしか格納されないドロップ配列
	private ArrayList<Integer> _dropNumber = new ArrayList<Integer>();

	private L1NpcInstance _boss1;
	private L1NpcInstance _boss2;

	private KukulcanRaidController() {
		_raidConfig = RaidConfigTable.getInstance().getRaidConfig(KUKULCAN_RAID);
		_allDropList = RaidDropTable.getInstance().getRaidDrop(KUKULCAN_RAID);
		MAX_DROP_PATTERN = RaidDropTable.getInstance().getMaxDropPattern(KUKULCAN_RAID);
	}

	// 初期設定各種

	private void setDrop() {
		int dropPattern = 1;
		if (MAX_DROP_PATTERN > dropPattern) {
			dropPattern = _random.nextInt(MAX_DROP_PATTERN) + 1;
		}
		_dropList = new ArrayList<L1RaidDrop>();
		for (L1RaidDrop rd : _allDropList) {
			if (rd.getPatternId() == dropPattern) {
				_dropList.add(rd);
			}
		}

		for (int i = 1; i <= _raidConfig.getMaxPlayer(); i++) {
			_dropNumber.add(i);
		}

		Collections.shuffle(_dropNumber);
		Collections.shuffle(_dropList);
	}

	// メイン処理

	@Override
	public void run() {
		try {
			_isActive = true;
			setDrop();
			for (int raidOpenTime = RAID_OPEN_TIME; raidOpenTime > 0; raidOpenTime--) {
				Thread.sleep(60000);
			}
			_isEnterTime = true;
			Thread.sleep(60000);
			_isEnterTime = false; // 入れなくなる？不明だが一応ロック。
			checkPlayerList();
			for (L1PcInstance pc : _playerList) {
				// ククルカン： 恐れのない者だな。ここに入ってくるなんて！ ゼブ レクイ！ 目覚めろ！
				pc.sendPackets(new S_ServerMessage(1485));
			}
			Thread.sleep(10000);
			checkPlayerList();
			for (L1PcInstance pc : _playerList) {
				// ゼブ レクイ： すうううう…すうううう…
				pc.sendPackets(new S_ServerMessage(1486));
			}
			Thread.sleep(10000);
			checkPlayerList();
			for (L1PcInstance pc : _playerList) {
				// ゼブ レクイ： ふぃぃぃぃ…ふぃぃぃぃ…
				pc.sendPackets(new S_ServerMessage(1487));
			}
			Thread.sleep(10000);
			spawnZeblekui();

			boolean isBossEnd = false;
			int raidBossLimit = RAID_BOSS_LIMIT * 60;
			while (!isBossEnd && raidBossLimit > 0) {
				if (_boss1.isDead() && _boss2.isDead()) {
					isBossEnd = true;
					break;
				}
				if (_playerList.size() < 1) {
					break;
				}
				Thread.sleep(1000);
				checkPlayerList();
				raidBossLimit--;
			}

			if (isBossEnd) { // ボス討伐に成功した
				checkPlayerList();
				for (L1PcInstance pc : _playerList) {
					// ククルカン： なんということだ！ 我々の負けだ。
					pc.sendPackets(new S_ServerMessage(1488));
				}
				Thread.sleep(5000);
				checkPlayerList();
				for (L1PcInstance pc : _playerList) {
					// ククルカン： これから1日間ティカル寺院を解放してやろう。
					pc.sendPackets(new S_ServerMessage(1489));
				}
				Thread.sleep(5000);
				checkPlayerList();
				storeDrop(); // ドロップを配布
				Thread.sleep(5000);
				checkPlayerList();
				// 時の歪みの力が弱まっています。 異界の空間は1日間存在します。
				L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1470));
				Calendar closeTime = Calendar.getInstance();
				closeTime.add(Calendar.MINUTE, DISTORTION_EXTEND_TIME); // 歪み時間を延長
				DistortionOfTimeController.getInstance().setCloseTime(closeTime);
				DistortionTable.getInstance().saveDistortion();
				checkPlayerList();
				for (L1PcInstance pc : _playerList) {
					// システムメッセージ： 30秒後にテレポートします。
					pc.sendPackets(new S_ServerMessage(1476));
				}
				Thread.sleep(10000);
				checkPlayerList();
				for (L1PcInstance pc : _playerList) {
					// システムメッセージ： 20秒後にテレポートします。
					pc.sendPackets(new S_ServerMessage(1477));
				}
				Thread.sleep(10000);
				checkPlayerList();
				for (L1PcInstance pc : _playerList) {
					// システムメッセージ：10秒後にテレポートします。
					pc.sendPackets(new S_ServerMessage(1478));
				}
				Thread.sleep(10000);
			} else { // 討伐に失敗した
				DistortionOfTimeController.getInstance().closeDistortion();
				if (!_boss1.isDead()) {
					_boss1.deleteMe();
				}
				if (!_boss2.isDead()) {
					_boss2.deleteMe();
				}
				for (L1PcInstance pc : _playerList) {
					// ククルカン： 貴様らの無謀な勇気と愚かさを覚えておいてやろう！
					pc.sendPackets(new S_ServerMessage(1490));
				}
				Thread.sleep(5000);
				for (int messageId = 1480; messageId <= 1484; messageId++) {
					for (L1PcInstance pc : _playerList) {
						// システムメッセージ： 5~1秒後にテレポートします。
						pc.sendPackets(new S_ServerMessage(messageId));
						Thread.sleep(1000);
					}
				}
			}
			checkPlayerList();
			L1Location loc = new L1Location(32793, 32753, 783);
			for (L1PcInstance pc : _playerList) {
				L1Teleport.teleport(pc, loc, pc.getHeading(), true);
			}
			_isActive = false;
			refresh();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			for (L1PcInstance pc : _playerList) {
				pc.sendPackets(new S_SystemMessage("システムエラーの為、10秒後にテレポートします。"));
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				_log.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
			}
			checkPlayerList();
			L1Location loc = new L1Location(32626, 32900, 780);
			for (L1PcInstance pc : _playerList) {
				L1Teleport.teleport(pc, loc, pc.getHeading(), true);
			}
			_isActive = false;
			refresh();
		}
	}

	private void storeDrop() {
		Collections.shuffle(_playerList);
		HashMap<Integer, L1PcInstance> _playerMapList = new HashMap<Integer, L1PcInstance>();

		for (int i = 1; i <= _playerList.size(); i++) {
			_playerMapList.put(_dropNumber.get(i - 1), _playerList.get(i - 1));
		}

		int min;
		int max;
		int count;
		L1ItemInstance item = null;
		L1PcInstance pc = null;

		for (L1RaidDrop drop : _dropList) {
			if (_playerMapList.get(drop.getSetId()) == null) {
				continue;
			}
			pc = _playerMapList.get(drop.getSetId());

			min = drop.getMinCount();
			max = drop.getMaxCount();
			count = min;
			if (min != max) {
				count = _random.nextInt(max - min) + min;
			}
			L1Item temp = ItemTable.getInstance().getTemplate(drop.getItemId());
			if (temp != null) {
				if (temp.isStackable()) {
					item = ItemTable.getInstance().createItem(drop.getItemId());
					item.setEnchantLevel(0);
					item.setCount(count);
					item.setIdentified(false);
					if (pc.getInventory().checkAddItem(item, count) == L1Inventory.OK) {
						pc.getInventory().storeItem(item);
					}
				} else {
					int createCount;
					for (createCount = 0; createCount < count; createCount++) {
						item = ItemTable.getInstance().createItem(drop.getItemId());
						item.setEnchantLevel(0);
						item.setIdentified(false);
						if (pc.getInventory().checkAddItem(item, 1) == L1Inventory.OK) {
							pc.getInventory().storeItem(item);
							if (item.getItem().getType2() == 1
									|| item.getItem().getType2() == 2) {
								item.setIsHaste(item.getItem().isHaste());
								if (item.getItem().getType2() == 1) {
									item.setCanBeDmg(item.getItem().getCanbeDmg());
								}
								item.save();
							}
						} else {
							break;
						}
					}
				}
			}
			for (L1PcInstance pclist : _playerList) {
				pclist.sendPackets(new S_ServerMessage(813, "$8319", item.getLogName(), pc.getName()));
			}
		}
	}

	private void spawnZeblekui() {
		_boss1 = NpcTable.getInstance().newNpcInstance(90518); // ゼブレクイ(右側)
		_boss1.setId(IdFactory.getInstance().nextId());
		_boss1.setMap((short) 784);
		_boss1.getLocation().set(32751, 32871, 784);
		_boss1.setHomeX(_boss1.getX());
		_boss1.setHomeY(_boss1.getY());
		_boss1.setHeading(6);
		L1World.getInstance().storeObject(_boss1);
		L1World.getInstance().addVisibleObject(_boss1);
		_boss1.updateLight();

		_boss2 = NpcTable.getInstance().newNpcInstance(90519); // ゼブレイク(左側)
		_boss2.setId(IdFactory.getInstance().nextId());
		_boss2.setMap((short) 784);
		_boss2.getLocation().set(32750, 32859, 784);
		_boss2.setHomeX(_boss2.getX());
		_boss2.setHomeY(_boss2.getY());
		_boss2.setHeading(6);
		L1World.getInstance().storeObject(_boss2);
		L1World.getInstance().addVisibleObject(_boss2);
		_boss2.updateLight();
	}

	private void checkPlayerList() {
		for (L1PcInstance pc : _playerList) {
			if (pc.getMapId() != 784) { // 祭壇にはいない
				_playerList.remove(pc);
			}
		}
	}

	// 後処理
	private void refresh() {
		_playerList.clear();
		_boss1 = null;
		_boss2 = null;
		_isActive = false;

	}

	public List<L1PcInstance> getPlayerList() {
		return _playerList;
	}

	public synchronized void addPlayerList(L1PcInstance pc) {
		if (!_playerList.contains(pc)) {
			_playerList.add(pc);
		}
	}

	public boolean isActive() {
		return _isActive;
	}

	public boolean isEnterTime() {
		return _isEnterTime;
	}

	public L1RaidConfig getRaidConfig() {
		return _raidConfig;
	}

}
