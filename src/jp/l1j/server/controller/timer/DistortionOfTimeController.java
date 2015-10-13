package jp.l1j.server.controller.timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.configure.Config;
import jp.l1j.server.GeneralThreadPool;
import jp.l1j.server.controller.raid.KukulcanRaidController;
import jp.l1j.server.controller.raid.ThebesRaidController;
import jp.l1j.server.datatables.DistortionTable;
import jp.l1j.server.datatables.NpcTable;
import jp.l1j.server.model.L1Location;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.utils.IdFactory;

public class DistortionOfTimeController extends Thread {

	private static Logger _log = Logger.getLogger(DistortionOfTimeController.class.getName());

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private static DistortionOfTimeController _instance = null;

	public static synchronized DistortionOfTimeController getInstance() {
		if (_instance == null) {
			_instance = new DistortionOfTimeController();
		}
		return _instance;
	}

	private final L1Location DISTORTION1 = new L1Location(34272, 33361, 4); // 山脈A
	private final L1Location DISTORTION2 = new L1Location(34257, 33203, 4); // 山脈B
	private final L1Location DISTORTION3 = new L1Location(34226, 33313, 4); // 山脈C
	private final L1Location DISTORTION4 = new L1Location(32730, 33142, 4); // 砂漠A
	private final L1Location DISTORTION5 = new L1Location(32910, 33170, 4); // 砂漠B
	private final L1Location DISTORTION6 = new L1Location(32965, 33242, 4); // 砂漠C
	private final L1Location DISTORTION7 = new L1Location(32716, 32689, 4); // 荒地A
	private final L1Location DISTORTION8 = new L1Location(32832, 32647, 4); // 荒地B
	private final L1Location DISTORTION9 = new L1Location(32851, 32713, 4); // 荒地C

	public final L1Location THEBES = new L1Location(32626, 32900, 780); // テーベ着地点
	public final L1Location TIKAL = new L1Location(32793, 32753, 783); // ティカル着地点

	private final int RAID_OPEN_TIME = Config.DISTORTION_RAID_OPEN_TIME; // 歪みが開いてから祭壇入場までの時間(分)
	private final int RAID_BOSS_LIMIT = Config.DISTORTION_RAID_BOSS_LIMIT; // 祭壇入場からボスを討伐するまでの猶予時間(分)
	private final int NEXT_DISTORTION_TIME = Config.NEXT_DISTORTION_TIME; // 次回何分後に歪みが開くか。

	private final int CHECK_TIME = 60000; // 1分単位でチェック。これは変えないでください。

	private L1Location[] TELEPORT_LOCATION = { THEBES, TIKAL }; // テーベ及びティカルの着地点

	public L1Location[] getTeleportLocationList() {
		return TELEPORT_LOCATION;
	}

	private L1Location[] SPAWN_DISTORTIN_LOCATION = { DISTORTION1, DISTORTION2,
									DISTORTION3, DISTORTION4, DISTORTION5, DISTORTION6,
									DISTORTION7, DISTORTION8, DISTORTION9 }; // 歪み出現場所リスト

