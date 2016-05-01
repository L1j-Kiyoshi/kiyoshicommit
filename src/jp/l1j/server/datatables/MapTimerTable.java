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

package jp.l1j.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

public class MapTimerTable {
	private static Logger _log = Logger.getLogger(MapTimerTable.class.getName());

	private static MapTimerTable _instance = null;

	public static synchronized MapTimerTable getInstance() {
		if (_instance == null) {
			_instance = new MapTimerTable();
		}
		return _instance;
	}

	public void loadMapTimer(L1PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM map_timers WHERE account_id=?");
			pstm.setInt(1, pc.getAccountId());
			rs = pstm.executeQuery();
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			while (rs.next()) {
				if (MapTable.getInstance().isAccount(rs.getInt("area_id"))) {
					map.put(rs.getInt("area_id"), rs.getInt("remaining_time"));
				}
			}
			SqlUtil.close(rs);
			SqlUtil.close(pstm);
			pstm = con.prepareStatement("SELECT * FROM map_timers WHERE char_id=?");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();
			while (rs.next()) {
				map.put(rs.getInt("area_id"), rs.getInt("remaining_time"));
			}
			pc.setMapTimer(map);
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(rs);
			SqlUtil.close(pstm);
			SqlUtil.close(con);
		}
	}

	public void saveMapTimer(L1PcInstance pc) {
		if (pc.getMapTimer().isEmpty()) {
			return;
		}
		if (pc.getMapTimer().size() <= 0) {
			return;
		}
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			for (int areaId : pc.getMapTimer().keySet()) {
				if (MapTable.getInstance().isAccount(areaId)) {
					pstm = con.prepareStatement("REPLACE INTO map_timers SET account_id=?, char_id=?, area_id=?, remaining_time=?");
					pstm.setInt(1, pc.getAccountId());
					pstm.setInt(2, 0);
					pstm.setInt(3, areaId);
					pstm.setInt(4, pc.getMapTimer().get(areaId));
					pstm.execute();
				} else {
					pstm = con.prepareStatement("REPLACE INTO map_timers SET account_id=?, char_id=?, area_id=?, remaining_time=?");
					pstm.setInt(1, pc.getAccountId());
					pstm.setInt(2, pc.getId());
					pstm.setInt(3, areaId);
					pstm.setInt(4, pc.getMapTimer().get(areaId));
					pstm.execute();
				}
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(rs);
			SqlUtil.close(pstm);
			SqlUtil.close(con);
		}
	}

	public void deleteMapTimer(int areaId) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM map_timers WHERE area_id=?");
			pstm.setInt(1, areaId);
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(pstm);
			SqlUtil.close(con);
		}
	}
}
