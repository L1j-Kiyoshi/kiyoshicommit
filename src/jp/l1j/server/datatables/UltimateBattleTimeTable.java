package jp.l1j.server.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import jp.l1j.server.templates.L1UltimateBattleTimes;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

public class UltimateBattleTimeTable {

	private static Logger _log = Logger.getLogger(UltimateBattleTimeTable.class.getName());

	private static UltimateBattleTimeTable _instance = null;

	public static synchronized UltimateBattleTimeTable getInstance() {
		if (_instance == null) {
			_instance = new UltimateBattleTimeTable();
		}
		return _instance;
	}

	private ArrayList<L1UltimateBattleTimes> _ubTimeList = new ArrayList<L1UltimateBattleTimes>();

	public ArrayList<L1UltimateBattleTimes> getUBTimeList() {
		return _ubTimeList;
	}

	private UltimateBattleTimeTable() {
		load();
	}

	private void load() {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM ub_times");
			rs = pstm.executeQuery();
			String[] timeString;
			int[] timesInt;
			while (rs.next()) {
				L1UltimateBattleTimes ubt = new L1UltimateBattleTimes();
				ubt.setUltimateBattleId(rs.getInt("ub_id"));
				timeString = rs.getString("ub_time").split(",");
				timesInt = new int[timeString.length];
				for (int i = 0; i < timeString.length; i++) {
					timesInt[i] = Integer.parseInt(timeString[i]);
				}
				ubt.setTimes(timesInt);
				_ubTimeList.add(ubt);
			}
		} catch (SQLException e) {
			_log.warning("ubconfig couldnt be initialized:" + e);
		} finally {
			SqlUtil.close(rs, pstm, con);
		}
	}
}
