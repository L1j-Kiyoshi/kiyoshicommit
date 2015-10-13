package jp.l1j.server.controller.raid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.configure.Config;
import jp.l1j.server.codes.ActionCodes;
import jp.l1j.server.datatables.ItemTable;
import jp.l1j.server.datatables.NpcTable;
import jp.l1j.server.datatables.RaidConfigTable;
import jp.l1j.server.datatables.RaidDropTable;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.inventory.L1Inventory;
import jp.l1j.server.model.skill.L1BuffUtil;
import jp.l1j.server.packets.server.S_DoActionGFX;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1Item;
import jp.l1j.server.templates.L1RaidDrop;
import jp.l1j.server.utils.IdFactory;

import static jp.l1j.server.controller.raid.L1RaidId.*;

public class AntharasRaid extends DragonRaid {

	private static Logger _log = Logger.getLogger(AntharasRaid.class.getName());

	public AntharasRaid(int mapid) {
		super();
		_mapId = mapid;
		_raidConfig = RaidConfigTable.getInstance().getRaidConfig(ANTHARAS_RAID);
		_allDropList = RaidDropTable.getInstance().getRaidDrop(ANTHARAS_RAID);
		MAX_DROP_PATTERN = RaidDropTable.getInstance().getMaxDropPattern(ANTHARAS_RAID);
		setDrop();
	}

	// 初期設定各種

