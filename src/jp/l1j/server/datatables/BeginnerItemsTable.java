package jp.l1j.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import jp.l1j.server.templates.L1BeginnerItems;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

public class BeginnerItemsTable {

	private static Logger _log = Logger.getLogger(BeginnerItemsTable.class.getName());

	private static BeginnerItemsTable _instance = null;

	public static synchronized BeginnerItemsTable getInstance() {
		if (_instance == null) {
			_instance = new BeginnerItemsTable();
		}
		return _instance;
	}

	private BeginnerItemsTable() {
		loadBeginnerItems();
	}

	private ArrayList<L1BeginnerItems> _allBeginnerItems = new ArrayList<L1BeginnerItems>();

	private void loadBeginnerItems() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM beginner_items");
			rs = pstm.executeQuery();
			while (rs.next()) {
				L1BeginnerItems bi = new L1BeginnerItems();
				bi.setId(rs.getInt("id"));
				bi.setItemId(rs.getInt("item_id"));
				bi.setItemCount(rs.getInt("item_count"));
				bi.setChargeCount(rs.getInt("charge_count"));
				bi.setEnchantLevel(rs.getInt("enchant_level"));
				bi.setClassInitial(getArray(rs.getString("class_initial"), ","));
				_allBeginnerItems.add(bi);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Unable to load PhysicalSkillTable.", e);
		} finally {
			SqlUtil.close(rs, pstm, con);
		}
	}

	private static String[] getArray(String s, String sToken) {
		StringTokenizer st = new StringTokenizer(s, sToken);
		int size = st.countTokens();
		String temp = null;
		String[] array = new String[size];
		for (int i = 0; i < size; i++) {
			temp = st.nextToken();
			array[i] = String.valueOf(temp);
		}
		return array;
	}

	public ArrayList<L1BeginnerItems> getAllBeginnerItems() {
		return _allBeginnerItems;
	}
}
