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
package jp.l1j.server.templates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.server.model.skill.executor.L1BuffSkillExecutor;
import jp.l1j.server.model.skill.executor.L1SkillExecutor;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.utils.ReflectionUtil;
import jp.l1j.server.utils.StringUtil;

public class L1Skill {

	private static Logger _log = Logger.getLogger(L1Skill.class.getName());

	public static final int ATTR_NONE = 0;

	public static final int ATTR_EARTH = 1;

	public static final int ATTR_FIRE = 2;

	public static final int ATTR_WATER = 4;

	public static final int ATTR_WIND = 8;

	public static final int ATTR_RAY = 16;

	public static final int TYPE_PROBABILITY = 1;

	public static final int TYPE_CHANGE = 2;

	public static final int TYPE_CURSE = 4;

	public static final int TYPE_DEATH = 8;

	public static final int TYPE_HEAL = 16;

	public static final int TYPE_RESTORE = 32;

	public static final int TYPE_ATTACK = 64;

	public static final int TYPE_OTHER = 128;

	public static final int TARGET_TO_ME = 0;

	public static final int TARGET_TO_PC = 1;

	public static final int TARGET_TO_NPC = 2;

	public static final int TARGET_TO_CLAN = 4;

	public static final int TARGET_TO_PARTY = 8;

	public static final int TARGET_TO_PET = 16;

	public static final int TARGET_TO_PLACE = 32;

	private static final String DEFAULT_PACKAGE = "jp.l1j.server.model.skill.executor";

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private int _skillId;

	private L1Skill() {
	}

	public int getSkillId() {
		return _skillId;
	}

	private String _name;

	public String getName() {
		return _name;
	}

	private int _baseSkillId;

	public int getBaseSkillId() {
		return _baseSkillId;
	}

	private int _skillLevel;

	public int getSkillLevel() {
		return _skillLevel;
	}

	private int _skillNumber;

	public int getSkillNumber() {
		return _skillNumber;
	}

	private int _consumeMp;

	public int getConsumeMp() {
		return _consumeMp;
	}

	private int _consumeHp;

	public int getConsumeHp() {
		return _consumeHp;
	}

	private int _consumeItmeId;

	public int getConsumeItemId() {
		return _consumeItmeId;
	}

	private int _consumeAmount;

	public int getConsumeAmount() {
		return _consumeAmount;
	}

	private int _reuseDelay; // 単位：ミリ秒

	public int getReuseDelay() {
		return _reuseDelay;
	}

	private int _removeDelay; // 単位：ミリ秒

	public int getRemoveDelay() {
		return _removeDelay;
	}

	private ArrayList<Double> _buffDuration = new ArrayList<Double>(); // 単位：秒

	public void setBuffDuration(ArrayList<Double> al) {
		_buffDuration = al;
	}