	@Override
	public void run() {
		try {
			if (!Config.DISTORTIONS) { // 歪み設定がOFFの場合は歪みを召喚して終了。
				for (L1Location loc : SPAWN_DISTORTIN_LOCATION) {
					L1NpcInstance dis = NpcTable.getInstance().newNpcInstance(71254);
					dis.setId(IdFactory.getInstance().nextId());
					dis.setMap((short) dis.getMapId());
					dis.getLocation().set(loc);
					dis.setHomeX(dis.getX());
					dis.setHomeY(dis.getY());
					dis.setHeading(dis.getHeading());
					L1World.getInstance().storeObject(dis);
					L1World.getInstance().addVisibleObject(dis);
					dis.updateLight();
					Thread.sleep(500);
				}
				return;
			}

			// データベースから各値を挿入する。
			DistortionTable.getInstance().load();
			setDistortion(null); // 歪みのObjectId取得

			if (getTeleportLocation() != null) { // 歪みがオープン状態でサーバーが閉じた。
				Calendar now = Calendar.getInstance();
				if (now.compareTo(getCloseTime()) > 0) { // 閉じる時間が過去の場合は開けない。
					setTeleporLocation(null);
					if (now.compareTo(getNextTime()) > 0) { // 開く時間が既に過去になっている場合。
						while (now.compareTo(getNextTime()) > 0) {
							getNextTime().add(Calendar.MINUTE, NEXT_DISTORTION_TIME);
							getNextTime().set(Calendar.SECOND, 0); // 00秒
						}
						getCloseTime().set(Calendar.DATE, getNextTime().get(Calendar.DATE));
						getCloseTime().set(Calendar.MINUTE, getNextTime().get(Calendar.MINUTE));
						getCloseTime().set(Calendar.SECOND, getNextTime().get(Calendar.SECOND));
						getCloseTime().add(Calendar.MINUTE, RAID_OPEN_TIME + RAID_BOSS_LIMIT + 1);
					}
				} else { // 閉じる時間が未来にある
					_isOpen = true;
					Thread.sleep(CHECK_TIME); // 1分後に歪み再召喚
					respawnDistortion(); // 時の歪みの設置
				}
			}

			while (true) {
				Thread.sleep(CHECK_TIME); // checkは1分固定

				if (isDistortionOpenFlag()) { // 開く時間
					spawnDistortion();
					if (getTeleportLocation().getMapId() == 780) { // テーベ
						GeneralThreadPool.getInstance().execute(ThebesRaidController.getInstance());
					} else if (getTeleportLocation().getMapId() == 783) { // ティカル
						GeneralThreadPool.getInstance().execute(KukulcanRaidController.getInstance());
					}
				}

				if (isDistortionCloseFlag()) { // 閉じる時間
					if (getTeleportLocation().getMapId() == 780) { // テーベ
						if (!ThebesRaidController.getInstance().isActive()) { // レイド中ではない
							closeDistortion();
						}
					} else if (getTeleportLocation().getMapId() == 783) { // ティカル
						if (!KukulcanRaidController.getInstance().isActive()) { // レイド中ではない
							closeDistortion();
						}
					}
				}
			}
		} catch(Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private boolean _isOpen = false; // 歪みが開いているか。

	public boolean isDistortionOpen() {
		return _isOpen;
	}

	private L1NpcInstance _distortion = null; // 歪み

	public L1NpcInstance getDistortion() {
		return _distortion;
	}
	public void setDistortion(L1Location loc) {
		if (_distortion == null) {
			_distortion = NpcTable.getInstance().newNpcInstance(71254);
			_distortion.setId(IdFactory.getInstance().nextId());
		}
		if (loc != null) {
			_distortion.setLocation(loc);
		}
	}

	private L1Location _teleportLocation = null; // テレポート先

	public L1Location getTeleportLocation() {
		return _teleportLocation;
	}

	public void setTeleporLocation(L1Location loc) {
		_teleportLocation = loc;
	}

	public void setTeleporLocation(int mapId) {
		if (mapId == 780) {
			_teleportLocation = new L1Location(THEBES);
		} else if (mapId == 783) {
			_teleportLocation = new L1Location(TIKAL);
		}
	}

	private int _bossSpawnTimer = 0; // 歪みが開いてからボスが出現するまでの時間(分)

	public void setBossSpawnTimer(int i) {
		_bossSpawnTimer = i;
	}

	public int getBossSpawnTimer() {
		return _bossSpawnTimer;
	}

	private Calendar _closeTime; // 歪みが閉まる日時を保存

	public Calendar getCloseTime() {
		return _closeTime;
	}

	public void setCloseTime(Calendar i) {
		_closeTime = i;
	}

	/**
	 * 歪みが開いている時間を引き延ばす。
	 * @param time
	 * 		引き延ばす時間(分)
	 */
	public void addCloseTime(int time) {
		_closeTime.add(Calendar.MINUTE, time);
	}

	private Calendar _nextTime; // 次回歪みが開く日時を保存

	public Calendar getNextTime() {
		return _nextTime;
	}

	public void setNextTime(Calendar i) {
		_nextTime = i;
	}

	private int _saveInterval; // データベース保存間隔

	public int getSaveInterval() {
		return _saveInterval;
	}

	public void setSaveInterval(int i) {
		_saveInterval = i;
	}

	/**
	 * 歪みの出現とMAPの設置。
	 */
	private void spawnDistortion() {
		_isOpen = true;
		setTeleporLocation(TELEPORT_LOCATION[_random.nextInt(TELEPORT_LOCATION.length)]);
		L1Location distortionLoc = SPAWN_DISTORTIN_LOCATION[_random.nextInt(SPAWN_DISTORTIN_LOCATION.length)];
		_distortion.setMap((short) distortionLoc.getMapId());
		_distortion.getLocation().set(distortionLoc);
		_distortion.setHomeX(_distortion.getX());
		_distortion.setHomeY(_distortion.getY());
		_distortion.setHeading(_distortion.getHeading());
		L1World.getInstance().storeObject(_distortion);
		L1World.getInstance().addVisibleObject(_distortion);
		_distortion.updateLight();
		L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1469));
		// 時の歪みが開きました。 異界の侵攻が始まります。

		setNextTimeProcedure(); // 次回の開く時間を決定する。
		DistortionTable.getInstance().saveDistortion(); // データベースに保存する。
	}

