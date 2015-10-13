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

package jp.l1j.server.model.gametime;

import java.util.Timer;
import java.util.TimerTask;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_GameTime;

public class L1GameTimeCarrier extends TimerTask {
	private static final Timer _timer = new Timer();
	private L1PcInstance _pc;

	public L1GameTimeCarrier(L1PcInstance pc) {
		_pc = pc;
	}

	@Override
	public void run() {
		try {
			if (_pc.getNetConnection() == null) {
				cancel();
				return;
			}

			int serverTime = L1GameTimeClock.getInstance().currentTime()
					.getSeconds();
			if (serverTime % 300 == 0) {
				_pc.sendPackets(new S_GameTime(serverTime));
			}
		} catch (Exception e) {
			// ignore
		}
	}

	public void start() {
		_timer.scheduleAtFixedRate(this, 0, 500);
	}

	public void stop() {
		cancel();
	}
}
