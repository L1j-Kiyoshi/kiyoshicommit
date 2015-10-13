package jp.l1j.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import jp.l1j.server.templates.L1PhysicalAttack;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

public class PhysicalSkillsTable {

	private static Logger _log = Logger.getLogger(PhysicalSkillsTable.class.getName());

	private Map<Integer, L1PhysicalAttack> _physicalSkills = new HashMap<Integer,L1PhysicalAttack>();

	private static PhysicalSkillsTable _instance;

	public static synchronized PhysicalSkillsTable getInstance() {
		if (_instance == null) {
			_instance = new PhysicalSkillsTable();
		}
		return _instance;
	}

	private PhysicalSkillsTable() {
		loadSkills();
	}

	private void loadSkills() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM physical_skills");
			rs = pstm.executeQuery();
			while (rs.next()) {
				L1PhysicalAttack pa = new L1PhysicalAttack();
				pa.setSkillId(rs.getInt("skill_id"));
				pa.setName(rs.getString("name"));
				pa.setType(rs.getString("type"));
				pa.setRange(rs.getInt("ranged"));
				pa.setActionId(rs.getInt("action_id"));
				pa.setDamageValue(rs.getInt("damage_value"));
				pa.setDamageDice(rs.getInt("damage_dice"));
				pa.setDamageDiceCount(rs.getInt("damage_dice_count"));
				pa.setArea(rs.getInt("area"));
				pa.setBaseRL(rs.getInt("base_left_right"));
				pa.setBaseFB(rs.getInt("base_front_back"));
				pa.setAreaFront(rs.getInt("area_front"));
				pa.setAreaBack(rs.getInt("area_back"));
				pa.setAreaRight(rs.getInt("area_right"));
				pa.setAreaLeft(rs.getInt("area_left"));
				pa.setCastGfx(rs.getInt("castgfx"));
				pa.setAreaEffect(rs.getInt("area_effect"));
				_physicalSkills.put(pa.getSkillId(), pa);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Unable to load PhysicalSkillTable.", e);
		} finally {
			SqlUtil.close(rs, pstm, con);
		}
	}

	public L1PhysicalAttack findBySkillId(int skillId) {
		return _physicalSkills.get(skillId);
	}
}