	/**
	 * 鯖再起動用の歪み設置
	 */
	private void respawnDistortion() {
		_isOpen = true;
		_distortion.setHomeX(_distortion.getX());
		_distortion.setHomeY(_distortion.getY());
		_distortion.setHeading(_distortion.getHeading());
		L1World.getInstance().storeObject(_distortion);
		L1World.getInstance().addVisibleObject(_distortion);
		_distortion.updateLight();
		// 時の歪みが開きました。 異界の侵攻が始まります。
		L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1469));
	}

	/**
	 * 時の歪みが閉まるときの処理。
	 */
	public void closeDistortion() {
		try {
			// まもなく時の歪みが閉じます。
			L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1467));
			Thread.sleep(30000);
			// 時の歪みが閉じます。
			L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1468));
		} catch (Exception e) {

		}
		_distortion.deleteMe();

		int x = 33085;
		int y = 33393;

		// 歪み内部にいるPCをギランへ飛ばす（本当はアデン）
		for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
			if (pc.getMapId() >= 780 && pc.getMapId() <= 784) {
				x += _random.nextInt(6);
				y += _random.nextInt(6);
				L1Teleport.teleport(pc, x, y, (short) 4, 5, true);
			}
		}

		refresh(); // 各値の初期化

		// 時の歪みが閉じます。
		L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1468));
	}

	/**
	 * このコントローラー内の値とデータベースの値をリセット。
	 */
	private void refresh() {
		_isOpen = false;
		setTeleporLocation(null);
		getDistortion().setLocation(0, 0, 0);
		DistortionTable.getInstance().saveDistortion();
	}

	/**
	 * 次回の歪み出現日時を計算してセットする。
	 */
	private void setNextTimeProcedure() {
		Calendar openTime = Calendar.getInstance();
		openTime.set(Calendar.MINUTE, getNextTime().get(Calendar.MINUTE));
		openTime.add(Calendar.MINUTE, NEXT_DISTORTION_TIME);
		openTime.set(Calendar.SECOND, 0); // 00秒
		setNextTime(openTime);
		Calendar closeTime = Calendar.getInstance();
		openTime.set(Calendar.MINUTE, getNextTime().get(Calendar.MINUTE));
		closeTime.add(Calendar.MINUTE, NEXT_DISTORTION_TIME); // 今の時刻の二日後の
		closeTime.set(Calendar.SECOND, 0); // 00秒
		closeTime.add(Calendar.MINUTE, RAID_OPEN_TIME + RAID_BOSS_LIMIT + 1);
		setCloseTime(closeTime);
	}

	/**
	 * 歪みが閉じる時刻かどうか
	 * @return
	 * 		閉じるならtrue、閉じないならfalse
	 */

	private boolean isDistortionCloseFlag() {
		if (!_isOpen) { // 既に閉じている場合は何もしない
			return false;
		}

		Calendar now = Calendar.getInstance();
		now.set(Calendar.SECOND, 0);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowTime = sdf.format(now.getTime());
		String closeTime = sdf.format(getCloseTime().getTime());
		return nowTime.equalsIgnoreCase(closeTime);
	}

	/**
	 * 歪みが開くかどうか。
	 * @return
	 * 		開くならtrue、開かないならfalse
	 */
	private boolean isDistortionOpenFlag() {
		if (_isOpen) {
			return false;
		}
		Calendar now = Calendar.getInstance();
		now.set(Calendar.SECOND, 0);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowTime = sdf.format(now.getTime());
		String openTime = sdf.format(getNextTime().getTime());
		return nowTime.equalsIgnoreCase(openTime);
	}
}
