package jp.l1j.server.controller.timer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.configure.Config;
import jp.l1j.server.codes.ActionCodes;
import jp.l1j.server.datatables.ItemTable;
import jp.l1j.server.datatables.NpcTable;
import jp.l1j.server.datatables.UltimateBattleConfigTable;
import jp.l1j.server.datatables.UltimateBattleSpawnTable;
import jp.l1j.server.datatables.UltimateBattleTimeTable;
import jp.l1j.server.model.L1Location;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1MonsterInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.inventory.L1GroundInventory;
import jp.l1j.server.model.inventory.L1Inventory;
import jp.l1j.server.packets.server.S_NpcChatPacket;
import jp.l1j.server.packets.server.S_NpcPack;
import jp.l1j.server.packets.server.S_SystemMessage;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1Item;
import jp.l1j.server.templates.L1SpawnUltimateBattle;
import jp.l1j.server.templates.L1UltimateBattleConfig;
import jp.l1j.server.templates.L1UltimateBattleTimes;
import jp.l1j.server.utils.IdFactory;

import static jp.l1j.locale.I18N.*;
import static jp.l1j.server.model.item.L1ItemId.*;

public class UltimateBattleController implements Runnable {

	private static Logger _log = Logger.getLogger(UltimateBattleController.class.getName());

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private int _ultimateBattleId;
	private L1UltimateBattleConfig _config; // このUBの設定
	private L1NpcInstance _manager; // コロシアム管理人
	private L1NpcInstance _subManager; // コロシアム副管理人

	private ArrayList<L1PcInstance> _playerList = new ArrayList<L1PcInstance>();

	private static final String ANNOUNCE_CHAT1 = "競技開始まであと";
	private static final String ANNOUNCE_CHAT2 = "分残っています。";
	private static final String ANNOUNCE_CHAT3 = "参加を望まれる方は今すぐご参加ください。";

	private ArrayList<L1SpawnUltimateBattle> _group1 = new ArrayList<L1SpawnUltimateBattle>();
	private ArrayList<L1SpawnUltimateBattle> _group2 = new ArrayList<L1SpawnUltimateBattle>();
	private ArrayList<L1SpawnUltimateBattle> _group3 = new ArrayList<L1SpawnUltimateBattle>();
	private ArrayList<L1SpawnUltimateBattle> _group4 = new ArrayList<L1SpawnUltimateBattle>();

	public UltimateBattleController(int ubid) {
		_ultimateBattleId = ubid;
		_config = UltimateBattleConfigTable.getInstance().getUBConfig().get(ubid);
		_manager = NpcTable.getInstance().newNpcInstance(_config.getManagerId());
		_manager.setId(IdFactory.getInstance().nextId());
		_manager.setMap((short) _config.getManagerMapId());
		_manager.getLocation().set(_config.getManagerX(), _config.getManagerY(), _config.getManagerMapId());
		_manager.setHomeX(_manager.getX());
		_manager.setHomeY(_manager.getY());
		_manager.setHeading(_config.getManagerHeading());
		L1World.getInstance().storeObject(_manager);
		L1World.getInstance().addVisibleObject(_manager);
		_manager.updateLight();
		_subManager = NpcTable.getInstance().newNpcInstance(_config.getSubManagerId());
		_subManager.setId(IdFactory.getInstance().nextId());
		_subManager.setMap((short) _config.getSubManagerMapId());
		_subManager.getLocation().set(_config.getSubManagerX(), _config.getSubManagerY(), _config.getSubManagerMapId());
		_subManager.setHomeX(_subManager.getX());
		_subManager.setHomeY(_subManager.getY());
		_subManager.setHeading(_config.getSubManagerHeading());
		L1World.getInstance().storeObject(_subManager);
		L1World.getInstance().addVisibleObject(_subManager);
		_subManager.updateLight();
	}

	private boolean _isActivate = false;

	private boolean _isUltimateBattleStart = false;

	public boolean isUBStart() {
		return _isUltimateBattleStart;
	}

	private boolean _isEnter = false;

	public boolean isEnter() {
		return _isEnter;
	}

	public L1UltimateBattleConfig getConfig() {
		return _config;
	}

	public void addPlayer(L1PcInstance pc) {
		_playerList.add(pc);
	}

	public ArrayList<L1PcInstance> getPlayers() {
		return _playerList;
	}

