package jp.l1j.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.server.templates.L1RaidDrop;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

public class RaidDropTable {

	private static Logger _log = Logger.getLogger(RaidDropTable.class.getName());

	/**
	 * RaidId毎のドロップリストを管理。（パターン分けはされていない）
	 */
	private HashMap<Integer, ArrayList<L1RaidDrop>> _raidDrop;

	private HashMap<Integer, Integer> _maxDropPattern;

	private static RaidDropTable _instance = null;

	public static synchronized RaidDropTable getInstance() {
		if (_instance == null) {
			_instance = new RaidDropTable();
		}
		return _instance;
	}

	private RaidDropTable() {
		load();
	}

	private void load() {
		_raidDrop = new HashMap<Integer, ArrayList<L1RaidDrop>>();
		_maxDropPattern = new HashMap<Integer, Integer>();
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("select * from raid_drop");
			rs = pstm.executeQuery();
			while (rs.next()) {
				L1RaidDrop rd = new L1RaidDrop();
				rd.setRaidId(rs.getInt("raid_id"));
				rd.setPatternId(rs.getInt("pattern_id"));
				rd.setSetId(rs.getInt("set_id"));
				rd.setItemId(rs.getInt("item_id"));
				rd.setItemName(rs.getString("item_name"));
				rd.setMinCount(rs.getInt("min_count"));
				rd.setMaxCount(rs.getInt("max_count"));
				rd.setDropChance(rs.getInt("chance"));
				if (!_raidDrop.containsKey(rd.getRaidId())) {
					_raidDrop.put(rd.getRaidId(), new ArrayList<L1RaidDrop>());
				}
				if (!_maxDropPattern.containsKey(rd.getRaidId())) {
					_maxDropPattern.put(rd.getRaidId(), rd.getPatternId());
				} else if (_maxDropPattern.get(rd.getRaidId()) < rd.getPatternId()) {
					_maxDropPattern.put(rd.getRaidId(), rd.getPatternId());
				}
				_raidDrop.get(rd.getRaidId()).add(rd);
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(rs, pstm, con);
		}
	}

	public ArrayList<L1RaidDrop> getRaidDrop(int raidId) {
		return _raidDrop.get(raidId);
	}

	public int getMaxDropPattern(int raidId) {
		if (_maxDropPattern.get(raidId) == null) {
			return 0;
		}
		return _maxDropPattern.get(raidId);
	}
}
