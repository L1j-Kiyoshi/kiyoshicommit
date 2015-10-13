package jp.l1j.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.server.controller.timer.DistortionOfTimeController;
import jp.l1j.server.model.L1Location;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

public class DistortionTable {

	private static Logger _log = Logger.getLogger(DistortionTable.class.getName());

	private static DistortionTable _instance = null;

	public static DistortionTable getInstance() {
		if (_instance == null) {
			_instance = new DistortionTable();
		}
		return _instance;
	}

	private DistortionTable() {
		load();
	}

	public void load() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM distortion WHERE id = 1");
			rs = pstm.executeQuery();
			L1Location distortionLoc = new L1Location();
			while (rs.next()) {
				DistortionOfTimeController.getInstance().setNextTime(timestampToCalendar((Timestamp) rs.getObject("next_time")));
				DistortionOfTimeController.getInstance().setCloseTime(timestampToCalendar((Timestamp) rs.getObject("close_time")));
				distortionLoc.set(rs.getInt("distortionX"), rs.getInt("distortionY"), rs.getInt("distortionMapId"));
				DistortionOfTimeController.getInstance().setDistortion(distortionLoc);
				DistortionOfTimeController.getInstance().setTeleporLocation(rs.getInt("teleportMapId"));
			}
		} catch (SQLException e) {
			throw new RuntimeException("Unable to load DistortionTable.", e);
		} finally {
			SqlUtil.close(rs, pstm, con);
		}
	}

	public void deleteDistortion() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM distortion");
			pstm.execute();
		} catch (SQLException e) {
			throw new RuntimeException("Unable to load DistortionTable.", e);
		} finally {
			SqlUtil.close(rs, pstm, con);
		}
	}

	public void saveDistortion() {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE distortion SET next_time=?, close_time=?, distortionX=?, distortionY=?, distortionMapId=?, teleportMapId=? WHERE id=?");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String nextTime = sdf.format(DistortionOfTimeController.getInstance().getNextTime().getTime());
			String closeTime = sdf.format(DistortionOfTimeController.getInstance().getCloseTime().getTime());
			pstm.setString(1, nextTime);
			pstm.setString(2, closeTime);
			if (DistortionOfTimeController.getInstance().getDistortion().getLocation() != null) {
				pstm.setInt(3, DistortionOfTimeController.getInstance().getDistortion().getX());
				pstm.setInt(4, DistortionOfTimeController.getInstance().getDistortion().getY());
				pstm.setInt(5, DistortionOfTimeController.getInstance().getDistortion().getMapId());
			} else {
				pstm.setInt(3, 0);
				pstm.setInt(4, 0);
				pstm.setInt(5, 0);
			}
			if (DistortionOfTimeController.getInstance().getTeleportLocation() != null) {
				pstm.setInt(6, DistortionOfTimeController.getInstance().getTeleportLocation().getMapId());
			} else {
				pstm.setInt(6, 0);
			}
			pstm.setInt(7, 1);
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(pstm);
			SqlUtil.close(con);
		}
	}

	public void updateDistortionTime() {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE distortion SET next_time=?, close_time=? WHERE id=?");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String nextTime = sdf.format(DistortionOfTimeController.getInstance().getNextTime().getTime());
			String closeTime = sdf.format(DistortionOfTimeController.getInstance().getCloseTime().getTime());
			pstm.setString(1, nextTime);
			pstm.setString(2, closeTime);
			pstm.setInt(3, 1);
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(pstm);
			SqlUtil.close(con);
		}
	}

	private Calendar timestampToCalendar(Timestamp ts) {
		if (ts == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(ts.getTime());
		return cal;
	}
}
