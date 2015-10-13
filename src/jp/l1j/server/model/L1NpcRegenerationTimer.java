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
package jp.l1j.server.model;

import java.util.Timer;

/**
 * NPCの自然回復TimerTaskをスケジュールするためのTimerクラス
 * 
 */
public class L1NpcRegenerationTimer extends Timer {

	private static L1NpcRegenerationTimer _instance = null;

	private L1NpcRegenerationTimer() {
	}

	public static L1NpcRegenerationTimer getInstance() {
		if (_instance == null) {
			_instance = new L1NpcRegenerationTimer();
		}
		return _instance;
	}

}