	private void setDrop() {
		if (MAX_DROP_PATTERN <= 0) {
			return;
		}
		int dropPattern = 1;
		if (MAX_DROP_PATTERN > dropPattern) {
			dropPattern = _random.nextInt(MAX_DROP_PATTERN) + 1;
		}
		_dropList = new ArrayList<L1RaidDrop>();
		RandomGenerator random = RandomGeneratorFactory.getSharedRandom();
		int randomChance = 0;
		for (L1RaidDrop rd : _allDropList) {
			if (rd.getPatternId() == dropPattern) {
				if (rd.getDropChance() != 1000000) { // 100%ドロップではないもの。
					randomChance = random.nextInt(0xf4240) + 1;
					if (rd.getDropChance() < randomChance) {
						continue;
					}
				}
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
			_isCompletedRun = true;
			_isActive = true;
			for (int i = 0; i < 60; i++) {
				Thread.sleep(1000);
				checkedPlayer();
			}
			_isEnter = false;
			boolean isNextStage = false;

			// 第一ラウンド
			_isDragonAwake = true;
			sendMessage(1);
			spawnDragon(1);

			while (true) {
				if (_portal._destroyed) { // ポータルが閉まった場合
					portalDestroyed();
					break;
				}
				if (_dragonSlayers.size() < 1) { // プレイヤーが全員いない
					break;
				}
				if (_dragon.isDead()) { // 龍を倒した
					isNextStage = true;
					break;
				}
				Thread.sleep(1000);
				checkedPlayer();
			}

			if (!isNextStage) {
				refresh();
				return;
			}

			storeDrop(1);
			isNextStage = false;
			sendMessage(2);
			spawnDragon(2);

			while (true) {
				if (_portal._destroyed) { // ポータルが閉まった場合
					portalDestroyed();
					break;
				}
				if (_dragonSlayers.size() < 1) { // プレイヤーが全員いない
					break;
				}
				if (_dragon.isDead()) { // 龍を倒した
					isNextStage = true;
					break;
				}
				Thread.sleep(1000);
				checkedPlayer();
			}

			if (!isNextStage) {
				refresh();
				return;
			}

			storeDrop(2);
			isNextStage = false;
			sendMessage(3);
			spawnDragon(3);

			while (true) {
				if (_portal._destroyed) { // ポータルが閉まった場合
					portalDestroyed();
					break;
				}
				if (_dragonSlayers.size() < 1) { // プレイヤーが全員いない
					break;
				}
				if (_dragon.isDead()) { // 龍を倒した
					isNextStage = true;
					break;
				}
				Thread.sleep(1000);
				checkedPlayer();
			}

			if (!isNextStage) {
				refresh();
				return;
			}

			addAntharasBloodstain();
			sendMessage(4);
			storeDrop(3);

			for (int msgid = 1476; msgid <= 1478; msgid++) {
				for (L1PcInstance pc : _dragonSlayers) {
					pc.sendPackets(new S_ServerMessage(msgid));
				}
				Thread.sleep(10000);
			}
			checkedPlayer();
			for (L1PcInstance pc : _allPlayerList) {
				L1Teleport.teleport(pc, _restartLocation[_random.nextInt(_restartLocation.length)],
																				pc.getHeading(), true);
			}
			_isActive = false;
			_isPortalOpen = false;
			_portal.deleteMe();
		} catch(Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private void storeDrop(int round) {
		checkedPlayer();
		if (round == 1 || round == 2) {
			L1ItemInstance item = null;
			for (L1PcInstance pc : _dragonSlayers) {
				if (isDragonSlayer(pc)) {
					item = pc.getInventory().storeItem(50556, 1); // 地竜の証
				}
				for (L1PcInstance pclist : _dragonSlayers) {
					if (isDragonSlayer(pclist)) {
						pclist.sendPackets(new S_ServerMessage(813, _dragon.getNameId(),
															item.getName(), pc.getName()));
					}
				}
			}
			for (L1PcInstance pc : _dragonSlayers) {
				if (isDragonSlayer(pc)) {
					item = pc.getInventory().storeItem(50553, 1); // 逃げたドラゴンの痕跡
				}
				for (L1PcInstance pclist : _dragonSlayers) {
					if (isDragonSlayer(pc)) {
						pclist.sendPackets(new S_ServerMessage(813, _dragon.getNameId(),
															item.getName(), pc.getName()));
					}
				}
			}
		} else if (round == 3) {
			Collections.shuffle(_dragonSlayers);
			HashMap<Integer, L1PcInstance> _playerMapList = new HashMap<Integer, L1PcInstance>();

			for (int i = 1; i <= _dragonSlayers.size(); i++) {
				_playerMapList.put(_dropNumber.get(i - 1), _dragonSlayers.get(i - 1));
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
						setItemBless(item);
						if (pc.getInventory().checkAddItem(item, count) == L1Inventory.OK) {
							pc.getInventory().storeItem(item);
						}
					} else {
						int createCount;
						for (createCount = 0; createCount < count; createCount++) {
							item = ItemTable.getInstance().createItem(drop.getItemId());
							item.setEnchantLevel(0);
							item.setIdentified(false);
							setItemBless(item);
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
				for (L1PcInstance pclist : _dragonSlayers) {
					pclist.sendPackets(new S_ServerMessage(813, _dragon.getNameId(),
															item.getLogName(), pc.getName()));
				}
			}

			for (L1PcInstance pc2 : _dragonSlayers) {
				if (isDragonSlayer(pc2)) {
					item = pc2.getInventory().storeItem(50556, 1); // 地竜の証
				}
				for (L1PcInstance pclist : _dragonSlayers) {
					if (isDragonSlayer(pclist)) {
						pclist.sendPackets(new S_ServerMessage(813, _dragon.getNameId(),
															item.getName(), pc2.getName()));
					}
				}
			}
		}
	}

	private void refresh() {
		if (!_dragon.isDead()) {
			_dragon.deleteMe();
		}
		_dragon = null;
		_isActive = false;
		_isDragonAwake = false;
		_isEnter = true;
		_isPortalOpen = false;
	}

	private void addAntharasBloodstain() {
		for (L1PcInstance pc : _dragonSlayers) {
			if (isDragonSlayer(pc)) {
				L1BuffUtil.bloodstain(pc, (byte) 0, Config.DRAGON_SLAYER_INTERVAL, true); // アンタラスの血痕
			}
		}
	}

	private boolean isDragonSlayer(L1PcInstance pc) {
		if (pc.getMapId() != _mapId) {
			return false;
		}
		if (pc.getX() < 32744 || 32812 < pc.getX()) {
			return false;
		}
		if (pc.getY() < 32659 || 32723 < pc.getY()) {
			return false;
		}
		return true;
	}

	private void sendMessage(int round) {
		try {
			if (round == 1) {
				for (int msgid = 1570; msgid <= 1572; msgid++) {
					Thread.sleep(5000);
					for (L1PcInstance pc : _dragonSlayers) {
						if (isDragonSlayer(pc)) {
							pc.sendPackets(new S_ServerMessage(msgid));
						}
					}
				}
			} else if (round == 2) {
				for (L1PcInstance pc : _dragonSlayers) {
					if (isDragonSlayer(pc)) {
						pc.sendPackets(new S_ServerMessage(1573));
					}
				}
				Thread.sleep(5000);
				for (L1PcInstance pc : _dragonSlayers) {
					if (isDragonSlayer(pc)) {
						pc.sendPackets(new S_ServerMessage(1574));
					}
				}
				Thread.sleep(10000);
				for (L1PcInstance pc : _dragonSlayers) {
					if (isDragonSlayer(pc)) {
						pc.sendPackets(new S_ServerMessage(1575));
					}
				}
				Thread.sleep(50000);
				for (L1PcInstance pc : _dragonSlayers) {
					if (isDragonSlayer(pc)) {
						pc.sendPackets(new S_ServerMessage(1576));
					}
				}
			} else if (round == 3) {
				for (L1PcInstance pc : _dragonSlayers) {
					if (isDragonSlayer(pc)) {
						pc.sendPackets(new S_ServerMessage(1577));
					}
				}
				Thread.sleep(5000);
				for (L1PcInstance pc : _dragonSlayers) {
					if (isDragonSlayer(pc)) {
						pc.sendPackets(new S_ServerMessage(1578));
					}
				}
				Thread.sleep(50000);
				for (L1PcInstance pc : _dragonSlayers) {
					if (isDragonSlayer(pc)) {
						pc.sendPackets(new S_ServerMessage(1579));
					}
				}
			} else if (round == 4) {
				Thread.sleep(5000);
				for (int msgid = 1580; msgid <= 1581; msgid++) {
					Thread.sleep(5000);
					for (L1PcInstance pc : _dragonSlayers) {
						if (isDragonSlayer(pc)) {
							pc.sendPackets(new S_ServerMessage(msgid));
						}
					}
				}
				Thread.sleep(10000);
			}
		} catch (Exception e) {

		}
	}

	private void spawnDragon(int round) {
		try {
			int[] npcId = new int[] { 91200, 91201, 91202 };
			_dragon = NpcTable.getInstance().newNpcInstance(npcId[round - 1]);
			_dragon.setId(IdFactory.getInstance().nextId());
			_dragon.setMap((short) _mapId);
			int tryCount = 0;
			do {
				tryCount++;
				_dragon.setX(32783 + _random.nextInt(10) - _random.nextInt(10));
				_dragon.setY(32693 + _random.nextInt(10) - _random.nextInt(10));
				if (_dragon.getMap().isInMap(_dragon.getLocation())
						&& _dragon.getMap().isPassable(_dragon.getLocation())) {
					break;
				}
				Thread.sleep(1);
			} while (tryCount < 50);

			if (tryCount >= 50) {
				_dragon.setX(32783);
				_dragon.setY(32693);
			}

			_dragon.setHomeX(_dragon.getX());
			_dragon.setHomeY(_dragon.getY());
			_dragon.setHeading(_random.nextInt(8));

			L1World.getInstance().storeObject(_dragon);
			L1World.getInstance().addVisibleObject(_dragon);

			if (_dragon.getGfxId() == 7557 // アンタラスLv1
					|| _dragon.getGfxId() == 7539 // アンタラスLv2
					|| _dragon.getGfxId() == 7558 // アンタラスLv3
					) {
				_dragon.setDelayTime(ActionCodes.ACTION_AxeWalk, L1NpcInstance.ATTACK_SPEED);
				for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(_dragon)) {
					_dragon.onPerceive(pc);
					S_DoActionGFX gfx = new S_DoActionGFX(_dragon.getId(), ActionCodes.ACTION_AxeWalk);
					pc.sendPackets(gfx);
				}
			}

			_dragon.updateLight();
			_dragon.startChat(L1NpcInstance.CHAT_TIMING_APPEARANCE); // チャット開始
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	// 各リストの整理
	private void checkedPlayer() {
		L1PcInstance pc = null;
		for (Iterator<L1PcInstance> i = _allPlayerList.iterator(); i.hasNext();) {
			pc = i.next();
			if (pc.getMapId() != _mapId) {
				i.remove();
			}
		}
		for (Iterator<L1PcInstance> i = _dragonSlayers.iterator(); i.hasNext();) {
			pc = i.next();
			if (!isDragonSlayer(pc)) {
				i.remove();
			}
		}
	}

	@Override
	public void portalDestroyed() {
		try {
			_portal = null;
			_dragon.setCurrentHp(_dragon.getMaxHp());
			for (int msgId = 1480; msgId <= 1484; msgId++) {
				for (L1PcInstance pc : _allPlayerList) {
					if (isDragonSlayer(pc)) {
						pc.sendPackets(new S_ServerMessage(msgId));
					}
				}
				Thread.sleep(1000);
			}
			checkedPlayer();
			for (L1PcInstance pc : _allPlayerList) {
				L1Teleport.teleport(pc, _restartLocation[_random.nextInt(_restartLocation.length)],
																				pc.getHeading(), true);
			}
			if (!_dragon.isDead()) {
				_dragon.deleteMe();
			}
			_isActive = false;
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
