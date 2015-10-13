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
package jp.l1j.server.templates;

import java.util.logging.Logger;

public class L1Drop {
	private static Logger _log = Logger.getLogger(L1Drop.class.getName());

	int _mobId;
	int _itemId;
	int _min;
	int _max;
	int _chance;

	public L1Drop(int mobId, int itemId, int min, int max, int chance) {
		_mobId = mobId;
		_itemId = itemId;
		_min = min;
		_max = max;
		_chance = chance;
	}

	public int getChance() {
		return _chance;
	}

	public int getItemid() {
		return _itemId;
	}

	public int getMax() {
		return _max;
	}

	public int getMin() {
		return _min;
	}

	public int getMobid() {
		return _mobId;
	}
}
