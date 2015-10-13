package jp.l1j.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.server.templates.L1RaidConfig;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

public class RaidConfigTable {

	private static Logger _log = Logger.getLogger(RaidConfigTable.class.getName());

	private static RaidConfigTable _instance = null;

	private static HashMap<Integer, L1RaidConfig> _raidConfigLists; // レイド毎の設定

	public static synchronized RaidConfigTable getInstance() {
		if (_instance == null) {
			_instance = new RaidConfigTable();
		}
		return _instance;
	}

	private RaidConfigTable() {
		load();
	}

	private void load() {
		_raidConfigLists = new HashMap<Integer, L1RaidConfig>();
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("select * from raid_config");
			rs = pstm.executeQuery();
			while (rs.next()) {
				L1RaidConfig rc = new L1RaidConfig();
				rc.setRaidId(rs.getInt("raid_id"));
				rc.setRaidName(rs.getString("raid_name"));
				rc.setMinLevel(rs.getInt("min_level"));
				rc.setMaxLevel(rs.getInt("max_level"));
				rc.setMinPlayer(rs.getInt("min_player"));
				rc.setMaxPlayer(rs.getInt("max_player"));
				rc.setMaxRaidLimit(rs.getInt("max_raid_limit"));
				_raidConfigLists.put(rc.getRaidId(), rc);
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(rs, pstm, con);
		}
	}

	public L1RaidConfig getRaidConfig(int raidId) {
		return _raidConfigLists.get(raidId);
	}

}
