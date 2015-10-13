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
package jp.l1j.server.model;

import jp.l1j.server.model.map.L1Map;
import jp.l1j.server.model.map.L1WorldMap;
import jp.l1j.server.types.Rectangle;

public class L1MapArea extends Rectangle {
	private L1Map _map = L1Map.newNull();

	public L1Map getMap() {
		return _map;
	}

	public void setMap(L1Map map) {
		_map = map;
	}

	public int getMapId() {
		return _map.getId();
	}

	public L1MapArea(int left, int top, int right, int bottom, int mapId) {
		super(left, top, right, bottom);

		_map = L1WorldMap.getInstance().getMap((short) mapId);
	}

	public boolean contains(L1Location loc) {
		return (_map.getId() == loc.getMap().getId()) && super.contains(loc);
	}
}
