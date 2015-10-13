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

package jp.l1j.server.model.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import jp.l1j.server.codes.ActionCodes;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.inventory.L1Inventory;

/**
 * @author Owner
 *
 */
public class L1InstanceMap {
		private static Logger _log = Logger.getLogger(L1InstanceMap.class.getName());
		private int _curId;

		private Object _monitor = new Object();

		private static final int ICE_QUEEN_CASTLE_START = 2101;
		private static final int ICE_QUEEN_CASTLE_END = 2150;

		private static final int ICE_DAEMON_CASTLE_START = 2151;
		private static final int ICE_DAEMON_CASTLE_END = 2200;

		private static final int MILITARY_TRAINING_AREA_START = 1400;
		private static final int MILITARY_TRAINING_AREA_END = 148;

		private static final short FIRST_ID = 10000;
		private static final short LAST_ID = 12000;

		private static L1InstanceMap _instance;
		public static L1InstanceMap getInstance() {
			if (_instance == null) {
				_instance = new L1InstanceMap();
			}
			return _instance;
		}

	private final List<Integer> _instanceMapList = new ArrayList<Integer>();

	L1InstanceMap(){
		_curId = FIRST_ID;
	}
	/**
	 * インスタンスマップを追加する
	 *
	 * @param mapId 基準となるマップ（コピー元）
	 * @return 生成したマップのID
	 */
	public int addInstanceMap(int mapId) {
		L1Map instanceMap = L1WorldMap.getInstance().getMap((short) mapId).clone();
		int nextId;
		if (mapId == ICE_QUEEN_CASTLE_START) { // IQ城追加
			nextId = ICE_QUEEN_CASTLE_START + 1;
			while (_instanceMapList.contains(nextId)) {
				nextId++;
			}
		} else if (mapId == ICE_DAEMON_CASTLE_START) { // ID城追加
			nextId = ICE_DAEMON_CASTLE_START + 1;
			while (_instanceMapList.contains(nextId)) {
				nextId++;
			}
		} else {
			nextId = nextId();
			while(_instanceMapList.contains(nextId)){
				nextId = nextId();
			}
		}
		instanceMap.setId(nextId);
		L1WorldMap.getInstance().addMap(instanceMap);
		_instanceMapList.add(instanceMap.getId());
		return instanceMap.getId();
	}
	/**
	 * 任意のMapIdでインスタンスマップの作成を試みる。
	 *
	 * @param baseMapId 基準となるマップ（コピー元）
	 * @param instanceMapId コピー先のMAPID
	 * @return 生成出来ればtrue、出来なければfalse
	 */
	public boolean addInstanceMap(int baseMapId, int instanceMapId) {
		if (_instanceMapList.contains(instanceMapId)) {
			return false;
		}
		L1Map instanceMap = L1WorldMap.getInstance().getMap((short) baseMapId).clone();
		instanceMap.setId(instanceMapId);
		L1WorldMap.getInstance().addMap(instanceMap);
		_instanceMapList.add(instanceMap.getId());
		return true;
	}

	/**
	 * インスタンスマップを削除する
	 *
	 * @param mapId 削除対象のマップのID
	 * @return true 成功 false 失敗
	 */
	public boolean removeInstanceMap(int mapId) {
		if (!_instanceMapList.contains(mapId)) {
			return false;
		}
		for (Object obj : L1World.getInstance().getVisibleObjects(mapId).values()) {
			if (obj instanceof L1NpcInstance) { // モンスター削除
				L1NpcInstance npc = (L1NpcInstance) obj;
				if (!npc.isDead()) {
					npc.setDead(true);
					npc.setStatus(ActionCodes.ACTION_Die);
					npc.setCurrentHpDirect(0);
					npc.deleteMe();
				}
			} else if (obj instanceof L1Inventory) { // アイテム削除
				L1Inventory inventory = (L1Inventory) obj;
				inventory.clearItems();
			}
		}
		for (Iterator<Integer> i =_instanceMapList.listIterator(); i.hasNext();) {
			int key = i.next();  //次の要素の呼び出し
			if(key == mapId){
				i.remove();  //要素の削除
				break;
			}
		}
		L1WorldMap.getInstance().removeMap(mapId);
		return true;
	}

	public List<Integer> getInstanceMap() {
		return _instanceMapList;
	}

	public int getNumOfInstanceMaps() {
		return _instanceMapList.size();
	}

	public int nextId() {
		synchronized (_monitor) {
			if(_curId > LAST_ID){
				_curId = FIRST_ID;
			}
			return _curId++;
		}
	}
}
