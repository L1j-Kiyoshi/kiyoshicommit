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

import jp.l1j.server.model.map.L1Map;
import jp.l1j.server.model.map.L1WorldMap;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.types.Point;

public class L1Location extends Point {
	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	protected L1Map _map = L1Map.newNull();

	public L1Location() {
		super();
	}

	public L1Location(L1Location loc) {
		this(loc._x, loc._y, loc._map);
	}

	public L1Location(int x, int y, int mapId) {
		super(x, y);
		setMap(mapId);
	}

	public L1Location(int x, int y, L1Map map) {
		super(x, y);
		_map = map;
	}

	public L1Location(Point pt, int mapId) {
		super(pt);
		setMap(mapId);
	}

	public L1Location(Point pt, L1Map map) {
		super(pt);
		_map = map;
	}

	public void set(L1Location loc) {
		_map = loc._map;
		_x = loc._x;
		_y = loc._y;
	}

	public void set(int x, int y, int mapId) {
		set(x, y);
		setMap(mapId);
	}

	public void set(int x, int y, L1Map map) {
		set(x, y);
		_map = map;
	}

	public void set(Point pt, int mapId) {
		set(pt);
		setMap(mapId);
	}

	public void set(Point pt, L1Map map) {
		set(pt);
		_map = map;
	}

	public L1Map getMap() {
		return _map;
	}

	public int getMapId() {
		return _map.getId();
	}

	public void setMap(L1Map map) {
		_map = map;
	}

	public void setMap(int mapId) {
		_map = L1WorldMap.getInstance().getMap((short) mapId);
	}

	/**
	 * このLocationがmin～maxのLocationの範囲内かどうか。
	 * @param min
	 * @param max
	 * @return
	 * 		範囲内ならばtrue
	 */
	public boolean isRangeLocation(L1Location min, L1Location max) {
		if (getX() < min.getX() || max.getX() < getX()) { // Xの値が範囲外
			return false;
		}
		if (getY() < min.getY() || max.getY() < getY()) { // Yの値が範囲外
			return false;
		}
		if (getMapId() < min.getMapId() || max.getMapId() < getMapId()) { // MAPIDが範囲外
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof L1Location)) {
			return false;
		}
		L1Location loc = (L1Location) obj;
		return (this.getMap() == loc.getMap()) && (this.getX() == loc.getX())
				&& (this.getY() == loc.getY());
	}

	@Override
	public int hashCode() {
		return 7 * _map.getId() + super.hashCode();
	}

	@Override
	public String toString() {
		return String.format("(%d, %d) on %d", _x, _y, _map.getId());
	}

	/**
	 * このLocationに対する、移動可能なランダム範囲のLocationを返す。
	 * ランダムテレポートの場合は、城エリア、アジト内のLocationは返却されない。
	 *
	 * @param max
	 *            ランダム範囲の最大値
	 * @param isRandomTeleport
	 *            ランダムテレポートか
	 * @return 新しいLocation
	 */
	public L1Location randomLocation(int max, boolean isRandomTeleport) {
		return randomLocation(0, max, isRandomTeleport);
	}

	/**
	 * このLocationに対する、移動可能なランダム範囲のLocationを返す。
	 * ランダムテレポートの場合は、城エリア、アジト内のLocationは返却されない。
	 *
	 * @param min
	 *            ランダム範囲の最小値(0で自身の座標を含む)
	 * @param max
	 *            ランダム範囲の最大値
	 * @param isRandomTeleport
	 *            ランダムテレポートか
	 * @return 新しいLocation
	 */
	public L1Location randomLocation(int min, int max, boolean isRandomTeleport) {
		return L1Location.randomLocation(this, min, max, isRandomTeleport);
	}

	/**
	 * 引数のLocationに対して、移動可能なランダム範囲のLocationを返す。
	 * ランダムテレポートの場合は、城エリア、アジト内のLocationは返却されない。
	 *
	 * @param baseLocation
	 *            ランダム範囲の元になるLocation
	 * @param min
	 *            ランダム範囲の最小値(0で自身の座標を含む)
	 * @param max
	 *            ランダム範囲の最大値
	 * @param isRandomTeleport
	 *            ランダムテレポートか
	 * @return 新しいLocation
	 */
	public static L1Location randomLocation(L1Location baseLocation, int min,
			int max, boolean isRandomTeleport) {
		if (min > max) {
			throw new IllegalArgumentException("param is invalid. (min > max)");
		}
		if (max <= 0) {
			return new L1Location(baseLocation);
		}
		if (min < 0) {
			min = 0;
		}
		L1Location newLocation = new L1Location();
		int newX = 0;
		int newY = 0;
		int locX = baseLocation.getX();
		int locY = baseLocation.getY();
		short mapId = (short) baseLocation.getMapId();
		L1Map map = baseLocation.getMap();
		newLocation.setMap(map);
		int locX1 = locX - max;
		int locX2 = locX + max;
		int locY1 = locY - max;
		int locY2 = locY + max;
		// map範囲
		int mapX1 = map.getX();
		int mapX2 = mapX1 + map.getWidth();
		int mapY1 = map.getY();
		int mapY2 = mapY1 + map.getHeight();
		// 最大でもマップの範囲内までに補正
		if (locX1 < mapX1) {
			locX1 = mapX1;
		}
		if (locX2 > mapX2) {
			locX2 = mapX2;
		}
		if (locY1 < mapY1) {
			locY1 = mapY1;
		}
		if (locY2 > mapY2) {
			locY2 = mapY2;
		}
		int diffX = locX2 - locX1; // x方向
		int diffY = locY2 - locY1; // y方向
		int trial = 0;
		// 試行回数を範囲最小値によってあげる為の計算
		int amax = (int) Math.pow(1 + (max * 2), 2);
		int amin = (min == 0) ? 0 : (int) Math.pow(1 + ((min - 1) * 2), 2);
		int trialLimit = 40 * amax / (amax - amin);
		while (true) {
			if (trial >= trialLimit) {
				newLocation.set(locX, locY);
				break;
			}
			trial++;
			newX = locX1 + L1Location._random.nextInt(diffX + 1);
			newY = locY1 + L1Location._random.nextInt(diffY + 1);
			newLocation.set(newX, newY);
			if (baseLocation.getTileLineDistance(newLocation) < min) {
				continue;
			}
			if (isRandomTeleport) { // ランダムテレポートの場合
				if (L1CastleLocation.checkInAllWarArea(newX, newY, mapId)) { // いずれかの城エリア
					continue;
				}
				// いずれかのアジト内
				if (L1HouseLocation.isInHouse(newX, newY, mapId)) {
					continue;
				}
			}
			if (map.isInMap(newX, newY) && map.isPassable(newX, newY)) {
				break;
			}
		}
		return newLocation;
	}

	/**
	 * ブックマーク禁止エリアの設定
	 */
	public static boolean noMarkableLocation(int locX, int locY, short mapId){
		boolean isNoMarkable = false;
		if (locX >= 33490 && locX <= 33819 && locY >= 32212 // 火山エリア
				&& locY <= 32433 && mapId == 4){
			isNoMarkable = true;
		}
		if (locX >= 34238 && locX <= 34283 && locY >= 33105 // ジャイアント（傲慢の塔入口）エリア
				&& locY <= 33510 && mapId == 4){
			isNoMarkable = true;
		}
		return isNoMarkable;
	}
}
