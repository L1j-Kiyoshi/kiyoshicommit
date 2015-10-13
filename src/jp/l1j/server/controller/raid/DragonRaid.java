package jp.l1j.server.controller.raid;

import java.util.ArrayList;

import jp.l1j.server.model.L1Location;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1RaidConfig;
import jp.l1j.server.templates.L1RaidDrop;

public class DragonRaid extends Thread {

	protected ArrayList<L1PcInstance> _allPlayerList = new ArrayList<L1PcInstance>();

	public ArrayList<L1PcInstance> getAllPlayerList() {
		return _allPlayerList;
	}

	public void addAllPlayer(L1PcInstance pc) {
		if (!_allPlayerList.contains(pc)) {
			_allPlayerList.add(pc);
		}
	}

	protected ArrayList<L1PcInstance> _dragonSlayers = new ArrayList<L1PcInstance>();

	public ArrayList<L1PcInstance> getDragonSlayers() {
		return _dragonSlayers;
	}

	public void addDragonSlayers(L1PcInstance pc) {
		if (!_dragonSlayers.contains(pc)) {
			_dragonSlayers.add(pc);
		}
	}

	protected static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	protected L1Location[] _restartLocation = { new L1Location(33700, 32500, 4),
												new L1Location(33730, 32488, 4) };

	protected L1RaidConfig _raidConfig = null;

	protected ArrayList<L1RaidDrop> _allDropList = new ArrayList<L1RaidDrop>();
	protected ArrayList<L1RaidDrop> _dropList; // 今回確定したパターンしか格納されないドロップ配列
	protected ArrayList<Integer> _dropNumber = new ArrayList<Integer>();
	protected int MAX_DROP_PATTERN = 1;

	protected int _mapId = 0;
	protected boolean _isCompletedRun = false;
	protected boolean _isPortalOpen = false;
	protected boolean _isActive = false;
	protected boolean _isEnter = true;
	protected boolean _isDragonAwake = false;

	protected L1NpcInstance _portal = null;
	protected L1NpcInstance _dragon = null;

	// このスレッドが既に実行された後で破棄されているかどうか。(run()が実行されたかどうか。)
	public boolean isCompletedRun() {
		return _isCompletedRun;
	}

	// このスレッドの動作起点のL1DragonPortalInstance
	public void setDragonPortal(L1NpcInstance portal) {
		_portal = portal;
	}
	// このスレッドの動作起点のL1DragonPortalInstance
	public L1NpcInstance getDragonPortal() {
		return _portal;
	}

	// このスレッドのポータルが開いているor閉じているかのセット
	public void setPortalOpen(boolean b) {
		_isPortalOpen = b;
	}

	// このスレッドのポータルが開いている状態かどうか。
	public boolean isPortalOpen() {
		return _isPortalOpen;
	}

	// このスレッドがアクティブ状態かどうか。
	public synchronized boolean isActive() {
		return _isActive;
	}

	// レアに入れるかどうか。
	public boolean isDragonAwaked() {
		return _isDragonAwake;
	}

	// このポータルに入れるかどうか。
	public boolean isEnter() {
		return _isEnter;
	}

	// 動作停止用
	public void portalDestroyed() {
	}

	public int getMapId() {
		return _mapId;
	}

	protected void setItemBless(L1ItemInstance item) {
		if (item.getItem().getType() == 10) { // 魔法書類
			item.getItem().setBless(1);
		}
	}
}
