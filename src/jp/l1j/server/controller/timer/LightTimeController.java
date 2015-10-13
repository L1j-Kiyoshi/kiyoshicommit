/*
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

import java.util.logging.Logger;
import jp.l1j.server.datatables.SpawnLightTable;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.gametime.L1GameTimeClock;
import jp.l1j.server.model.instance.L1FieldObjectInstance;

public class LightTimeController implements Runnable {
	private static Logger _log = Logger.getLogger(LightTimeController.class.getName());

	private static LightTimeController _instance;

	private boolean isSpawn = false;

	public static LightTimeController getInstance() {
		if (_instance == null) {
			_instance = new LightTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				checkLightTime();
				Thread.sleep(60000);
			}
		} catch (Exception e1) {
		}
	}

	private void checkLightTime() {
		int serverTime = L1GameTimeClock.getInstance().currentTime().getSeconds();
		int nowTime = serverTime % 86400;
		if (nowTime >= ((5 * 3600) + 3300) && nowTime < ((17 * 3600) + 3300)) { // 5:55~17:55
			if (isSpawn) {
				isSpawn = false;
				for (L1Object object : L1World.getInstance().getObject()) {
					if (object instanceof L1FieldObjectInstance) {
						L1FieldObjectInstance npc = (L1FieldObjectInstance) object;
						if ((npc.getNpcTemplate().getNpcId() == 81177
							|| npc.getNpcTemplate().getNpcId() == 81178
							|| npc.getNpcTemplate().getNpcId() == 81179
							|| npc.getNpcTemplate().getNpcId() == 81180
							|| npc.getNpcTemplate().getNpcId() == 81181)
							&& (npc.getMapId() == 0 || npc.getMapId() == 4)) {
							npc.deleteMe();
						}
					}
				}
			}
		} else if ((nowTime >= ((17 * 3600) + 3300) && nowTime <= 24 * 3600)
				|| (nowTime >= 0 * 3600 && nowTime < ((5 * 3600) + 3300))) { // 17:55~24:00,0:00~5:55
			if (!isSpawn) {
				isSpawn = true;
				SpawnLightTable.getInstance();
			}
		}
	}
}
