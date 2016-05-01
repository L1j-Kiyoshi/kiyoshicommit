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
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.server.templates.L1GetBackRestart;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.PerformanceTimer;
import jp.l1j.server.utils.SqlUtil;

public class RestartLocationTable {
	private static Logger _log = Logger.getLogger(RestartLocationTable.class.getName());

	private static RestartLocationTable _instance;

	private static HashMap<Integer, L1GetBackRestart> _restartLocations = new HashMap<Integer, L1GetBackRestart>();

	public static RestartLocationTable getInstance() {
		if (_instance == null) {
			_instance = new RestartLocationTable();
		}
		return _instance;
	}

	private RestartLocationTable() {
		loadRestartLocations(_restartLocations);
	}
	
	public void loadRestartLocations(HashMap<Integer, L1GetBackRestart> restartLocations) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM restart_locations");
			rs = pstm.executeQuery();
			while (rs.next()) {
				L1GetBackRestart gbr = new L1GetBackRestart();
				int area = rs.getInt("area");
				gbr.setArea(area);
				gbr.setLocX(rs.getInt("loc_x"));
				gbr.setLocY(rs.getInt("loc_y"));
				gbr.setMapId(rs.getShort("map_id"));
				restartLocations.put(new Integer(area), gbr);
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(rs, pstm, con);
		}
	}

	public void reload() {
		PerformanceTimer timer = new PerformanceTimer();
		System.out.print("loading restart locations...");
		HashMap<Integer, L1GetBackRestart> restartLocations = new HashMap<Integer, L1GetBackRestart>();
		loadRestartLocations(restartLocations);
		_restartLocations = restartLocations;
		System.out.println("OK! " + timer.elapsedTimeMillis() + "ms");
	}
	
	public L1GetBackRestart[] getGetBackRestartTableList() {
		return _restartLocations.values().toArray(new L1GetBackRestart[_restartLocations.size()]);
	}
}
