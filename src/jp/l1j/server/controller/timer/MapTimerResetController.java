package jp.l1j.server.controller.timer;

import java.util.ArrayList;
import java.util.Calendar;

import jp.l1j.server.datatables.MapTable;
import jp.l1j.server.datatables.MapTimerTable;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1PcInstance;

public class MapTimerResetController implements Runnable {

	private static MapTimerResetController _instance = null;

	public static MapTimerResetController getInstance() {
		if (_instance == null) {
			_instance = new MapTimerResetController();
		}
		return _instance;
	}

	private MapTimerResetController() {

	}

	@Override
	public void run() {
		try {
			ArrayList<Integer> resetAreaList = new ArrayList<Integer>();
			int maxReminingTime;
			while (true) {
				Thread.sleep(60000);
				for (int mapId : MapTimerController.getInstance().getMonitoringMapIdList()) {
					if (!resetAreaList.contains(MapTable.getInstance().getAreaId(mapId))) {
						resetAreaList.add(MapTable.getInstance().getAreaId(mapId));
					}
				}
				for (int areaId : resetAreaList) {
					if (isResetMap(areaId)) {
						maxReminingTime = MapTable.getInstance().getMaxTime(areaId);
						resetMapExecute(areaId, maxReminingTime);
						maxReminingTime = 0;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resetMapExecute(int areaId, int reminingTime) {
		for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
			if (pc.getMapTimer().get(areaId) != null) {
				pc.getMapTimer().remove(areaId);
			}
		}
		MapTimerTable.getInstance().deleteMapTimer(areaId);
		System.out.println("マップタイマーがリセットされました。 AreaID:" + areaId);
	}

	private boolean isResetMap(int areaId) {
		int resetDate = MapTable.getInstance().getResetDate(areaId);
		int resetWeek = calcDayOfWeek(MapTable.getInstance().getResetWeek(areaId));
		int resetTime = MapTable.getInstance().getResetTime(areaId);
		Calendar cal = Calendar.getInstance();
		boolean[] isReset = { false, false, false, false };
		if (resetTime == -1 || resetTime == (cal.get(Calendar.HOUR_OF_DAY) * 100) + cal.get(Calendar.MINUTE)) {
			isReset[0] = true;
		}
		if (resetWeek == -1 || resetWeek == cal.get(Calendar.DAY_OF_WEEK)) {
			isReset[1] = true;
		}
		if (resetDate == -1 || resetDate == cal.get(Calendar.DAY_OF_MONTH)) {
			isReset[2] = true;
		}
		return (isReset[0] && isReset[1] && isReset[2]);
	}

	private int calcDayOfWeek(String week) {
		if (week == null) {
			return -1;
		} else if (week.equalsIgnoreCase("sunday")) {
			return Calendar.SUNDAY;
		} else if (week.equalsIgnoreCase("monday")) {
			return Calendar.MONDAY;
		} else if (week.equalsIgnoreCase("tuesday")) {
			return Calendar.TUESDAY;
		} else if (week.equalsIgnoreCase("wednesday")) {
			return Calendar.WEDNESDAY;
		} else if (week.equalsIgnoreCase("thursday")) {
			return Calendar.THURSDAY;
		} else if (week.equalsIgnoreCase("friday")) {
			return Calendar.FRIDAY;
		} else if (week.equalsIgnoreCase("saturday")) {
			return Calendar.SATURDAY;
		}
		return -1;
	}
}
