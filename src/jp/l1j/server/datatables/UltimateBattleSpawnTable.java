package jp.l1j.server.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.server.templates.L1SpawnUltimateBattle;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

public class UltimateBattleSpawnTable {

	private static Logger _log = Logger.getLogger(UltimateBattleSpawnTable.class.getName());

	private static UltimateBattleSpawnTable _instance;

	public static synchronized UltimateBattleSpawnTable getInstance() {
		if (_instance == null) {
			_instance = new UltimateBattleSpawnTable();
		}
		return _instance;
	}

	/**
	 * アルティメットバトルのモンスター召喚リストを取得する。
	 * @param ubId
	 * 			取得したいUBID
	 * @param pattern
	 * 			取得したいパターン
	 * @param group
	 * 			取得したいグループ
	 * @return
	 */
	public ArrayList<L1SpawnUltimateBattle> getSpawnList(int ubId, int pattern, int group) {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		ArrayList<L1SpawnUltimateBattle> list = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM spawn_ub_mobs WHERE ub_id=? AND pattern=? AND group_id=? ORDER BY id ASC");
			pstm.setInt(1, ubId);
			pstm.setInt(2, pattern);
			pstm.setInt(3, group);
			rs = pstm.executeQuery();
			list = new ArrayList<L1SpawnUltimateBattle>();
			while (rs.next()) {
				L1SpawnUltimateBattle sub = new L1SpawnUltimateBattle();
				sub.setMonsterId(rs.getInt("npc_id"));
				sub.setCount(rs.getInt("count"));
				sub.setSpawnDelay(rs.getInt("spawn_delay"));
				sub.setDrop(rs.getInt("is_drop") == 0);
				sub.setSealCount(rs.getInt("seal_count"));
				list.add(sub);
			}
		} catch (SQLException e) {
			_log.warning("ubsettings couldnt be initialized:" + e);
		} finally {
			SqlUtil.close(rs, pstm, con);
		}
		return list;
	}

	/**
	 * アルティメットバトルのモンスター召喚リストを取得する。
	 * @param ubId
	 * 			取得したいUBID
	 */
	public int getMaxPattern(int ubId) {
		int n = 0;
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT MAX(pattern) FROM spawn_ub_mobs WHERE ub_id=?");
			pstm.setInt(1, ubId);
			rs = pstm.executeQuery();
			if (rs.next()) {
				n = rs.getInt(1);
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(rs);
			SqlUtil.close(pstm);
			SqlUtil.close(con);
		}
		return n;
	}
}
