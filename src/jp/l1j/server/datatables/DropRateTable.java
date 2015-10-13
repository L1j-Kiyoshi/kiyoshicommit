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
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.PerformanceTimer;
import jp.l1j.server.utils.SqlUtil;

public final class DropRateTable {
	private class dropItemData {
		public double dropRate = 1;
		public double dropAmount = 1;
		public double uniqueRate = 1;
	}

	private static Logger _log = Logger.getLogger(DropRateTable.class.getName());

	private static DropRateTable _instance;

	private static Map<Integer, dropItemData> _dropItems = new HashMap<Integer, dropItemData>();

	public static DropRateTable getInstance() {
		if (_instance == null) {
			_instance = new DropRateTable();
		}
		return _instance;
	}

	private DropRateTable() {
		load();
	}

	private void loadDropItems(Map<Integer, dropItemData> dropItems) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM drop_rates");
			for (rs = pstm.executeQuery(); rs.next();) {
				dropItemData data = new dropItemData();
				int itemId = rs.getInt("item_id");
				data.dropRate = rs.getDouble("drop_rate");
				data.dropAmount = rs.getDouble("drop_amount");
				data.uniqueRate = rs.getDouble("unique_rate");
				dropItems.put(new Integer(itemId), data);
			}
			_log.config("drop_rates " + dropItems.size());
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(rs, pstm, con);
		}
	}

	private void load() {
		loadDropItems(_dropItems);
	}
	
	public void reload() {
		PerformanceTimer timer = new PerformanceTimer();
		System.out.print("loading drop rates...");
		Map<Integer, dropItemData> dropItems = new HashMap<Integer, dropItemData>();
		loadDropItems(dropItems);
		_dropItems = dropItems;
		System.out.println("OK! " + timer.elapsedTimeMillis() + "ms");
	}
	
	public double getDropRate(int itemId) {
		dropItemData data = _dropItems.get(itemId);
		if (data == null) {
			return 1;
		}
		return data.dropRate;
	}

	public double getDropAmount(int itemId) {
		dropItemData data = _dropItems.get(itemId);
		if (data == null) {
			return 1;
		}
		return data.dropAmount;
	}

	public double getUniqueRate(int itemId) {
		dropItemData data = _dropItems.get(itemId);
		if (data == null) {
			return 1;
		}
		return data.uniqueRate;
	}
}
