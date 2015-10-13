package jp.l1j.server.controller;

import java.util.HashMap;

import jp.l1j.configure.Config;
import jp.l1j.server.GeneralThreadPool;
import jp.l1j.server.controller.raid.AntharasRaid;
import jp.l1j.server.controller.raid.DragonRaid;
import jp.l1j.server.controller.raid.FafurionRaid;
import jp.l1j.server.controller.raid.LindviorRaid;
import jp.l1j.server.datatables.NpcTable;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.utils.IdFactory;

import static jp.l1j.server.controller.raid.L1RaidId.*;

public class DragonPortalController {

	private static DragonPortalController _instance = null;

	public static synchronized DragonPortalController getInstance() {
		if (_instance == null) {
			_instance = new DragonPortalController();
		}
		return _instance;
	}

	private DragonPortalController() {
		for (int mapId = 1005; mapId <= 1010; mapId++) {
			_portal.put(mapId, new AntharasRaid(mapId));
		}
		for (int mapId = 1011; mapId <= 1016; mapId++) {
			_portal.put(mapId, new FafurionRaid(mapId));
		}
		for (int mapId = 1017; mapId <= 1022; mapId++) {
			_portal.put(mapId, new LindviorRaid(mapId));
		}
	}

	private HashMap<Integer, DragonRaid> _portal = new HashMap<Integer, DragonRaid>();

	public boolean isPortalActive(int mapId) {
		return _portal.get(mapId).isPortalOpen();
	}

	public synchronized void startDragonRaid(int mapId) {
		if (_portal.get(mapId).isActive()) {
			return;
		}
		if (_portal.get(mapId).isCompletedRun()) {
			raidMapCleaning(mapId);
		}
		GeneralThreadPool.getInstance().execute(_portal.get(mapId));
	}

	public DragonRaid getDragonRaid(int mapId) {
		return _portal.get(mapId);
	}

	public int getOpenRaid(int raidId) {
		int openMapId = 0;
		boolean isOpenMap = false;
		int[] mapId = null;
		if (raidId == ANTHARAS_RAID) {
			mapId = new int[] { 1005, 1006, 1007, 1008, 1009, 1010 };
		} else if (raidId == FAFURION_RAID) {
			mapId = new int[] { 1011, 1012, 1013, 1014, 1015, 1016 };
		} else if (raidId == LINDVIOR_RAID) {
			mapId = new int[] { 1017, 1018, 1019, 1020, 1021, 1022 };
		} else if (raidId == VALAKAS_RAID) {
			mapId = new int[] { 1023, 1024, 1025, 1026, 1027, 1028 };
		}
		if (mapId == null) {
			return 0;
		}
		for (int openId : mapId) {
			isOpenMap = _portal.get(openId).isPortalOpen();
			if (!isOpenMap) {
				openMapId = openId;
				break;
			}
		}
		return openMapId;
	}

	public void raidMapCleaning(int mapId) {
		if (1005 >= mapId && mapId <= 1010) {
			_portal.put(mapId, new AntharasRaid(mapId));
		}
		if (1011 >= mapId && mapId <= 1016) {
			_portal.put(mapId, new DragonRaid());
		}
		if (1017 >= mapId && mapId <= 1022) {
			_portal.put(mapId, new DragonRaid());
		}
	}
}
