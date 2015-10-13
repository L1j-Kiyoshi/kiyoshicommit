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

import jp.l1j.server.controller.timer.EventController;
import jp.l1j.server.templates.L1Event;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

public class EventTable {

	private static Logger _log = Logger.getLogger(EventTable.class.getName());

	private static EventTable _instance = null;

	public static synchronized EventTable getInstance() {
		if (_instance == null) {
			_instance = new EventTable();
		}
		return _instance;
	}

	private EventTable() {
		load();
	}

	private void load() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM events");
			rs = pstm.executeQuery();
			while (rs.next()) {
				L1Event event = new L1Event();
				event.setEventId(rs.getInt("event_id"));
				event.setName(rs.getString("name"));
				event.setStartTime(timestampToCalendar((Timestamp) rs.getObject("start_time")));
				event.setEndTime(timestampToCalendar((Timestamp) rs.getObject("end_time")));
				event.setNextStart(rs.getString("next_start"));
				EventController.getInstance().addEvent(event);
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(rs);
			SqlUtil.close(pstm);
			SqlUtil.close(con);
		}
	}

	/**
	 * このイベントの次の開催時間をデータベースに保存する。
	 * @param L1Event event
	 */
	public void updateEvent(L1Event event) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE events SET start_time=?, end_time=? WHERE event_id=?");
			pstm.setString(1, sdf.format(event.getStartTime().getTime()));
			pstm.setString(2, sdf.format(event.getEndTime().getTime()));
			pstm.setInt(3, event.getEventId());
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(pstm);
			SqlUtil.close(con);
		}
	}

	private Calendar timestampToCalendar(Timestamp ts) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(ts.getTime());
		return cal;
	}
}