	@Override
	public void run() {
		try {
			int announceTime = Config.ULTIMATE_BATTLE_NOTICE;

			// 開始されるUBのパターンを確定する。
			setUltimateBattlePattern();

			_manager.broadcastPacket(new S_NpcChatPacket(_manager, ANNOUNCE_CHAT1 + announceTime + ANNOUNCE_CHAT2, 2));
			announceTime--;
			for (; announceTime > 5; announceTime--) {
				Thread.sleep(60000);
				_manager.broadcastPacket(new S_NpcChatPacket(_manager, ANNOUNCE_CHAT1 + announceTime + ANNOUNCE_CHAT2, 2));
			}
			for (; announceTime > 1; announceTime--) {
				for (int i = 0; i < 6; i++) { // 入場可能時間中は10秒毎に退場者を判定する。
					Thread.sleep(10000);
					removeRetiredMembers();
				}
				_manager.broadcastPacket(new S_NpcChatPacket(_manager, ANNOUNCE_CHAT1
									+ announceTime + ANNOUNCE_CHAT2 + ANNOUNCE_CHAT3, 2));
				_isEnter = true;
			}

			Thread.sleep(50000);
			removeRetiredMembers();
			sendCountDownMessage();
			_isEnter = false;

			if (_isActivate) { // 参加プレイヤーがいる場合のみ動作。
				_isUltimateBattleStart = true;
				sendSystemMessage("第1ラウンドです。");
				for (L1SpawnUltimateBattle spawn : _group1) { // 第一ラウンド
					removeRetiredMembers();
					Thread.sleep(spawn.getSpawnDelay() * 1000);
					spawnAll(spawn);
				}
				Thread.sleep(14000); // 14秒後
				dropItemForGround(1); // 補充アイテム
				sendSystemMessage("第1ラウンド終了です。1分後に第2ラウンドが始まります。");
				removeRetiredMembers();
				Thread.sleep(60000); // 次回ラウンドまでのインターバル

				sendSystemMessage("第2ラウンドです。");
				for (L1SpawnUltimateBattle spawn : _group2) { // 第二ラウンド
					removeRetiredMembers();
					Thread.sleep(spawn.getSpawnDelay() * 1000);
					spawnAll(spawn);
				}
				Thread.sleep(14000); // 14秒後
				dropItemForGround(2); // 補充アイテム
				sendSystemMessage("第2ラウンド終了です。1分後に第3ラウンドが始まります。");
				Thread.sleep(60000); // 次回ラウンドまでのインターバル

				sendSystemMessage("第3ラウンドです。");
				for (L1SpawnUltimateBattle spawn : _group3) { // 第三ラウンド
					removeRetiredMembers();
					Thread.sleep(spawn.getSpawnDelay() * 1000);
					spawnAll(spawn);
				}
				Thread.sleep(4000); // 4秒後
				dropItemForGround(3); // 補充アイテム
				sendSystemMessage("第3ラウンド終了です。3分後に最終ラウンドが始まります。");
				Thread.sleep(180000); // 次回ラウンドまでのインターバル(本当は6分)

				sendSystemMessage("最終ラウンドです。");
				for (L1SpawnUltimateBattle spawn : _group4) { // 第四ラウンド
					removeRetiredMembers();
					Thread.sleep(spawn.getSpawnDelay() * 1000);
					spawnAll(spawn);
				}
				for (int i = 5; i > 1; i--) { // 第四ラウンドのすべてのモンスターが登場してから5分。
					sendSystemMessage("競技終了まで残り" + i + "分です。");
					removeRetiredMembers();
					Thread.sleep(60000);
				}
				removeRetiredMembers();
				Thread.sleep(30000); // 残り30秒
				sendSystemMessage("競技終了まで残り30秒です。");
				removeRetiredMembers();
				Thread.sleep(10000); // 残り20秒
				sendSystemMessage("競技終了まで残り20秒です。");
				removeRetiredMembers();
				Thread.sleep(10000); // 残り10秒
				sendSystemMessage("競技終了まで残り10秒です。");
				removeRetiredMembers();
				Thread.sleep(5000); // 残り5秒
				sendSystemMessage("競技終了まで残り5秒です。");
				removeRetiredMembers();
				Thread.sleep(1000); // 残り4秒
				sendSystemMessage("競技終了まで残り4秒です。");
				removeRetiredMembers();
				Thread.sleep(1000); // 残り3秒
				sendSystemMessage("競技終了まで残り3秒です。");
				removeRetiredMembers();
				Thread.sleep(1000); // 残り2秒
				sendSystemMessage("競技終了まで残り2秒です。");
				removeRetiredMembers();
				Thread.sleep(1000); // 残り1秒
				sendSystemMessage("競技終了まで残り1秒です。");
				removeRetiredMembers();
				Thread.sleep(1000);
			}
			// アルティメットバトル終了
			removePlayerList(); // 参加者を全員外へ出す。
			clearColosseum(); // 競技場内のモンスターとアイテムを削除。
			_isUltimateBattleStart = false;
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			sendSystemMessage("エラーの為アルティメットバトルを中断します。10秒後にテレポートします。");
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
			}
			removePlayerList(); // 参加者を全員外へ出す。
			clearColosseum(); // 競技場内のモンスターとアイテムを削除。
			_isUltimateBattleStart = false;
		}
	}

	private void spawnAll(L1SpawnUltimateBattle sub) {
		for (int i = 0; i < sub.getCount(); i++) {
			spawnMob(sub);
		}
	}

	private void spawnMob(L1SpawnUltimateBattle sub) {
		L1Location loc = _config.getLocation().randomLocation(
				(_config.getLocX2() - _config.getLocX1()) / 2, false);
		L1MonsterInstance mob = new L1MonsterInstance(NpcTable.getInstance().getTemplate(sub.getMonsterId()));
		if (mob == null) {
			_log.warning("MonsterId:" + sub.getMonsterId() + " is null");
			return;
		}
		if (mob.getNpcTemplate() == null) {
			_log.warning("MonsterId:" + sub.getMonsterId() + " is template null");
			return;
		}
		mob.setId(IdFactory.getInstance().nextId());
		mob.setHeading(5);
		mob.setX(loc.getX());
		mob.setHomeX(loc.getX());
		mob.setY(loc.getY());
		mob.setHomeY(loc.getY());
		mob.setMap((short) loc.getMapId());
		mob.setStoreDroped(sub.isDrop());
		mob.setUbSealCount(sub.getSealCount());
		mob.setUbId(_ultimateBattleId);

		L1World.getInstance().storeObject(mob);
		L1World.getInstance().addVisibleObject(mob);

		S_NpcPack S_NpcPack = new S_NpcPack(mob);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(mob)) {
			pc.addKnownObject(mob);
			mob.addKnownObject(pc);
			pc.sendPackets(S_NpcPack);
		}
		// モンスターのＡＩを開始
		mob.onNpcAI();
		mob.updateLight();
	}

	/**
	 * 競技場内のプレイヤーを全員外へ出す。
	 */
	private void removePlayerList() {
		int x = 33438 + _random.nextInt(6);
		int y = 32813 + _random.nextInt(6);
		for (L1PcInstance pc : _playerList) {
			// 一律でギランにテレポートする(改善が必要)
			L1Teleport.teleport(pc, x, y, (short) 4, 5, true);
		}
		_playerList.clear();
	}

	/**
	 * コロシアムから出たメンバーをメンバーリストから削除する。
	 */
	private void removeRetiredMembers() {
		try {
			ArrayList<L1PcInstance> list = new ArrayList<L1PcInstance>(_playerList);
			for (L1PcInstance pc : _playerList) {
				if (pc.getMapId() != _config.getMapId()) {
					list.remove(pc);
				}
			}
			if (list.size() < 1) { // 参加者がいない。
				_isActivate = false;
			} else {
				_isActivate = true;
			}
			_playerList = new ArrayList<L1PcInstance>(list);
		} catch (Exception e) {

		}
	}

	/**
	 * コロシアム上のアイテムとモンスターを全て削除する。
	 */
	private void clearColosseum() {
		for (Object obj : L1World.getInstance().getVisibleObjects(_config.getMapId()).values()) {
			if (obj instanceof L1MonsterInstance) { // モンスター削除
				L1MonsterInstance mob = (L1MonsterInstance) obj;
				if (!mob.isDead()) {
					mob.setDead(true);
					mob.setStatus(ActionCodes.ACTION_Die);
					mob.setCurrentHpDirect(0);
					mob.deleteMe();
				}
			} else if (obj instanceof L1Inventory) { // アイテム削除
				L1Inventory inventory = (L1Inventory) obj;
				inventory.clearItems();
			}
		}
	}

	private void dropItemForGround(int nowRound) {
		if (nowRound == 1) {
			spawnGroundItem(ADENA, 1000, 60);
			spawnGroundItem(POTION_OF_CURE_POISON, 3, 20);
			spawnGroundItem(POTION_OF_EXTRA_HEALING, 5, 20);
			spawnGroundItem(POTION_OF_GREATER_HEALING, 3, 20);
			spawnGroundItem(40317, 1, 5); // 砥石
			spawnGroundItem(40079, 1, 20); // 帰還スク
		} else if (nowRound == 2) {
			spawnGroundItem(ADENA, 5000, 50);
			spawnGroundItem(POTION_OF_CURE_POISON, 5, 20);
			spawnGroundItem(POTION_OF_EXTRA_HEALING, 10, 20);
			spawnGroundItem(POTION_OF_GREATER_HEALING, 5, 20);
			spawnGroundItem(40317, 1, 7); // 砥石
			spawnGroundItem(40093, 1, 10); // ブランクスク(Lv4)
			spawnGroundItem(40079, 1, 5); // 帰還スク
		} else if (nowRound == 3) {
			spawnGroundItem(ADENA, 10000, 30);
			spawnGroundItem(POTION_OF_CURE_POISON, 7, 20);
			spawnGroundItem(POTION_OF_EXTRA_HEALING, 20, 20);
			spawnGroundItem(POTION_OF_GREATER_HEALING, 10, 20);
			spawnGroundItem(40317, 1, 10); // 砥石
			spawnGroundItem(40094, 1, 10); // ブランクスク(Lv5)
		}
	}

	/**
	 * コロシアム上へアイテムを出現させる。
	 *
	 * @param itemId
	 *            出現させるアイテムのアイテムID
	 * @param stackCount
	 *            アイテムのスタック数
	 * @param count
	 *            出現させる数
	 */
	private void spawnGroundItem(int itemId, int stackCount, int count) {
		L1Item temp = ItemTable.getInstance().getTemplate(itemId);
		if (temp == null) {
			return;
		}
		for (int i = 0; i < count; i++) {
			L1Location loc = _config.getLocation().randomLocation((_config.getLocX2() - _config.getLocX1()) / 2, false);
			if (temp.isStackable()) {
				L1ItemInstance item = ItemTable.getInstance().createItem(itemId);
				item.setEnchantLevel(0);
				item.setCount(stackCount);
				L1GroundInventory ground = L1World.getInstance().getInventory(loc.getX(), loc.getY(), _config.getMapId());
				if (ground.checkAddItem(item, stackCount) == L1Inventory.OK) {
					ground.storeItem(item);
				}
			} else {
				L1ItemInstance item = null;
				for (int createCount = 0; createCount < stackCount; createCount++) {
					item = ItemTable.getInstance().createItem(itemId);
					item.setEnchantLevel(0);
					L1GroundInventory ground = L1World.getInstance().getInventory(loc.getX(), loc.getY(), _config.getMapId());
					if (ground.checkAddItem(item, stackCount) == L1Inventory.OK) {
						ground.storeItem(item);
					}
				}
			}
		}
	}

	private void setUltimateBattlePattern() {
		int maxPattern = UltimateBattleSpawnTable.getInstance().getMaxPattern(_ultimateBattleId);
		int pattern = _random.nextInt(maxPattern);
		if (maxPattern < 2) {
			pattern = 1;
		}
		_group1 = UltimateBattleSpawnTable.getInstance().getSpawnList(_ultimateBattleId, pattern, 1);
		_group2 = UltimateBattleSpawnTable.getInstance().getSpawnList(_ultimateBattleId, pattern, 2);
		_group3 = UltimateBattleSpawnTable.getInstance().getSpawnList(_ultimateBattleId, pattern, 3);
		_group4 = UltimateBattleSpawnTable.getInstance().getSpawnList(_ultimateBattleId, pattern, 4);
	}

	/**
	 * １０秒前からのカウントダウンメッセージ。
	 * @throws InterruptedException
	 */
	private void sendCountDownMessage() throws InterruptedException {
		sendSystemMessage("まもなくアルティメットバトルがスタートします！準備はよろしいですか？"); // 10秒前
		Thread.sleep(5000);
		removeRetiredMembers();
		sendSystemMessage("5"); // 5秒前
		Thread.sleep(1000);
		removeRetiredMembers();
		sendSystemMessage("4"); // 4秒前
		Thread.sleep(1000);
		removeRetiredMembers();
		sendSystemMessage("3"); // 3秒前
		Thread.sleep(1000);
		removeRetiredMembers();
		sendSystemMessage("2"); // 2秒前
		Thread.sleep(1000);
		removeRetiredMembers();
		sendSystemMessage("1"); // 1秒前
		Thread.sleep(1000);
		removeRetiredMembers();
		sendSystemMessage("アルティメット バトル スタートです！"); // スタート
	}

	/**
	 * UBに参加しているプレイヤーへメッセージ(S_SystemMessage)を送信する。
	 *
	 * @param msg
	 *            送信するメッセージ
	 */
	private void sendSystemMessage(String msg) {
		for (L1PcInstance pc : _playerList) {
			pc.sendPackets(new S_SystemMessage(msg));
		}
	}


	private String[] _ubInfo; // UBの設定の情報(HTML用)

	public String[] makeUbInfoStrings() {
		if (_ubInfo != null) {
			return _ubInfo;
		}
		String nextUbTime = getNextUBTime();
		// クラス
		StringBuilder classesBuff = new StringBuilder();
		if (_config.isEnterDarkelf()) {
			classesBuff.append(I18N_DARK_ELF + " "); // ダークエルフ
		}
		if (_config.isEnterMage()) {
			classesBuff.append(I18N_WIZARD + " "); // ウィザード
		}
		if (_config.isEnterElf()) {
			classesBuff.append(I18N_ELF + " "); // エルフ
		}
		if (_config.isEnterKnight()) {
			classesBuff.append(I18N_KNIGHT + " "); // ナイト
		}
		if (_config.isEnterRoyal()) {
			classesBuff.append(I18N_PRINCE + " "); // プリンス
		}
		if (_config.isEnterDragonKnight()) {
			classesBuff.append(I18N_DRAGON_KNIGHT + " "); // ドラゴンナイト
		}
		if (_config.isEnterIllusionist()) {
			classesBuff.append(I18N_ILLUSIONIST + " "); // イリュージョニスト
		}
		String classes = classesBuff.toString().trim();
		// 性別
		StringBuilder sexBuff = new StringBuilder();
		if (_config.isEnterMale()) {
			sexBuff.append(I18N_MALE + " "); // 男
		}
		if (_config.isEnterFemale()) {
			sexBuff.append(I18N_FEMALE + " "); // 女
		}
		String sex = sexBuff.toString().trim();
		String loLevel = String.valueOf(_config.getMinLevel());
		String hiLevel = String.valueOf(_config.getMaxLevel());
		String teleport = _config.getLocation().getMap().isEscapable() ? I18N_POSSIBLE : I18N_IMPOSSIBLE; // 可能 : 不可
		String res = _config.getLocation().getMap().isUseResurrection() ? I18N_POSSIBLE : I18N_IMPOSSIBLE; // 可能 : 不可
		String pot = I18N_POSSIBLE; // 可能
		String hpr = String.valueOf(_config.getHpr());
		String mpr = String.valueOf(_config.getMpr());
		String summon = _config.getLocation().getMap().isTakePets() ? I18N_POSSIBLE : I18N_IMPOSSIBLE; // 可能 : 不可
		_ubInfo = new String[] { nextUbTime, classes, sex, loLevel, hiLevel,
				teleport, res, pot, hpr, mpr, summon };
		return _ubInfo;
	}

	private String getNextUBTime() {
		int nextTime = 0;
		int nowTime;
		Calendar now = Calendar.getInstance();
		nowTime = (now.get(Calendar.HOUR_OF_DAY) * 100) + now.get(Calendar.MINUTE);
		for (L1UltimateBattleTimes ubt : UltimateBattleTimeTable.getInstance().getUBTimeList()) {
			if (ubt.getUltimateBattleId() == _ultimateBattleId) {
				for (int time : ubt.getTimes()) {
					if (nowTime < time) {
						nextTime = time;
						break;
					}
				}
				if (nextTime == 0) {
					nextTime = ubt.getTimes()[0];
				}
				break;
			}
		}
		return String.valueOf(nextTime);
	}
}
