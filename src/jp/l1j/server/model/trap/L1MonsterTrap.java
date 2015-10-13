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
package jp.l1j.server.model.trap;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.l1j.server.datatables.NpcTable;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.L1Location;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.map.L1Map;
import jp.l1j.server.storage.TrapStorage;
import jp.l1j.server.templates.L1Npc;
import jp.l1j.server.types.Point;
import jp.l1j.server.utils.IdFactory;

public class L1MonsterTrap extends L1Trap {
	private static Logger _log = Logger
			.getLogger(L1MonsterTrap.class.getName());

	private final int _npcId;
	private final int _count;

	private L1Npc _npcTemp = null; // パフォーマンスのためにキャッシュ
	private Constructor _constructor = null; // パフォーマンスのためにキャッシュ

	public L1MonsterTrap(TrapStorage storage) {
		super(storage);

		_npcId = storage.getInt("monster_npc_id");
		_count = storage.getInt("monster_count");
	}

	private void addListIfPassable(List<Point> list, L1Map map, Point pt) {
		if (map.isPassable(pt)) {
			list.add(pt);
		}
	}

	private List<Point> getSpawnablePoints(L1Location loc, int d) {
		List<Point> result = new ArrayList<Point>();
		L1Map m = loc.getMap();
		int x = loc.getX();
		int y = loc.getY();
		// locを中心に、1辺dタイルの正方形を描くPointリストを作る
		for (int i = 0; i < d; i++) {
			addListIfPassable(result, m, new Point(d - i + x, i + y));
			addListIfPassable(result, m, new Point(-(d - i) + x, -i + y));
			addListIfPassable(result, m, new Point(-i + x, d - i + y));
			addListIfPassable(result, m, new Point(i + x, -(d - i) + y));
		}
		return result;
	}

	private Constructor getConstructor(L1Npc npc) throws ClassNotFoundException {
		return Class.forName(
				"jp.l1j.server.model.instance." + npc.getImpl()
						+ "Instance").getConstructors()[0];
	}

	private L1NpcInstance createNpc() throws Exception {
		if (_npcTemp == null) {
			_npcTemp = NpcTable.getInstance().getTemplate(_npcId);
		}
		if (_constructor == null) {
			_constructor = getConstructor(_npcTemp);
		}

		return (L1NpcInstance) _constructor
				.newInstance(new Object[] { _npcTemp });
	}

	private void spawn(L1Location loc) throws Exception {
		L1NpcInstance npc = createNpc();
		npc.setId(IdFactory.getInstance().nextId());
		npc.getLocation().set(loc);
		npc.setHomeX(loc.getX());
		npc.setHomeY(loc.getY());
		L1World.getInstance().storeObject(npc);
		L1World.getInstance().addVisibleObject(npc);

		npc.onNpcAI();
		npc.updateLight();
		npc.startChat(L1NpcInstance.CHAT_TIMING_APPEARANCE); // チャット開始
	}

	@Override
	public void onTrod(L1PcInstance trodFrom, L1Object trapObj) {
		sendEffect(trapObj);

		List<Point> points = getSpawnablePoints(trapObj.getLocation(), 5);
		
		// 沸ける場所が無ければ終了
		if (points.isEmpty()) {
			return;
		}

		try {
			int cnt = 0;
			while (true) {
				for (Point pt : points) {
					spawn(new L1Location(pt, trapObj.getMap()));
					cnt++;
					if (_count <= cnt) {
						return;
					}
				}
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
