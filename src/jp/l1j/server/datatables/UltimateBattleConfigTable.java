package jp.l1j.server.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.server.templates.L1UltimateBattleConfig;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

public class UltimateBattleConfigTable {

	private static Logger _log = Logger.getLogger(UltimateBattleConfigTable.class.getName());

	private static UltimateBattleConfigTable _instance = null;

	public static synchronized UltimateBattleConfigTable getInstance() {
		if (_instance == null) {
			_instance = new UltimateBattleConfigTable();
		}
		return _instance;
	}

	private Map<Integer, L1UltimateBattleConfig> _UBConfig;

	public Map<Integer, L1UltimateBattleConfig> getUBConfig() {
		return _UBConfig;
	}

	private UltimateBattleConfigTable() {
		loadUBConfig();
	}

	public void loadUBConfig() {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		_UBConfig = new HashMap<Integer, L1UltimateBattleConfig>();
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM ub_config");
			rs = pstm.executeQuery();

			while (rs.next()) {
				L1UltimateBattleConfig ub = new L1UltimateBattleConfig();
				ub.setUltimateBattleId(rs.getInt("id"));
				ub.setManagerId(rs.getInt("manager_id"));
				ub.setManagerX(rs.getInt("manager_x"));
				ub.setManagerY(rs.getInt("manager_y"));
				ub.setManagerMapId(rs.getInt("manager_map_id"));
				ub.setManagerHeading(rs.getInt("manager_heading"));
				ub.setSubManagerId(rs.getInt("sub_manager_id"));
				ub.setSubManagerX(rs.getInt("sub_manager_x"));
				ub.setSubManagerY(rs.getInt("sub_manager_y"));
				ub.setSubManagerMapId(rs.getInt("sub_manager_map_id"));
				ub.setSubManagerHeading(rs.getInt("sub_manager_heading"));
				ub.setMapId(rs.getShort("map_id"));
				ub.setLocX1(rs.getInt("area_x1"));
				ub.setLocY1(rs.getInt("area_y1"));
				ub.setLocX2(rs.getInt("area_x2"));
				ub.setLocY2(rs.getInt("area_y2"));
				ub.setMinLevel(rs.getInt("min_level"));
				ub.setMaxLevel(rs.getInt("max_level"));
				ub.setMaxPlayer(rs.getInt("max_player"));
				ub.setEnterRoyal(rs.getBoolean("enter_royal"));
				ub.setEnterKnight(rs.getBoolean("enter_knight"));
				ub.setEnterMage(rs.getBoolean("enter_wizard"));
				ub.setEnterElf(rs.getBoolean("enter_elf"));
				ub.setEnterDarkelf(rs.getBoolean("enter_darkelf"));
				ub.setEnterDragonKnight(rs.getBoolean("enter_dragonknight"));
				ub.setEnterIllusionist(rs.getBoolean("enter_illusionist"));
				ub.setEnterMale(rs.getBoolean("enter_male"));
				ub.setEnterFemale(rs.getBoolean("enter_female"));
				ub.setUsePot(rs.getBoolean("use_pot"));
				ub.setHpr(rs.getInt("hpr_bonus"));
				ub.setMpr(rs.getInt("mpr_bonus"));
				ub.resetLoc();
				_UBConfig.put(ub.getUltimateBattleId(), ub);
			}
		} catch (SQLException e) {
			_log.warning("ubconfig couldnt be initialized:" + e);
		} finally {
			SqlUtil.close(rs, pstm, con);
		}
	}

	/**
	 * アルティメットバトルの最大数を返す
	 * @return UBの最大数
	 */
	public int getMaxUltimateBattle() {
		int n = 0;
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT MAX(id) FROM ub_config");
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

	/**
	 * npcが関連しているUBのIDを返す。
	 * @param npcId
	 * @return
	 * 		引数のNpcが関係しているUBのIDを返す。
	 */
	public int getUltimateBattleIdForNpc(int npcId) {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		int ubId = 0;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT id FROM ub_config WHERE manager_id = ? OR sub_manager_id = ?");
			pstm.setInt(1, npcId);
			pstm.setInt(2, npcId);
			rs = pstm.executeQuery();
			if (rs.next()) {
				ubId = rs.getInt("id");
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(rs);
			SqlUtil.close(pstm);
			SqlUtil.close(con);
		}
		return ubId;
	}
}