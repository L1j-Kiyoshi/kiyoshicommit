/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jp.l1j.server.controller.timer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import jp.l1j.configure.Config;
import jp.l1j.server.GeneralThreadPool;
import jp.l1j.server.datatables.UltimateBattleConfigTable;
import jp.l1j.server.datatables.UltimateBattleTimeTable;
import jp.l1j.server.templates.L1UltimateBattleTimes;

public class UltimateBattleTimeController implements Runnable {

	private static Logger _log = Logger.getLogger(UltimateBattleTimeController.class.getName());

	private static UltimateBattleTimeController _instance = null;

	public static synchronized UltimateBattleTimeController getInstance() {
		if (_instance == null) {
			_instance = new UltimateBattleTimeController();
		}
		return _instance;
	}

	private ArrayList<L1UltimateBattleTimes> _ubTimeList;

	private Map<Integer, UltimateBattleController> _ubControllerList = null;

	public UltimateBattleController getUBController(int ubId) {
		return _ubControllerList.get(ubId);
	}

	@Override
	public void run() {
		try {
			_ubControllerList = new HashMap<Integer, UltimateBattleController>();
			_ubTimeList = UltimateBattleTimeTable.getInstance().getUBTimeList();
			int maxUb = UltimateBattleConfigTable.getInstance().getMaxUltimateBattle();
			for (int i = 1; i <= maxUb; i++) { // 各UBのController生成
				_ubControllerList.put(i, new UltimateBattleController(i));
			}

			while (true) {
				checkTime(); // UB開始時間をチェック
				Thread.sleep(60000);
			}
		} catch (Exception e1) {
			_log.warning(e1.getMessage());
		}
	}

	private void checkTime() {
		Calendar now = Calendar.getInstance();
		int nowTime = (now.get(Calendar.HOUR_OF_DAY) * 100) + now.get(Calendar.MINUTE);
		Calendar ubTime = Calendar.getInstance();
		int time;
		for (L1UltimateBattleTimes ubt : _ubTimeList) {
			for (int i = 0; i < ubt.getTimes().length; i++) {
				ubTime.set(Calendar.HOUR_OF_DAY, ubt.getTimes()[i] / 100);
				ubTime.set(Calendar.MINUTE, ubt.getTimes()[i] % 100);
				ubTime.add(Calendar.MINUTE, -Config.ULTIMATE_BATTLE_NOTICE); // 告知分マイナス
				time = (ubTime.get(Calendar.HOUR_OF_DAY) * 100) + ubTime.get(Calendar.MINUTE);
				if (nowTime == time) { // 予告時間と現在時間が一致
					GeneralThreadPool.getInstance().execute(_ubControllerList.get(ubt.getUltimateBattleId()));
				}
			}
		}
	}
}
