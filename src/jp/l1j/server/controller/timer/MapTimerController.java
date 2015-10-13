package jp.l1j.server.controller.timer;

import java.util.ArrayList;
import java.util.logging.Logger;

import jp.l1j.server.datatables.MapTable;
import jp.l1j.server.datatables.MapTimerTable;
import jp.l1j.server.datatables.ReturnLocationTable;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_ServerMessage;

public class MapTimerController implements Runnable {

	private static Logger _log = Logger.getLogger(MapTimerController.class.getName());

	private static MapTimerController _instance = null;

	public static synchronized MapTimerController getInstance() {
		if (_instance == null) {
			_instance = new MapTimerController();
		}
		return _instance;
	}

	private ArrayList<L1PcInstance> _monitorPc = new ArrayList<L1PcInstance>();
	private ArrayList<Integer> _monitoringMapList = new ArrayList<Integer>();

	@Override
	public void run() {
		try {
			Thread.sleep(10000);
//			sendStartMonitoringLog();
			int areaId;
			while (true) {
				for (L1PcInstance pc : _monitorPc) {
					if (pc == null) {
						continue;
					}
					areaId = MapTable.getInstance().getAreaId(pc.getMapId());
					for (int monitorMapId : _monitoringMapList) {
						if (pc.getMapId() == monitorMapId) {
							if (pc.getMapTimer().get(areaId) == null) { // 初入場時
								pc.getMapTimer().put(areaId, MapTable.getInstance().getMaxTime(areaId));
								sendReminingMessage(pc, pc.getMapTimer().get(areaId));
							} else if (pc.getMapTimer().get(areaId) <= 0) { // タイムアップ時
								sendExitMessage(pc, areaId);
								MapTimerTable.getInstance().saveMapTimer(pc);
							} else {
								pc.getMapTimer().put(areaId, pc.getMapTimer().get(areaId) - 1);
								sendReminingMessage(pc, pc.getMapTimer().get(areaId));
							}
						}
					}
				}
				judgementMonitoringPc();
				Thread.sleep(1000);
			}
		} catch (Exception e) {

		}
	}

	public boolean isTimerMap(int mapId) {
		return _monitoringMapList.contains(mapId);
	}

	public void addmonitoringMapId(int mapId) {
		_monitoringMapList.add(mapId);
	}

	public ArrayList<Integer> getMonitoringMapIdList() {
		return _monitoringMapList;
	}

	public void addMonitorPc(L1PcInstance pc) {
		if (!_monitorPc.contains(pc)) {
			_monitorPc.add(pc);
		}
	}

	private void judgementMonitoringPc() {
		ArrayList<L1PcInstance> removePc = new ArrayList<L1PcInstance>();
		for (L1PcInstance pc : _monitorPc) {
			if (!isMonitoringPc(pc)) {
				removePc.add(pc);
			}
		}

		for (L1PcInstance pc : removePc) {
			MapTimerTable.getInstance().saveMapTimer(pc);
			_monitorPc.remove(pc);
		}
	}

	private boolean isMonitoringPc(L1PcInstance pc) {
		boolean flag = false;
		for (int monitorMapId : _monitoringMapList) {
			if (pc != null && pc.getMapId() == monitorMapId) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	private void sendReminingMessage(L1PcInstance pc, int remainingTime) {
		if (remainingTime % 3600 == 0) { // 時間単位
			pc.sendPackets(new S_ServerMessage(1526, String.valueOf(remainingTime / 3600)));
		} else if (remainingTime == 1800) { // 残り30分
			pc.sendPackets(new S_ServerMessage(1527, "30"));
		} else if (remainingTime == 1200) { // 残り20分
			pc.sendPackets(new S_ServerMessage(1527, "20"));
		} else if (remainingTime <= 600 && remainingTime >= 60) { // 残り10分以下、31秒以上
			if (remainingTime % 60 == 0) {
				pc.sendPackets(new S_ServerMessage(1527, String.valueOf(remainingTime / 60)));
			}
		} else if (remainingTime <= 30 && remainingTime >= 11) {
			if (remainingTime % 10 == 0) {
				pc.sendPackets(new S_ServerMessage(1528, String.valueOf(remainingTime)));
			}
		}
	}

	private void sendExitMessage(L1PcInstance pc, int areaId) {
		int hour = MapTable.getInstance().getMaxTime(areaId) / 3600;
		int time = (MapTable.getInstance().getMaxTime(areaId) - (hour * 3600)) / 60;
		if (time > 0 && hour > 0) { // 時間と秒がある
			pc.sendPackets(new S_ServerMessage(1524, String.valueOf(hour), String.valueOf(time)));
		} else if (hour > 0 && time <= 0) { // 時間がありで分が0
			pc.sendPackets(new S_ServerMessage(1522, String.valueOf(hour)));
		} else if (hour <= 0 && time > 0) { // 時間が0で分がある
			pc.sendPackets(new S_ServerMessage(1523, String.valueOf(time)));
		}
		int[] loc = ReturnLocationTable.getReturnLocation(pc, true);
		L1Teleport.teleport(pc, loc[0], loc[1], (short) loc[2], pc.getHeading(), true);
		_monitorPc.remove(pc);
	}

	private void sendStartMonitoringLog() {
		System.out.println("マップモニタリングを開始。");
		System.out.print("MonitaringMapIdList:");
		for (int mapId : _monitoringMapList) {
			System.out.print(mapId + ", ");
		}
		System.out.println("");
	}
}