	public double getBuffDuration() {
		double buffDuration = 0.0D;
		try {
			buffDuration = _buffDuration.get(_random.nextInt(_buffDuration.size()));
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return buffDuration;
	}

	private String _target;

	public String getTarget() {
		return _target;
	}

	private int _targetTo; // 対象 0:自分 1:PC 2:NPC 4:血盟 8:パーティ 16:ペット 32:場所

	public int getTargetTo() {
		return _targetTo;
	}

	private double _damageValue;

	public double getDamageValue() {
		return _damageValue;
	}

	private int _damageDice;

	public int getDamageDice() {
		return _damageDice;
	}

	private int _damageDiceCount;

	public int getDamageDiceCount() {
		return _damageDiceCount;
	}

	private int _probabilityValue;

	public int getProbabilityValue() {
		return _probabilityValue;
	}

	private int _probabilityDice;

	public int getProbabilityDice() {
		return _probabilityDice;
	}

	private int _MrValue;

	public int getMrValue() {
		return _MrValue;
	}

	private double _MrDice;

	public double getMrDice() {
		return _MrDice;
	}

	private int _probabilityMax;

	public int getProbabilityMax() {
		return _probabilityMax;
	}

	private int _attr;

	/**
	 * スキルの属性を返す。<br>
	 * 0.無属性魔法,1.地魔法,2.火魔法,4.水魔法,8.風魔法,16.光魔法
	 */
	public int getAttr() {
		return _attr;
	}

	private int _type; // タイプ

	/**
	 * スキルの作用の種類を返す。<br>
	 * 1.確率系,2.エンチャント,4.呪い,8.死,16.治療,32.復活,64.攻撃,128.その他特殊
	 */
	public int getType() {
		return _type;
	}

	private int _lawful;

	public int getLawful() {
		return _lawful;
	}

	private int _ranged;

	public int getRanged() {
		return _ranged;
	}

	private int _area;

	public int getArea() {
		return _area;
	}

	boolean _isThrough;

	public boolean isThrough() {
		return _isThrough;
	}

	private int _id;

	public int getId() {
		return _id;
	}

	private String _nameId;

	public String getNameId() {
		return _nameId;
	}

	private int _actionId;

	public int getActionId() {
		return _actionId;
	}

	private int _castGfx;

	public int getCastGfx() {
		return _castGfx;
	}

	private int _castGfxViolet;

	public int getCastGfxViolet() {
		return _castGfxViolet;
	}

	private int _castGfx2;

	public int getCastGfx2() {
		return _castGfx2;
	}

	private int _sysmsgIdHappen;

	public int getSysmsgIdHappen() {
		return _sysmsgIdHappen;
	}

	private int _sysmsgIdStop;

	public int getSysmsgIdStop() {
		return _sysmsgIdStop;
	}

	private int _sysmsgIdFail;

	public int getSysmsgIdFail() {
		return _sysmsgIdFail;
	}

	private boolean _isUserTarget;

	/**
	 * この魔法が確率系範囲魔法の場合、自分自身をターゲットにするか？<br>
	 * 例:エリアサイレンスならtrue
	 * @return
	 * 		ターゲットにするならtrue
	 */
	public boolean isUserTarget() {
		return _isUserTarget;
	}

	private boolean _canCastWithInvis;

	/**
	 * インビジ中に使用可能か？
	 * @return
	 * 		使用可能ならtrue
	 */
	public boolean canCastWithInvis() {
		return _canCastWithInvis;
	}

	private boolean _canUseSilence;

	/**
	 * サイレンス中に使用可能か？
	 * @return
	 * 		使用可能ならtrue
	 */
	public boolean canUseSilence() {
		return _canUseSilence;
	}

	private boolean _ignoresCounterMagic;

	/**
	 * カウンターマジックを貫通するかどうか。
	 * @return
	 * 		貫通するならtrue
	 */
	public boolean ignoresCounterMagic() {
		return _ignoresCounterMagic;
	}

	private boolean _ignoresAbsoluteBarrier;

	/**
	 * アブソリュートバリア中の相手に魔法がかかるかどうか。
	 * @return
	 * 		貫通するならtrue
	 */
	public boolean ignoresAbsoluteBarrier() {
		return _ignoresAbsoluteBarrier;
	}

	private boolean _isBuff;

	public boolean isBuff() {
		return _isBuff;
	}

	private String _impl;

	public String getImpl() {
		return _impl;
	}

	public static L1Skill fromResultSet(ResultSet rs) throws SQLException {
		L1Skill skill = new L1Skill();
		int skillId = rs.getInt("id");
		skill._skillId = skillId;
		skill._name = rs.getString("name");
		skill._baseSkillId = rs.getInt("base_skill_id");
		skill._skillLevel = rs.getInt("skill_level");
		skill._skillNumber = rs.getInt("skill_number");
		skill._consumeMp = rs.getInt("consume_mp");
		skill._consumeHp = rs.getInt("consume_hp");
		skill._consumeItmeId = rs.getInt("consume_item_id");
		skill._consumeAmount = rs.getInt("consume_amount");
		skill._reuseDelay = rs.getInt("reuse_delay");
		skill._removeDelay = rs.getInt("remove_delay");
		String buffDuration = rs.getString("buff_duration");
		if (buffDuration != null) {
			StringTokenizer st = new StringTokenizer(buffDuration, ",");
			while (st.hasMoreTokens()) {
				try {
					skill._buffDuration.add(Double.valueOf(st.nextToken()));
				} catch (Exception e) {
					_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
					_log.fine("skill data for id:" + skillId + " missing in skills table");
				}
			}
		}
		skill._target = rs.getString("target");
		skill._targetTo = rs.getInt("target_to");
		skill._damageValue = rs.getDouble("damage_value");
		skill._damageDice = rs.getInt("damage_dice");
		skill._damageDiceCount = rs.getInt("damage_dice_count");
		skill._probabilityValue = rs.getInt("probability_value");
		skill._probabilityDice = rs.getInt("probability_dice");
		skill._MrValue = rs.getInt("mr_value");
		skill._MrDice = rs.getDouble("mr_dice");
		skill._probabilityMax = rs.getInt("probability_max");
		skill._attr = rs.getInt("attr");
		skill._type = rs.getInt("type");
		skill._lawful = rs.getInt("lawful");
		skill._ranged = rs.getInt("ranged");
		skill._area = rs.getInt("area");
		skill._isThrough = rs.getBoolean("through");
		skill._id = rs.getInt("skill_id");
		skill._nameId = rs.getString("name_id");
		skill._actionId = rs.getInt("action_id");
		skill._castGfx = rs.getInt("cast_gfx");
		skill._castGfxViolet = rs.getInt("cast_gfx_violet");
		skill._castGfx2 = rs.getInt("cast_gfx2");
		skill._sysmsgIdHappen = rs.getInt("sys_msg_id_happen");
		skill._sysmsgIdStop = rs.getInt("sys_msg_id_stop");
		skill._sysmsgIdFail = rs.getInt("sys_msg_id_fail");
		skill._canCastWithInvis = rs.getBoolean("can_cast_with_invis");
		skill._canUseSilence = rs.getBoolean("can_use_silence");
		skill._ignoresCounterMagic = rs.getBoolean("ignores_counter_magic");
		skill._ignoresAbsoluteBarrier = rs.getBoolean("ignores_absolute_barrier");
		skill._isUserTarget = rs.getBoolean("is_user_target");
		skill._isBuff = rs.getBoolean("is_buff");
		skill._impl = rs.getString("impl");

		return skill;
	}

	public L1SkillExecutor newExecutor() {
		if (_impl == null) {
			return null;
		}
		String fullName = StringUtil.complementClassName(_impl, DEFAULT_PACKAGE);
		L1SkillExecutor exe = ReflectionUtil.newInstance(fullName);
		exe.initialize(this);
		return exe;
	}

	public L1BuffSkillExecutor newBuffSkillExecutor() {
		L1SkillExecutor exe = newExecutor();
		if (!(exe instanceof L1BuffSkillExecutor)) {
			return null;
		}
		return (L1BuffSkillExecutor) exe;
	}
}
