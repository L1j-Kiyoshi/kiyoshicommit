/**
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

package jp.l1j.server.model;

import static jp.l1j.locale.I18N.*;
import static jp.l1j.server.model.skill.L1SkillId.*;

import java.text.MessageFormat;
import java.util.logging.Logger;

import jp.l1j.configure.Config;
import jp.l1j.server.codes.ActionCodes;
import jp.l1j.server.controller.timer.WarTimeController;
import jp.l1j.server.datatables.SkillTable;
import jp.l1j.server.model.gametime.L1GameTimeClock;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.instance.L1PetInstance;
import jp.l1j.server.model.instance.L1SummonInstance;
import jp.l1j.server.model.poison.L1DamagePoison;
import jp.l1j.server.model.poison.L1ParalysisPoison;
import jp.l1j.server.model.poison.L1SilencePoison;
import jp.l1j.server.model.skill.L1SkillUse;
import jp.l1j.server.model.skill.executor.L1CounterBarrier;
import jp.l1j.server.packets.server.S_AttackMissPacket;
import jp.l1j.server.packets.server.S_AttackPacket;
import jp.l1j.server.packets.server.S_AttackPacketForNpc;
import jp.l1j.server.packets.server.S_DoActionGFX;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.packets.server.S_SkillIconGFX;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.packets.server.S_SystemMessage;
import jp.l1j.server.packets.server.S_UseArrowSkill;
import jp.l1j.server.packets.server.S_UseAttackSkill;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1MagicDoll;
import jp.l1j.server.templates.L1Skill;
import jp.l1j.server.types.Point;

public class L1Attack {
	private static Logger _log = Logger.getLogger(L1Attack.class.getName());

	private L1PcInstance _pc = null;

	private L1Character _target = null;

	private L1PcInstance _targetPc = null;

	private L1NpcInstance _npc = null;

	private L1NpcInstance _targetNpc = null;

	private L1Magic _magic = null;

	private final int _targetId;

	private int _targetX;

	private int _targetY;

	private int _statusDamage = 0;

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private int _hitRate = 0;

	private int _calcType;

	private static final int PC_PC = 1;

	private static final int PC_NPC = 2;

	private static final int NPC_PC = 3;

	private static final int NPC_NPC = 4;

	private byte _effectId = 0;

	private boolean _isHit = false;

	private int _damage = 0;

	private int _drainHp = 0;

	private int _drainMana = 0;

	private int _attckGrfxId = 0;

	private int _attckActId = 0;

	// 攻撃者がプレイヤーの場合の武器情報 テスト
	private L1ItemInstance weapon = null;

	private int _weaponId = 0;

	private int _weaponType = 0;

	private int _weaponType2 = 0;

	private int _sayhaDmg = -1;

	private int _sayhaGfx = -1;

	private int _weaponAddHit = 0;

	private int _weaponAddDmg = 0;

	private int _weaponAddPvPHit = 0;

	private int _weaponAddPvPDmg = 0;

	private int _weaponSmall = 0;

	private int _weaponLarge = 0;

	private int _weaponRange = 1;

	private int _weaponBless = 0;

	private int _weaponEnchant = 0;

	private int _weaponMaterial = 0;

	private int _weaponSpecial = 0;

	private boolean _isHpDrain = false;

	private int _hpDrainChance = 0;

	private boolean _isMpDrain = false;

	private int _weaponAttrEnchantKind = 0;

	private int _weaponAttrEnchantLevel = 0;

	private L1ItemInstance _arrow = null;

	private L1ItemInstance _sting = null;

	private int _leverage = 10; // 1/10倍で表現する。

	private int _skillId;

	private double _skillDamage = 0;

	private int _addDamage = 0; // 特殊な追加ダメージ。NPCスキル用

	public void setAddDamage(int dmg) {
		_addDamage = dmg;
	}

	public int getAddDamage() {
		return _addDamage;
	}

	public void setLeverage(int i) {
		_leverage = i;
	}

	private int getLeverage() {
		return _leverage;
	}

	// 攻撃者がプレイヤーの場合のステータスによる補正
	// private static final int[] strHit = { -2, -2, -2, -2, -2, -2, -2, -2, -2,
	// -2, -1, -1, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9,
	// 9, 10, 10, 11, 11, 12, 12, 13, 13, 14 };

	// private static final int[] dexHit = { -2, -2, -2, -2, -2, -2, -2, -2, -2,
	// -1, -1, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8,
	// 9, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14 };

	/*
	 * private static final int[] strHit = { -2, -2, -2, -2, -2, -2, -2, //
	 * 0〜7まで -1, -1, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, //
	 * 8〜26まで 7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, //
	 * 27〜44まで 13, 13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16, 17, 17, 17}; //
	 * 45〜59まで
	 *
	 * private static final int[] dexHit = { -2, -2, -2, -2, -2, -2, -1, -1, 0,
	 * 0, // 1〜10まで 1, 1, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
	 * 15, 16, // 11〜30まで 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
	 * 30, 31, // 31〜45まで 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44,
	 * 45, 46 }; // 46〜60まで
	 *
	 * private static final int[] strDmg = new int[128];
	 *
	 * static { // ＳＴＲダメージ補正 int dmg = -6; for (int str = 0; str <= 22; str++) {
	 * // ０〜２２は２毎に＋１ if (str % 2 == 1) { dmg++; } strDmg[str] = dmg; } for (int
	 * str = 23; str <= 28; str++) { // ２３〜２８は３毎に＋１ if (str % 3 == 2) { dmg++; }
	 * strDmg[str] = dmg; } for (int str = 29; str <= 32; str++) { //
	 * ２９〜３２は２毎に＋１ if (str % 2 == 1) { dmg++; } strDmg[str] = dmg; } for (int
	 * str = 33; str <= 39; str++) { // ３３〜３９は１毎に＋１ dmg++; strDmg[str] = dmg; }
	 * for (int str = 40; str <= 46; str++) { // ４０〜４６は１毎に＋２ dmg += 2;
	 * strDmg[str] = dmg; } for (int str = 47; str <= 127; str++) { //
	 * ４７〜１２７は１毎に＋１ dmg++; strDmg[str] = dmg; } }
	 *
	 * private static final int[] dexDmg = new int[128];
	 *
	 * static { // ＤＥＸダメージ補正 for (int dex = 0; dex <= 14; dex++) { // ０〜１４は０
	 * dexDmg[dex] = 0; } dexDmg[15] = 1; dexDmg[16] = 2; dexDmg[17] = 3;
	 * dexDmg[18] = 4; dexDmg[19] = 4; dexDmg[20] = 4; dexDmg[21] = 5;
	 * dexDmg[22] = 5; dexDmg[23] = 5; int dmg = 5; for (int dex = 24; dex <=
	 * 127; dex++) { // ２４〜１２７は１毎に＋１ dmg++; dexDmg[dex] = dmg; } }
	 */

	private static final int[] strHit = { -2, -2, -2, -2, -2, -2, -2, // 1〜7
			-2, -1, -1, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6, // 8〜26
			7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, // 27〜44
			13, 13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16, 17, 17, 17 }; // 45〜59
	private static final int[] dexHit = { -2, -2, -2, -2, -2, -2, -1, -1, 0, 0, // 1〜10
			1, 1, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, // 11〜30
			17, 18, 19, 19, 19, 20, 20, 20, 21, 21, 21, 22, 22, 22, 23, // 31〜45
			23, 23, 24, 24, 24, 25, 25, 25, 26, 26, 26, 27, 27, 27, 28 }; // 46〜60

	private static final int[] strDmg = new int[128];

	static {
		// STRダメージ補正
		int dmg = -6;
		for (int str = 0; str <= 22; str++) { // 0〜22は2毎に+1
			if (str % 2 == 1) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
		for (int str = 23; str <= 28; str++) { // 23〜28は3毎に+1
			if (str % 3 == 2) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
		for (int str = 29; str <= 32; str++) { // 29〜32は2毎に+1
			if (str % 2 == 1) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
		for (int str = 33; str <= 34; str++) { // 33〜34は1毎に+1
			dmg++;
			strDmg[str] = dmg;
		}
		for (int str = 35; str <= 46; str++) { // 35〜46は4毎に+1
			if (str % 4 == 3) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
		for (int str = 47; str <= 48; str++) { // 47〜48は2毎に+1
			if (str % 2 == 1) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
			dmg++;
			strDmg[49] = dmg; // 49は1毎に1
		for (int str = 50; str <= 127; str++) { // 50〜127は2毎に+1
			if (str % 2 == 0) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
	}

	private static final int[] dexDmg = new int[128];

	static {
		// DEXダメージ補正
		for (int dex = 0; dex <= 14; dex++) {
			// 0〜14は0
			dexDmg[dex] = 0;
		}
		dexDmg[15] = 1;
		dexDmg[16] = 2;
		dexDmg[17] = 3;
		dexDmg[18] = 4;
		dexDmg[19] = 4;
		dexDmg[20] = 4;
		dexDmg[21] = 5;
		dexDmg[22] = 5;
		dexDmg[23] = 5;
		int dmg = 5;
		for (int dex = 24; dex <= 35; dex++) { // 24〜35は3毎に+1
			if (dex % 3 == 0) {
				dmg++;
			}
			dexDmg[dex] = dmg;
		}
		for (int dex = 36; dex <= 127; dex++) { // 36〜127は4毎に1
			if (dex % 4 == 0) {
				dmg++;
			}
			dexDmg[dex] = dmg;
		}
	}

	public void setActId(int actId) {
		_attckActId = actId;
	}

	public void setGfxId(int gfxId) {
		_attckGrfxId = gfxId;
	}

	public int getActId() {
		return _attckActId;
	}

	public int getGfxId() {
		return _attckGrfxId;
	}

	public L1Attack(L1Character attacker, L1Character target) {
		this(attacker, target, 0);
	}

	public L1Attack(L1Character attacker, L1Character target, int skillId) {
		_skillId = skillId;
		if (_skillId != 0) {
			L1Skill skills = SkillTable.getInstance().findBySkillId(_skillId);
			_skillDamage = skills.getDamageValue();
		} else {
			_skillDamage = 0;
		}

		_magic = new L1Magic(attacker, target);

		if (attacker instanceof L1PcInstance) {
			_pc = (L1PcInstance) attacker;
			if (target instanceof L1PcInstance) {
				_targetPc = (L1PcInstance) target;
				_calcType = PC_PC;
			} else if (target instanceof L1NpcInstance) {
				_targetNpc = (L1NpcInstance) target;
				_calcType = PC_NPC;
			}
			// 武器情報の取得
			weapon = _pc.getWeapon();
			if (weapon != null) {
				_weaponId = weapon.getItem().getItemId();
				_weaponType = weapon.getItem().getType1();
				_weaponType2 = weapon.getItem().getType();
				_sayhaDmg = weapon.getItem().getSayhaDmg();
				_sayhaGfx = weapon.getItem().getSayhaGfx();
				_weaponAddHit = weapon.getItem().getHitModifier() + weapon.getHitByMagic();
				_weaponAddPvPHit = weapon.getItem().getPvPHitModifier();
				_weaponAddDmg = weapon.getItem().getDmgModifier() + weapon.getDmgByMagic();
				_weaponAddPvPDmg = weapon.getItem().getPvPDmgModifier();
				_weaponSmall = weapon.getItem().getDmgSmall();
				_weaponLarge = weapon.getItem().getDmgLarge();
				_weaponRange = weapon.getItem().getRange();
				_weaponBless = weapon.getItem().getBless();
				if (_weaponType != 20 && _weaponType != 62) { // 近接武器
					_weaponEnchant = weapon.getEnchantLevel()
							- weapon.getDurability(); // 損傷分マイナス
				} else {
					_weaponEnchant = weapon.getEnchantLevel();
				}
				_weaponMaterial = weapon.getItem().getMaterial();
				if (_weaponType == 20) { // アローの取得
					_arrow = _pc.getInventory().getArrow();
					if (_arrow != null) {
						_weaponBless = _arrow.getItem().getBless();
						_weaponMaterial = _arrow.getItem().getMaterial();
					}
				}
				if (_weaponType == 62) { // スティングの取得
					_sting = _pc.getInventory().getSting();
					if (_sting != null) {
						_weaponBless = _sting.getItem().getBless();
						_weaponMaterial = _sting.getItem().getMaterial();
					}
				}
				_weaponSpecial = weapon.getItem().getWeaponSpecial();
				_isHpDrain = weapon.getItem().isHpDrain();
				_hpDrainChance = weapon.getItem().getHpDrainChance();
				_isMpDrain = weapon.getItem().isMpDrain();
				_weaponAttrEnchantKind = weapon.getAttrEnchantKind();
				_weaponAttrEnchantLevel = weapon.getAttrEnchantLevel();
			}
			// ステータスによる追加ダメージ補正
			if (_weaponType == 20 || _weaponType == 62) { // 弓の場合はＤＥＸ値参照
				_statusDamage = dexDmg[_pc.getDex()];
			} else { // それ以外はＳＴＲ値参照
				_statusDamage = strDmg[_pc.getStr()];
			}
		} else if (attacker instanceof L1NpcInstance) {
			_npc = (L1NpcInstance) attacker;
			if (target instanceof L1PcInstance) {
				_targetPc = (L1PcInstance) target;
				_calcType = NPC_PC;
			} else if (target instanceof L1NpcInstance) {
				_targetNpc = (L1NpcInstance) target;
				_calcType = NPC_NPC;
			}
		}
		_target = target;
		_targetId = target.getId();
		_targetX = target.getX();
		_targetY = target.getY();
	}

	/* ■■■■■■■■■■■■■■■■ 命中判定 ■■■■■■■■■■■■■■■■ */

	public boolean calcHit() {
		if (_calcType == PC_PC || _calcType == PC_NPC) {
			_pc.setHeading(_pc.targetDirection(_targetX, _targetY)); // 向きのセット
			if (_weaponRange != -1) {
				if (_pc.getLocation()
						.getTileLineDistance(_target.getLocation()) > _weaponRange + 1) { // BIGのモンスターに対応するため射程範囲
					// +
					// 1
					_isHit = false; // 射程範囲外
					return _isHit;
				}
			} else {
				if (!_pc.getLocation().isInScreen(_target.getLocation())) {
					_isHit = false; // 射程範囲外
					return _isHit;
				}
			}
			if (_weaponType == 20 && _sayhaGfx == -1 && _arrow == null) {
				_isHit = false; // 矢がない場合はミス
			} else if (_weaponType == 62 && _sting == null) {
				_isHit = false; // スティングがない場合はミス
			} else if (!(_pc.glanceCheck(_pc.getX(), _pc.getY(), _targetX, _targetY)
							|| _pc.glanceCheck(_targetX, _targetY, _pc.getX(), _pc.getY()))) {
				_isHit = false; // 攻撃者がプレイヤーの場合は障害物判定
			} else if (_weaponId == 247 || _weaponId == 248 || _weaponId == 249) {
				_isHit = false; // 試練の剣B〜C 攻撃無効
			} else if (_calcType == PC_PC) {
				_isHit = calcPcPcHit();
			} else if (_calcType == PC_NPC) {
				_isHit = calcPcNpcHit();
			}
			if (_calcType == PC_NPC && _weaponId != 246 &&
					_targetNpc.getNpcTemplate().getNpcId() == 45878) {
				_isHit = false; // 試練の剣A以外でドレイクの幽霊への攻撃を無効
			}
		} else if (_calcType == NPC_PC) {
			_npc.setHeading(_npc.targetDirection(_targetX, _targetY)); // 向きのセット
			_isHit = calcNpcPcHit();
		} else if (_calcType == NPC_NPC) {
			_npc.setHeading(_npc.targetDirection(_targetX, _targetY)); // 向きのセット
			_isHit = calcNpcNpcHit();
		}
		return _isHit;
	}

	// ●●●● プレイヤー から プレイヤー への命中判定 ●●●●
	/*
	 * ＰＣへの命中率 ＝（PCのLv＋クラス補正＋STR補正＋DEX補正＋武器補正＋DAIの枚数/2＋魔法補正）×0.68−10
	 * これで算出された数値は自分が最大命中(95%)を与える事のできる相手側PCのAC そこから相手側PCのACが1良くなる毎に自命中率から1引いていく
	 * 最小命中率5% 最大命中率95%
	 */
	private boolean calcPcPcHit() {

		// マジックドール效果 - ダメージ回避
		if (L1MagicDoll.getDamageEvasionByDoll(_targetPc) > 0) {
			_hitRate = 0;
			return false;
		}
		// 魔眼によるダメージ回避
		if (_targetPc.hasSkillEffect(MAGIC_EYE_OF_ANTHARAS)
				|| _targetPc.hasSkillEffect(MAGIC_EYE_OF_BIRTH)
				|| _targetPc.hasSkillEffect(MAGIC_EYE_OF_SHAPE)
				|| _targetPc.hasSkillEffect(MAGIC_EYE_OF_LIFE)) {
			int _avoidChance = _random.nextInt(100) + 1;
			if (_avoidChance <= 10) {
				_hitRate = 0;
				return false;
			}
		}

		if (_weaponType2 == 14) {
			_hitRate = 100; // キーリンクの命中率は100%
			return true;
		}
		_hitRate = _pc.getLevel();

		if (_pc.getStr() > 59) {
			_hitRate += strHit[58];
		} else {
			_hitRate += strHit[_pc.getStr() - 1];
		}

		if (_pc.getDex() > 60) {
			_hitRate += dexHit[59];
		} else {
			_hitRate += dexHit[_pc.getDex() - 1];
		}

		if (_weaponType != 20 && _weaponType != 62) {
			_hitRate += _weaponAddHit + _weaponAddPvPHit + _pc.getHitup() + _pc.getOriginalHitup()
					+ (_weaponEnchant / 2);
		} else {
			_hitRate += _weaponAddHit + _weaponAddPvPHit + _pc.getBowHitup()
					+ _pc.getOriginalBowHitup() + (_weaponEnchant / 2);
		}

		if (_weaponType != 20 && _weaponType != 62) { // 防具による追加命中
			_hitRate += _pc.getHitModifierByArmor();
		} else {
			_hitRate += _pc.getBowHitModifierByArmor();
		}

		if (80 < _pc.getInventory().getWeight240() // 重量による命中補正
				&& 120 >= _pc.getInventory().getWeight240()) {
			_hitRate -= 1;
		} else if (121 <= _pc.getInventory().getWeight240()
				&& 160 >= _pc.getInventory().getWeight240()) {
			_hitRate -= 3;
		} else if (161 <= _pc.getInventory().getWeight240()
				&& 200 >= _pc.getInventory().getWeight240()) {
			_hitRate -= 5;
		}

		if (_pc.hasSkillEffect(COOKING_2_0_N) // 料理による追加命中
				|| _pc.hasSkillEffect(COOKING_2_0_S)) {
			if (_weaponType != 20 && _weaponType != 62) {
				_hitRate += 1;
			}
		}
		if (_pc.hasSkillEffect(COOKING_3_2_N) // 料理による追加命中
				|| _pc.hasSkillEffect(COOKING_3_2_S)) {
			if (_weaponType != 20 && _weaponType != 62) {
				_hitRate += 2;
			}
		}
		if (_pc.hasSkillEffect(COOKING_2_3_N) // 料理による追加命中
				|| _pc.hasSkillEffect(COOKING_2_3_S)
				|| _pc.hasSkillEffect(COOKING_3_0_N)
				|| _pc.hasSkillEffect(COOKING_3_0_S)) {
			if (_weaponType == 20 || _weaponType == 62) {
				_hitRate += 1;
			}
		}

		int attackerDice = _random.nextInt(20) + 1 + _hitRate - 10;

		// 回避率
		attackerDice -= _targetPc.getDodge();
		attackerDice += _targetPc.getNdodge();

		int defenderDice = 0;

		int defenderValue = (int) (_targetPc.getAc() * 1.5) * -1;

		if (_targetPc.getAc() >= 0) {
			defenderDice = 10 - _targetPc.getAc();
		} else if (_targetPc.getAc() < 0) {
			defenderDice = 10 + _random.nextInt(defenderValue) + 1;
		}

		int fumble = _hitRate - 9;
		int critical = _hitRate + 10;

		if (attackerDice <= fumble) {
			_hitRate = 0;
		} else if (attackerDice >= critical) {
			_hitRate = 100;
		} else {
			if (attackerDice > defenderDice) {
				_hitRate = 100;
			} else if (attackerDice <= defenderDice) {
				_hitRate = 0;
			}
		}

		int rnd = _random.nextInt(100) + 1;
		if (_weaponType == 20 && _hitRate > rnd) { // 弓の場合、ヒットした場合でもERでの回避を再度行う。
			return calcErEvasion();
		}

		return _hitRate >= rnd;
	}

	// ●●●● プレイヤー から ＮＰＣ への命中判定 ●●●●
	private boolean calcPcNpcHit() {
		// ＮＰＣへの命中率
		// ＝（PCのLv＋クラス補正＋STR補正＋DEX補正＋武器補正＋DAIの枚数/2＋魔法補正）×5−{NPCのAC×（-5）}

		if (_weaponType2 == 14) {
			_hitRate = 100; // キーリンクの命中率は100%
			// 特定条件有攻可能 NPC判定
			if (_pc.isAttackMiss(_pc, _targetNpc.getNpcTemplate().getNpcId())) {
				_hitRate = 0;
				return false;
			}
			return true;
		}

		_hitRate = _pc.getLevel();

		if (_pc.getStr() > 59) {
			_hitRate += strHit[58];
		} else {
			_hitRate += strHit[_pc.getStr() - 1];
		}

		if (_pc.getDex() > 60) {
			_hitRate += dexHit[59];
		} else {
			_hitRate += dexHit[_pc.getDex() - 1];
		}

		if (_weaponType != 20 && _weaponType != 62) {
			_hitRate += _weaponAddHit + _pc.getHitup() + _pc.getOriginalHitup()
					+ (_weaponEnchant / 2);
		} else {
			_hitRate += _weaponAddHit + _pc.getBowHitup()
					+ _pc.getOriginalBowHitup() + (_weaponEnchant / 2);
		}

		if (_weaponType != 20 && _weaponType != 62) { // 防具による追加命中
			_hitRate += _pc.getHitModifierByArmor();
		} else {
			_hitRate += _pc.getBowHitModifierByArmor();
		}

		if (80 < _pc.getInventory().getWeight240() // 重量による命中補正
				&& 120 >= _pc.getInventory().getWeight240()) {
			_hitRate -= 1;
		} else if (121 <= _pc.getInventory().getWeight240()
				&& 160 >= _pc.getInventory().getWeight240()) {
			_hitRate -= 3;
		} else if (161 <= _pc.getInventory().getWeight240()
				&& 200 >= _pc.getInventory().getWeight240()) {
			_hitRate -= 5;
		}

		if (_pc.hasSkillEffect(COOKING_2_0_N) // 料理による追加命中
				|| _pc.hasSkillEffect(COOKING_2_0_S)) {
			if (_weaponType != 20 && _weaponType != 62) {
				_hitRate += 1;
			}
		}
		if (_pc.hasSkillEffect(COOKING_3_2_N) // 料理による追加命中
				|| _pc.hasSkillEffect(COOKING_3_2_S)) {
			if (_weaponType != 20 && _weaponType != 62) {
				_hitRate += 2;
			}
		}
		if (_pc.hasSkillEffect(COOKING_2_3_N) // 料理による追加命中
				|| _pc.hasSkillEffect(COOKING_2_3_S)
				|| _pc.hasSkillEffect(COOKING_3_0_N)
				|| _pc.hasSkillEffect(COOKING_3_0_S)) {
			if (_weaponType == 20 || _weaponType == 62) {
				_hitRate += 1;
			}
		}

		int attackerDice = _random.nextInt(20) + 1 + _hitRate - 10;

		// 回避率
		attackerDice -= _targetNpc.getDodge();
		attackerDice += _targetNpc.getNdodge();

		int defenderDice = 10 - _targetNpc.getAc();

		int fumble = _hitRate - 9;
		int critical = _hitRate + 10;

		if (attackerDice <= fumble) {
			_hitRate = 0;
		} else if (attackerDice >= critical) {
			_hitRate = 100;
		} else {
			if (attackerDice > defenderDice) {
				_hitRate = 100;
			} else if (attackerDice <= defenderDice) {
				_hitRate = 0;
			}
		}

		// 特定条件有攻可能 NPC判定
		if (_pc.isAttackMiss(_pc, _targetNpc.getNpcTemplate().getNpcId())) {
			_hitRate = 0;
		}

		int rnd = _random.nextInt(100) + 1;

		return _hitRate >= rnd;
	}

	// ●●●● ＮＰＣ から プレイヤー への命中判定 ●●●●
	private boolean calcNpcPcHit() {

		if ((_npc instanceof L1PetInstance)
				|| (_npc instanceof L1SummonInstance)) {
			// 目標攻判定、NOPVP
			if ((_targetPc.getZoneType() == 1) || (_npc.getZoneType() == 1)
					|| (_targetPc.checkNonPvP(_targetPc, _npc))) {
				_hitRate = 0;
				return false;
			}
		}

		// マジックドール効果 - ダメージ回避
		if (L1MagicDoll.getDamageEvasionByDoll(_targetPc) > 0) {
			_hitRate = 0;
			return false;
		}
		// 魔眼によるダメージ回避
		if (_targetPc.hasSkillEffect(MAGIC_EYE_OF_ANTHARAS)
				|| _targetPc.hasSkillEffect(MAGIC_EYE_OF_BIRTH)
				|| _targetPc.hasSkillEffect(MAGIC_EYE_OF_SHAPE)
				|| _targetPc.hasSkillEffect(MAGIC_EYE_OF_LIFE)) {
			int _avoidChance = _random.nextInt(100) + 1;
			if (_avoidChance <= 10) {
				_hitRate = 0;
				return false;
			}
		}

		_hitRate += _npc.getLevel();

		if (_npc instanceof L1PetInstance) { // ペットの武器による追加命中
			_hitRate += ((L1PetInstance) _npc).getHitByWeapon();
		}

		_hitRate += _npc.getHitup();

		int attackerDice = _random.nextInt(20) + 1 + _hitRate - 1;

		// 回避率
		attackerDice -= _targetPc.getDodge();
		attackerDice += _targetPc.getNdodge();

		int defenderDice = 0;

		int defenderValue = (_targetPc.getAc()) * -1;

		if (_targetPc.getAc() >= 0) {
			defenderDice = 10 - _targetPc.getAc();
		} else if (_targetPc.getAc() < 0) {
			defenderDice = 10 + _random.nextInt(defenderValue) + 1;
		}

		int fumble = _hitRate;
		int critical = _hitRate + 19;

		if (attackerDice <= fumble) {
			_hitRate = 0;
		} else if (attackerDice >= critical) {
			_hitRate = 100;
		} else {
			if (attackerDice > defenderDice) {
				_hitRate = 100;
			} else if (attackerDice <= defenderDice) {
				_hitRate = 0;
			}
		}

		int rnd = _random.nextInt(100) + 1;

		// NPCの攻撃レンジが10以上の場合で、2以上離れている場合弓攻撃とみなす
		if (_npc.getNpcTemplate().getRanged() >= 10
				&& _hitRate > rnd
				&& _npc.getLocation().getTileLineDistance(
						new Point(_targetX, _targetY)) >= 2) {
			return calcErEvasion();
		}
		return _hitRate >= rnd;
	}

	// ●●●● ＮＰＣ から ＮＰＣ への命中判定 ●●●●
	private boolean calcNpcNpcHit() {

		_hitRate += _npc.getLevel();

		if (_npc instanceof L1PetInstance) { // ペットの武器による追加命中
			_hitRate += ((L1PetInstance) _npc).getHitByWeapon();
		}

		_hitRate += _npc.getHitup();

		int attackerDice = _random.nextInt(20) + 1 + _hitRate - 1;

		// 回避率
		attackerDice -= _targetNpc.getDodge();
		attackerDice += _targetNpc.getNdodge();

		int defenderDice = 0;

		int defenderValue = (_targetNpc.getAc()) * -1;

		if (_targetNpc.getAc() >= 0) {
			defenderDice = 10 - _targetNpc.getAc();
		} else if (_targetNpc.getAc() < 0) {
			defenderDice = 10 + _random.nextInt(defenderValue) + 1;
		}

		int fumble = _hitRate;
		int critical = _hitRate + 19;

		if (attackerDice <= fumble) {
			_hitRate = 0;
		} else if (attackerDice >= critical) {
			_hitRate = 100;
		} else {
			if (attackerDice > defenderDice) {
				_hitRate = 100;
			} else if (attackerDice <= defenderDice) {
				_hitRate = 0;
			}
		}

		int rnd = _random.nextInt(100) + 1;
		return _hitRate >= rnd;
	}

	// ●●●● ＥＲによる回避判定 ●●●●
	private boolean calcErEvasion() {
		int er = _targetPc.getEr();

		int rnd = _random.nextInt(100) + 1;
		return er < rnd;
	}

	/* ■■■■■■■■■■■■■■■ ダメージ算出 ■■■■■■■■■■■■■■■ */

	public int calcDamage() {
		if (_calcType == PC_PC) {
			_damage = calcPcPcDamage();
		} else if (_calcType == PC_NPC) {
			_damage = calcPcNpcDamage();
		} else if (_calcType == NPC_PC) {
			_damage = calcNpcPcDamage();
		} else if (_calcType == NPC_NPC) {
			_damage = calcNpcNpcDamage();
		}
		return _damage;
	}

	// ●●●● プレイヤー から プレイヤー へのダメージ算出 ●●●●
	public int calcPcPcDamage() {

		double dmg;
		if (_weaponType2 == 14) { // キーリンク
			if (_targetPc.isThroughAttack()) {
				return 0;
			}
			dmg = _magic.calcMagicDamage(_weaponSpecial);
			sendKiringkuEffect(_weaponSpecial);
			dmg += _weaponEnchant;
			dmg += calcAttrEnchantDmg();
			 // ＤＢでキーリングにスキルが設定されている場合
			dmg += L1WeaponSkill.getWeaponSkillDamage(_pc, _target);
			return (int) dmg;
		}

		// パプリオンの加護はダメージ発生前に発動する
		if (_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_FAFURION)) {
			_targetPc.commitFafurionHydroArmor();
		}
		// ハロウィンの加護はダメージ発生前に発動する
		if (_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_HALLOWEEN)) {
			_targetPc.commitDragonicHalloweenArmor();
		}


		if (_weaponType == 0) { // 素手
			return (_random.nextInt(5) + 4) / 4;
		}

		int weaponMaxDamage = _weaponSmall;

		int weaponDamage = 0;
		if (_weaponType == 58
				&& (_random.nextInt(1000) + 1) <= _weaponSpecial) { // クリティカルヒット
			weaponDamage = weaponMaxDamage;
			_pc.sendPackets(new S_SkillSound(_pc.getId(), 3671));
			_pc.broadcastPacket(new S_SkillSound(_pc.getId(), 3671));
		} else if (_weaponType == 0 || _weaponType == 20 || _weaponType == 62) { // 素手、弓、ガントトレット
			weaponDamage = 0;
		} else {
			weaponDamage = _random.nextInt(weaponMaxDamage) + 1;
		}
		if (_pc.hasSkillEffect(SOUL_OF_FLAME)) {
			if (_weaponType != 20 && _weaponType != 62) {
				weaponDamage = weaponMaxDamage;
			}
		}

		int weaponTotalDamage = weaponDamage + _weaponAddDmg + _weaponAddPvPDmg + _weaponEnchant;

		if (_weaponType == 54) {
			if ((_random.nextInt(1000) + 1) <= _weaponSpecial) { // ダブルヒット
				// ダブルヒットに乗るのは
				// 基礎値・強化値・武器追加打撃値・武器エンチャ（シャドウファンク等）・祝福効果
				weaponTotalDamage *= 2;
				_pc.sendPackets(new S_SkillSound(_pc.getId(), 3398));
				_pc.broadcastPacket(new S_SkillSound(_pc.getId(), 3398));
			}
		}

		int weaponAttrDmg = 0;
		weaponAttrDmg = calcAttrEnchantDmg(); // 属性強化ダメージボーナス
		weaponTotalDamage += weaponAttrDmg;

		if (_pc.hasSkillEffect(DOUBLE_BRAKE)) { // ダブルブレイク
			// ダブルブレイクで２倍になるのは
			// 基礎値・武器エンチャ（シャドウファング等）・武器属性・銀効果
			if (_weaponType == 54 || _weaponType == 58) {
				if (_random.nextInt(3) == 1) {
					weaponTotalDamage += weaponDamage + _weaponAddDmg + _weaponAddPvPDmg + weaponAttrDmg;
				}
			}
		}

		weaponTotalDamage += calcHpDrain(weaponTotalDamage); // HP吸収武器

		if (_weaponType != 20 && _weaponType != 62) {
			dmg = weaponTotalDamage + _statusDamage + _pc.getDmgup()
					+ _pc.getOriginalDmgup();
		} else {
			dmg = weaponTotalDamage + _statusDamage + _pc.getBowDmgup()
					+ _pc.getOriginalBowDmgup();
		}

		if (_weaponType == 20) { // 弓
			if (_arrow != null) {
				int add_dmg = _arrow.getItem().getDmgSmall();
				if (add_dmg == 0) {
					add_dmg = 1;
				}
				dmg += _random.nextInt(add_dmg) + 1;
			} else if (_sayhaGfx != -1) { // サイハの弓
				dmg += _random.nextInt(_sayhaDmg) + 1;
			}
		} else if (_weaponType == 62) { // ガントトレット
			int add_dmg = _sting.getItem().getDmgSmall();
			if (add_dmg == 0) {
				add_dmg = 1;
			}
			dmg = dmg + _random.nextInt(add_dmg) + 1;
		}

		if (_pc.hasSkillEffect(BURNING_SLASH)) {
			_pc.sendPackets(new S_SkillSound(_targetPc.getId(), 6591));
			_pc.broadcastPacket(new S_SkillSound(_targetPc.getId(), 6591));
			_pc.killSkillEffectTimer(BURNING_SLASH);
		}

		if (_weaponType2 != 14
				&& (_skillId == BONE_BREAK || _skillId == SMASH_ENERGY)) {
			dmg += _skillDamage;
		}

		if (_weaponType != 20 && _weaponType != 62) { // 防具による追加ダメージ
			dmg += _pc.getDmgModifierByArmor();
		} else {
			dmg += _pc.getBowDmgModifierByArmor();
		}

		if (_weaponType != 20 && _weaponType != 62) { // マジックドール効果
			L1MagicDoll.getDamageAddByDoll(_pc);
		}

		if (_pc.hasSkillEffect(COOKING_2_0_N) // 料理による追加ダメージ
				|| _pc.hasSkillEffect(COOKING_2_0_S)
				|| _pc.hasSkillEffect(COOKING_3_2_N)
				|| _pc.hasSkillEffect(COOKING_3_2_S)) {
			if (_weaponType != 20 && _weaponType != 62) {
				dmg += 1;
			}
		}

		if (_pc.hasSkillEffect(COOKING_2_3_N) // 料理による追加ダメージ
				|| _pc.hasSkillEffect(COOKING_2_3_S)
				|| _pc.hasSkillEffect(COOKING_3_0_N)
				|| _pc.hasSkillEffect(COOKING_3_0_S)) {
			if (_weaponType == 20 || _weaponType == 62) {
				dmg += 1;
			}
		}

		if (_pc.hasSkillEffect(MAGIC_EYE_OF_VALAKAS) // 魔眼による追加ダメージ
				|| _pc.hasSkillEffect(MAGIC_EYE_OF_LIFE)) {
			int _damageChance = _random.nextInt(100) + 1;
			if (_damageChance <= 10) {
				dmg += 2;
			}
		}

		dmg += getAddDamage(); // 特殊追加ダメージ

		if (_pc.hasSkillEffect(EYES_BREAKER)) { // ＰＣがアイズブレイカ—中。
			dmg -= 5;
		}

		dmg = calcWraknessExposure(dmg); // 弱点露出処理

		// ■■■■■■■■■■ ここから防御側のダメージ軽減系 ■■■■■■■■■■

		if (_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_PLATE) ||
				_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_SCALE) ||
				_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_LEATHER) ||
				_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_ROBE)) {
			_targetPc.commitLindoviorArmor((int) dmg); // 魔力のリンドビオルの加護処理
		}

		dmg -= _targetPc.getDamageReductionByArmor(); // 防具によるダメージ軽減

		// マジックドール效果 - ダメージリダクション
		dmg -= L1MagicDoll.getDamageReductionByDoll(_targetPc);

		if (_targetPc.isCookingReduction()) { // 幻想料理によるダメージ軽減
			dmg -= 5;
		}

		if (_targetPc.hasSkillEffect(COOKING_1_7_S) // デザートによるダメージ軽減
				|| _targetPc.hasSkillEffect(COOKING_2_7_S)
				|| _targetPc.hasSkillEffect(COOKING_3_7_S)) {
			dmg -= 5;
		}

		if (_targetPc.hasSkillEffect(REDUCTION_ARMOR)) {
			int targetPcLvl = _targetPc.getLevel();
			if (targetPcLvl < 50) {
				targetPcLvl = 50;
			}
			dmg -= (targetPcLvl - 50) / 5 + 1;
		}
		if (_targetPc.hasSkillEffect(SPIRIT_OF_BLACK_SNAKE)) {
			dmg -= 3;
		}
		if (_targetPc.hasSkillEffect(DRAGON_SKIN)) {
			dmg -= 5; // キャラクターケアアップデート
		}
		if (_targetPc.hasSkillEffect(PATIENCE)) {
			dmg -= 2;
		}

		if (_targetPc.hasSkillEffect(SHIELD_OF_TEAIROR)) { // 反逆者のシールド
			if (_random.nextInt(100) <= _targetPc.getTeairorChance()) {
				dmg -= 50;
				_targetPc.sendPackets(new S_SkillSound(_targetPc.getId(), 6320));
				_targetPc.broadcastPacket(new S_SkillSound(_targetPc.getId(), 6320));
			}
		}

		// ルームティスレッドイアリング・発動時のエフェクトは不明
		if (_targetPc.hasSkillEffect(EARRING_OF_ROOMTIS_RED)) {
			if (_random.nextInt(100) <= _targetPc.getRedEarringChance()) {
				dmg -= 20;
				//_targetPc.sendPackets(new S_SkillSound(_targetPc.getId(), 6320));
				//_targetPc.broadcastPacket(new S_SkillSound(_targetPc.getId(), 6320));
			}
		}

		// ■■■■■■■■■■ ダメージの軽減はここまで ■■■■■■■■■■

		// バーニングスピリット・エレメンタルファイアー・ブレイブメンタル
		if (_pc.hasSkillEffect(BURNING_SPIRIT) || _pc.hasSkillEffect(ELEMENTAL_FIRE) ||
				_pc.hasSkillEffect(BRAVE_MENTAL)) {
			if (_random.nextInt(3) == 1) { // 3分の1
				dmg *= 1.50D;
			}
		}

		if (_pc.hasSkillEffect(ILLUSION_AVATAR)) { // アバターは最終ダメージに+10
			dmg += 10;
		}
		// アーマーブレイクにアバターが乗るかは不明
		if (_targetPc.hasSkillEffect(ARMOR_BREAK)) {
			if (_weaponType != 20 && _weaponType != 62) {
				dmg *= 1.58; // 対象の被ダメージ58%増加
			}
		}

		// 魔法武器はここから
		if (_weaponId == 2 || _weaponId == 200002) { // ダイスダガー
			dmg = L1WeaponSkill.getDiceDaggerDamage(_pc, _targetPc, weapon);
		} else if (_weaponId == 204 || _weaponId == 100204) { // 真紅のクロスボウ
			L1WeaponSkill.giveFettersEffect(_pc, _targetPc);
		} else {
			dmg += L1WeaponSkill.getWeaponSkillDamage(_pc, _target);
		}
		// 魔法武器ここまで

		if (_targetPc.hasSkillEffect(IMMUNE_TO_HARM)) {
			dmg /= 2;
		}
		if (_targetPc.isThroughAttack()) {
			dmg = 0;
		}

		if (dmg <= 0) {
			_drainHp = 0; // ダメージ無しの場合は吸収による回復はしない
		}

		// マジックドールスキル
		L1SkillUse l1skilluse = new L1SkillUse();
		if (L1MagicDoll.getEffectByDoll(_pc, SLOW) == SLOW) {
			l1skilluse.handleCommands(_pc,SLOW, // スロー
				_targetPc.getId(), _targetPc.getX(), _targetPc.getY(), null, 0,L1SkillUse.TYPE_GMBUFF);
		}
		if (L1MagicDoll.getEffectByDoll(_pc, CURSE_PARALYZE) == CURSE_PARALYZE) {
			l1skilluse.handleCommands(_pc,CURSE_PARALYZE, // カーズパラライズ
				_targetPc.getId(), _targetPc.getX(), _targetPc.getY(), null, 0,L1SkillUse.TYPE_GMBUFF);
		}
		if (L1MagicDoll.getEffectByDoll(_pc, VAMPIRIC_TOUCH) == VAMPIRIC_TOUCH) {
				L1Skill l1skills = SkillTable.getInstance().findBySkillId(VAMPIRIC_TOUCH); // バンパイアリックタッチ
				L1Magic magic = new L1Magic(_pc, _targetPc);

				_pc.sendPackets(new S_SkillSound(_pc.getId(), l1skills.getCastGfx()));
				_pc.broadcastPacket(new S_SkillSound(_pc.getId(), l1skills.getCastGfx()));

				int damage = magic.calcMagicDamage(l1skills.getSkillId());
				_targetPc.sendPackets(new S_DoActionGFX(_targetPc.getId(), ActionCodes.ACTION_Damage));
				_targetPc.broadcastPacket(new S_DoActionGFX(_targetPc.getId(), ActionCodes.ACTION_Damage));
				_targetPc.receiveDamage(_pc, damage, false);
				_pc.setCurrentHp(_pc.getCurrentHp() + damage);
		}

		return (int) dmg;
	}

	// ●●●● プレイヤー から ＮＰＣ へのダメージ算出 ●●●●
	public int calcPcNpcDamage() {
		double dmg;

		if (_weaponType2 == 14) { // キーリンク
			if (_targetNpc.isThroughAttack()) {
				return 0;
			}
			if (_targetNpc.isCounterMagic()) {
				return 0;
			}
			dmg = _magic.calcMagicDamage(_weaponSpecial);
			sendKiringkuEffect(_weaponSpecial);
			dmg += _weaponEnchant;
			dmg += calcAttrEnchantDmg();
			 // ＤＢでキーリングにスキルが設定されている場合
			dmg += L1WeaponSkill.getWeaponSkillDamage(_pc, _target);
			return (int) dmg;
		}

		if (_weaponType != 20 && _weaponType != 62) {
			L1CounterBarrier cb = new L1CounterBarrier(_target, _pc);
			if (cb.isCounterBarrierNpc()) {
				cb.commitCounterBarrier();
				return 0;
			}
		}

		int weaponMaxDamage = 0;
		if (_targetNpc.getNpcTemplate().getSize().equalsIgnoreCase("small")
				&& _weaponSmall > 0) {
			weaponMaxDamage = _weaponSmall;
		} else if (_targetNpc.getNpcTemplate().getSize().equalsIgnoreCase(
				"large")
				&& _weaponLarge > 0) {
			weaponMaxDamage = _weaponLarge;
		}

		int weaponBaselineDamage = 0; // 武器の基礎値攻撃力
		if (_weaponType == 58
				&& (_random.nextInt(1000) + 1) <= _weaponSpecial) { // クリティカルヒット
			weaponBaselineDamage = weaponMaxDamage;
			_pc.sendPackets(new S_SkillSound(_pc.getId(), 3671));
			_pc.broadcastPacket(new S_SkillSound(_pc.getId(), 3671));
		} else if (_weaponType == 0 || _weaponType == 20 || _weaponType == 62) {
			// 素手 、 弓、ガントトレット
			weaponBaselineDamage = 0;
		} else {
			weaponBaselineDamage = _random.nextInt(weaponMaxDamage) + 1;
		}
		if (_pc.hasSkillEffect(SOUL_OF_FLAME)) {
			if (_weaponType != 20 && _weaponType != 62) {
				weaponBaselineDamage = weaponMaxDamage;
			}
		}
		int weaponDamage = weaponBaselineDamage + _weaponAddDmg; // 武器の性能のみの算出
		int weaponAttrDmg = calcAttrEnchantDmg(); // 属性ダメージ

		// ダブルヒット
		if (_weaponType == 54 && ((_random.nextInt(1000) + 1) <= _weaponSpecial)) {
			// ダブルヒットに乗るのは、基礎値・強化値・武器追加打撃値・武器エンチャ（シャドウファング等）・祝福効果
			weaponDamage += weaponDamage + _weaponEnchant + calcBlessDmg();
			_pc.sendPackets(new S_SkillSound(_pc.getId(), 3398));
			_pc.broadcastPacket(new S_SkillSound(_pc.getId(), 3398));
		}

		// ダブルブレイク処理
		if (_pc.hasSkillEffect(DOUBLE_BRAKE) && (_weaponType == 54 || _weaponType == 58)) {
			if (_random.nextInt(3) == 1) {
				weaponDamage += weaponDamage + weaponAttrDmg + calcMaterialDmg();
			}
		}

		int weaponTotalDamage = weaponDamage + _weaponAddDmg + _weaponEnchant;

		weaponTotalDamage += weaponAttrDmg; // 属性強化ダメージボーナス

		weaponTotalDamage += calcHpDrain(weaponTotalDamage); // HP吸収武器

		weaponTotalDamage += calcMaterialDmg(); // 素材ダメージボーナス
		weaponTotalDamage += calcBlessDmg(); // 祝福ダメージボーナス

		if (_weaponType != 20 && _weaponType != 62) {
			dmg = weaponTotalDamage + _statusDamage + _pc.getDmgup()
					+ _pc.getOriginalDmgup();
		} else {
			dmg = weaponTotalDamage + _statusDamage + _pc.getBowDmgup()
					+ _pc.getOriginalBowDmgup();
		}

		if (_weaponType == 20) { // 弓
			if (_arrow != null) {
				int add_dmg = 0;
				if (_targetNpc.getNpcTemplate().getSize().equalsIgnoreCase("large")) {
					add_dmg = _arrow.getItem().getDmgLarge();
				} else {
					add_dmg = _arrow.getItem().getDmgSmall();
				}
				if (add_dmg == 0) {
					add_dmg = 1;
				}
				if (_targetNpc.getNpcTemplate().isHard()) {
					add_dmg /= 2;
				}
				dmg = dmg + _random.nextInt(add_dmg) + 1;
			} else if (_sayhaGfx != -1) { // サイハの弓
				dmg += _random.nextInt(_sayhaDmg) + 1;
			}
		} else if (_weaponType == 62) { // ガントトレット
			int add_dmg = 0;
			if (_targetNpc.getNpcTemplate().getSize()
					.equalsIgnoreCase("large")) {
				add_dmg = _sting.getItem().getDmgLarge();
			} else {
				add_dmg = _sting.getItem().getDmgSmall();
			}
			if (add_dmg == 0) {
				add_dmg = 1;
			}
			dmg = dmg + _random.nextInt(add_dmg) + 1;
		}

		if (_pc.hasSkillEffect(BURNING_SLASH)) {
			_pc.sendPackets(new S_SkillSound(_targetNpc.getId(), 6591));
			_pc.broadcastPacket(new S_SkillSound(_targetNpc.getId(), 6591));
			_pc.killSkillEffectTimer(BURNING_SLASH);
		}

		if (_weaponType == 0) { // 素手
			dmg = (_random.nextInt(5) + 4) / 4;
		}

		if (_weaponType2 != 14
				&& (_skillId == BONE_BREAK || _skillId == SMASH_ENERGY || _skillId == EYES_BREAKER)) {
			dmg += _skillDamage;
		}

		if (_weaponType != 20 && _weaponType != 62) { // 防具による追加ダメージ
			dmg += _pc.getDmgModifierByArmor();
		} else {
			dmg += _pc.getBowDmgModifierByArmor();
		}

		if (_weaponType != 20 && _weaponType != 62) {// マジックドールによる追加ダメージ
			dmg += L1MagicDoll.getDamageAddByDoll(_pc);
		}

		if (_pc.hasSkillEffect(COOKING_2_0_N) // 料理による追加ダメージ
				|| _pc.hasSkillEffect(COOKING_2_0_S)
				|| _pc.hasSkillEffect(COOKING_3_2_N)
				|| _pc.hasSkillEffect(COOKING_3_2_S)) {
			if (_weaponType != 20 && _weaponType != 62) {
				dmg += 1;
			}
		}

		if (_pc.hasSkillEffect(COOKING_2_3_N) // 料理による追加ダメージ
				|| _pc.hasSkillEffect(COOKING_2_3_S)
				|| _pc.hasSkillEffect(COOKING_3_0_N)
				|| _pc.hasSkillEffect(COOKING_3_0_S)) {
			if (_weaponType == 20 || _weaponType == 62) {
				dmg += 1;
			}
		}

		if (_pc.hasSkillEffect(MAGIC_EYE_OF_VALAKAS) // 魔眼による追加ダメージ
				|| _pc.hasSkillEffect(MAGIC_EYE_OF_LIFE)) {
			int _damageChance = _random.nextInt(100) + 1;
			if (_damageChance <= 10) {
				dmg += 2;
			}
		}

		dmg += getAddDamage(); // 特殊追加ダメージ

		if (_pc.hasSkillEffect(EYES_BREAKER)) { // ＰＣがアイズブレイカ—中。
			dmg -= 5;
		}

		// バーニングスピリット・エレメンタルファイアー
		if (_pc.hasSkillEffect(BURNING_SPIRIT) || (_pc.hasSkillEffect(ELEMENTAL_FIRE))) {
			if (_random.nextInt(3) == 1) { // 3分の1
				dmg *= 1.50D;
			}
		}

		dmg = calcWraknessExposure(dmg); // 弱点露出処理

		if (_pc.hasSkillEffect(ILLUSION_AVATAR)) { // アバターは最終ダメージに+10
			dmg += 10;
		}

		// ■■■■■■■■■■ ここから防御側のダメージ軽減系 ■■■■■■■■■■

		dmg -= calcNpcDamageReduction();

		if (_targetNpc.hasSkillEffect(KNIGHTVALD_REDUCTION)) {
			dmg -= (_random.nextInt(10) + 11);
		}

		// ■■■■■■■■■■ 防御側のダメージ軽減系ここまで ■■■■■■■■■■

		// アーマーブレイクにアバターが乗るかは不明
		if (_targetNpc.hasSkillEffect(ARMOR_BREAK)) {
			if (_weaponType != 20 && _weaponType != 62) {
				dmg *= 1.58; // 対象の被ダメージ58%増加
			}
		}

		// プレイヤーからペット、サモンに攻撃
		boolean isNowWar = false;
		int castleId = L1CastleLocation.getCastleIdByArea(_targetNpc);
		if (castleId > 0) {
			isNowWar = WarTimeController.getInstance().isNowWar(castleId);
		}
		if (!isNowWar) {
			if (_targetNpc instanceof L1PetInstance) {
				dmg /= 8;
			}
			if (_targetNpc instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) _targetNpc;
				if (summon.isExsistMaster()) {
					dmg /= 8;
				}
			}
		}

		// 魔法武器ここから
		if (_weaponId == 2 || _weaponId == 200002) { // ダイスダガー
			dmg = L1WeaponSkill.getDiceDaggerDamage(_pc, _targetPc, weapon);
		} else if (_weaponId == 204 || _weaponId == 100204) { // 真紅のクロスボウ
			L1WeaponSkill.giveFettersEffect(_pc, _targetNpc);
		} else {
			dmg += L1WeaponSkill.getWeaponSkillDamage(_pc, _target);
		}
		// ここまで

		// 特定NPC 固定ダメージ判定
		int fixedDamage = _pc.getFixedDamage(_targetNpc.getNpcTemplate().getNpcId());
		if (fixedDamage >= 0) {
			dmg = fixedDamage;
		}

		if (_targetNpc.isThroughAttack()) {
			dmg = 0;
		}

		if (dmg <= 0) {
			_drainHp = 0; // ダメージ無しの場合は吸収による回復はしない
		}

		// マジックドールスキル
		L1SkillUse l1skilluse = new L1SkillUse();
		if (L1MagicDoll.getEffectByDoll(_pc, SLOW) == SLOW) {
			l1skilluse.handleCommands(_pc,SLOW, // スロー
				_targetNpc.getId(), _targetNpc.getX(), _targetNpc.getY(), null, 0,L1SkillUse.TYPE_GMBUFF);
		}
		if (L1MagicDoll.getEffectByDoll(_pc, CURSE_PARALYZE) == CURSE_PARALYZE) {
			l1skilluse.handleCommands(_pc,CURSE_PARALYZE, // カーズパラライズ
				_targetNpc.getId(), _targetNpc.getX(), _targetNpc.getY(), null, 0,L1SkillUse.TYPE_GMBUFF);
		}
		if (L1MagicDoll.getEffectByDoll(_pc, VAMPIRIC_TOUCH) == VAMPIRIC_TOUCH) {
				L1Skill l1skills = SkillTable.getInstance().findBySkillId(
						VAMPIRIC_TOUCH); // バンパイアリックタッチ
				L1Magic magic = new L1Magic(_pc, _targetNpc);

				_pc.sendPackets(new S_SkillSound(_pc.getId(), l1skills.getCastGfx()));
				_pc.broadcastPacket(new S_SkillSound(_pc.getId(), l1skills.getCastGfx()));

				int damage = magic.calcMagicDamage(l1skills.getSkillId());
				_targetNpc.broadcastPacket(new S_DoActionGFX(
						_targetNpc.getId(), ActionCodes.ACTION_Damage));
				_targetNpc.receiveDamage(_pc, damage);
				_pc.setCurrentHp(_pc.getCurrentHp() + damage);
		}
		return (int) dmg;
	}

	// ●●●● ＮＰＣ から プレイヤー へのダメージ算出 ●●●●
	private int calcNpcPcDamage() {

		// パプリオンの加護はダメージ発生前に発動する
		if (_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_FAFURION)) {
			_targetPc.commitFafurionHydroArmor();
		}
		// ハロウィンの加護はダメージ発生前に発動する
		if (_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_HALLOWEEN)) {
			_targetPc.commitDragonicHalloweenArmor();
		}

		int lvl = _npc.getLevel();
		double dmg = 0D;

		dmg = strDmg[_npc.getStr()];

		if (_npc instanceof L1PetInstance) {
			dmg += (lvl / 16); // ペットはLV16毎に追加打撃
			dmg += ((L1PetInstance) _npc).getDamageByWeapon();
		} else {
			dmg += (lvl / 5);
			int magnification = Math.max(1, (lvl / 15));
			dmg += (int) (dmg * magnification);
		}

		dmg += _npc.getDmgup();

		if (isUndeadDamage()) {
			dmg *= 1.1;
		}

		if (_npc.getNpcTemplate().getBoss()) { // ボス
			dmg += (int) (lvl / 3.0D);
		}

		dmg = dmg * getLeverage() / 10;

		if (lvl > 11) {
			dmg += _random.nextInt(lvl / 3);
		} else {
			dmg += _random.nextInt(3);
		}

		dmg = getAddDamage() == 0 ? dmg : getAddDamage(); // 特殊追加ダメージ

		if (_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_PLATE) ||
				_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_SCALE) ||
				_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_LEATHER) ||
				_targetPc.hasSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_ROBE)) {
			_targetPc.commitLindoviorArmor((int) dmg); // 魔力のリンドビオルの加護処理
		}

		dmg -= calcPcDefense();

		if (_targetPc.hasSkillEffect(SHIELD_OF_TEAIROR)) { // 反逆者のシールド
			if (_random.nextInt(100) <= _targetPc.getTeairorChance()) {
				dmg -= 50;
				_targetPc.sendPackets(new S_SkillSound(_targetPc.getId(), 6320));
				_targetPc.broadcastPacket(new S_SkillSound(_targetPc.getId(), 6320));
			}
		}

		// ルームティスレッドイアリング・発動時のエフェクトは不明
		if (_targetPc.hasSkillEffect(EARRING_OF_ROOMTIS_RED)) {
			if (_random.nextInt(100) <= _targetPc.getRedEarringChance()) {
				dmg -= 20;
				//_targetPc.sendPackets(new S_SkillSound(_targetPc.getId(), 6320));
				//_targetPc.broadcastPacket(new S_SkillSound(_targetPc.getId(), 6320));
			}
		}

		if (_npc.hasSkillEffect(EYES_BREAKER)) { // ＮＰＣがアイズブレイカ—中。
			dmg -= 5;
		}

		dmg -= _targetPc.getDamageReductionByArmor(); // 防具によるダメージ軽減

		// マジックドール效果 - ダメージリダクション
		dmg -= L1MagicDoll.getDamageReductionByDoll(_targetPc);

		if (_targetPc.isCookingReduction()) { // 幻想料理によるダメージ軽減
			dmg -= 5;
		}
		if (_targetPc.hasSkillEffect(COOKING_1_7_S) // デザートによるダメージ軽減
				|| _targetPc.hasSkillEffect(COOKING_2_7_S)
				|| _targetPc.hasSkillEffect(COOKING_3_7_S)) {
			dmg -= 5;
		}

		if (_targetPc.hasSkillEffect(REDUCTION_ARMOR)) {
			int targetPcLvl = _targetPc.getLevel();
			if (targetPcLvl < 50) {
				targetPcLvl = 50;
			}
			dmg -= (targetPcLvl - 50) / 5 + 1;
		}
		if (_targetPc.hasSkillEffect(SPIRIT_OF_BLACK_SNAKE)) {
			dmg -= 3;
		}
		if (_targetPc.hasSkillEffect(ARMOR_BREAK)) {
			if (_npc.getNpcTemplate().getRanged() > 3) {
				dmg *= 1.58; // 対象の被ダメージ58%増加
			}
		}
		if (_targetPc.hasSkillEffect(DRAGON_SKIN)) {
			dmg -= 5; // キャラクターケアアップデート
		}
		if (_targetPc.hasSkillEffect(PATIENCE)) {
			dmg -= 2;
		}
		if (_npc.isWeaponBreaked()) { // ＮＰＣがウェポンブレイク中。
			dmg /= 2;
		}
		if (_targetPc.hasSkillEffect(IMMUNE_TO_HARM)) {
			dmg /= 2;
		}
		if (_targetPc.isThroughAttack()) {
			dmg = 0;
		}

		// ペット、サモンからプレイヤーに攻撃
		boolean isNowWar = false;
		int castleId = L1CastleLocation.getCastleIdByArea(_targetPc);
		if (castleId > 0) {
			isNowWar = WarTimeController.getInstance().isNowWar(castleId);
		}
		if (!isNowWar) {
			if (_npc instanceof L1PetInstance) {
				dmg /= 8;
			}
			if (_npc instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) _npc;
				if (summon.isExsistMaster()) {
					dmg /= 8;
				}
			}
		}

		addNpcPoisonAttack(_npc, _targetPc);

		return (int) dmg;
	}

	// ●●●● ＮＰＣ から ＮＰＣ へのダメージ算出 ●●●●
	private int calcNpcNpcDamage() {
		int lvl = _npc.getLevel();
		double dmg = strDmg[_npc.getStr()];

		if (_npc instanceof L1PetInstance) {
			dmg += (lvl / 16); // ペットはLV16毎に追加打撃
			dmg += ((L1PetInstance) _npc).getDamageByWeapon();
		} else {
			int magnification = Math.max(1, (lvl / 15));
			dmg += (int) (dmg * magnification);
		}

		if (isUndeadDamage()) {
			dmg *= 1.1;
		}

		dmg += getAddDamage(); // 特殊追加ダメージ

		if (_npc.getNpcTemplate().getBoss()) { // ボス
			dmg += (int) (lvl / 3.0D);
		}

		dmg = dmg * getLeverage() / 10;

		if (lvl > 11) {
			dmg += _random.nextInt(lvl / 3);
		} else {
			dmg += _random.nextInt(2);
		}

		dmg -= calcNpcDamageReduction();

		if (_targetNpc.hasSkillEffect(KNIGHTVALD_REDUCTION)) {
			dmg -= (_random.nextInt(10) + 11);
		}

		if (_npc.isWeaponBreaked()) { // ＮＰＣがウェポンブレイク中。
			dmg /= 2;
		}

		if (_npc.hasSkillEffect(EYES_BREAKER)) { // ＮＰＣがアイズブレイカ—中。
			dmg -= 5;
		}

		if (_targetNpc.hasSkillEffect(ARMOR_BREAK)) {
			if (_npc.getNpcTemplate().getRanged() > 3) {
				dmg *= 1.58; // 対象の被ダメージ58%増加
			}
		}
		addNpcPoisonAttack(_npc, _targetNpc);

		// 特定NPC 固定ダメージ判定
		int fixedDamage = _npc.getFixedDamage(_targetNpc.getNpcTemplate().getNpcId());
		if (fixedDamage >= 0) {
			dmg = fixedDamage;
		}

		if (_targetNpc.isThroughAttack()) {
			dmg = 0;
		}

		return (int) dmg;
	}

	// ●●●● チェーンソードによる弱点露出 ●●●●
	private double calcWraknessExposure(double dmg) {
		if (_pc.getExposureTargetId() != _target.getId()) { // ターゲットが違う場合、弱点露出削除
			if (_pc.hasSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV1)) {
				_pc.killSkillEffectTimer(STATUS_WEAKNESS_EXPOSURE_LV1);
				_pc.removeSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV1);
			}
			if (_pc.hasSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV1)) {
				_pc.killSkillEffectTimer(STATUS_WEAKNESS_EXPOSURE_LV2);
				_pc.removeSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV2);
			}
			if (_pc.hasSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV1)) {
				_pc.killSkillEffectTimer(STATUS_WEAKNESS_EXPOSURE_LV3);
				_pc.removeSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV3);
			}
			_pc.sendPackets(new S_SkillIconGFX(75, 0));
		}
		if (_pc.isFoeSlayer()) {
			if (_pc.hasSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV1)) {
				dmg += 20;
			} else if (_pc.hasSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV2)) {
				dmg += 40;
			} else if (_pc.hasSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV3)) {
				dmg += 60;
			}
		} else if (_weaponType2 == 13
				&& (_random.nextInt(1000) + 1) <= _weaponSpecial) {
			if (_pc.hasSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV1)) {
				_pc.killSkillEffectTimer(STATUS_WEAKNESS_EXPOSURE_LV1);
				_pc.removeSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV1);
				_pc.setSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV2, 15000);
				_pc.sendPackets(new S_SkillIconGFX(75, 2));
			} else if (_pc.hasSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV2)) {
				_pc.killSkillEffectTimer(STATUS_WEAKNESS_EXPOSURE_LV2);
				_pc.removeSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV2);
				_pc.setSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV3, 15000);
				_pc.sendPackets(new S_SkillIconGFX(75, 3));
			} else if (_pc.hasSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV3)) {
				// LV3の場合は上書きしない。
			} else {
				_pc.setSkillEffect(STATUS_WEAKNESS_EXPOSURE_LV1, 15000);
				_pc.setExposureTargetId(_target.getId());
				_pc.sendPackets(new S_SkillIconGFX(75, 1));
			}
		}
		return dmg;
	}

	// ●●●● プレイヤーのＡＣによるダメージ軽減 ●●●●
	private int calcPcDefense() {
		int ac = Math.max(0, 10 - _targetPc.getAc());
		int acDefMax = _targetPc.getClassFeature().getAcDefenseMax(ac);
		return _random.nextInt(acDefMax + 1);
	}

	// ●●●● ＮＰＣのダメージリダクションによる軽減 ●●●●
	private int calcNpcDamageReduction() {
		return _targetNpc.getNpcTemplate().getDamageReduction();
	}

	// ●●●● 武器の材質による追加ダメージ算出 ●●●●
	private int calcMaterialDmg() {
		int damage = 0;
		int undead = _targetNpc.getNpcTemplate().getUndead();

		// シルバー、ミスリル、オリハルコン
		if (_weaponMaterial == 14 || _weaponMaterial == 17 || _weaponMaterial == 22) {
			if (undead == 1 || undead == 3 || undead == 5) { // アンデッド系・アンデッド系ボス・銀特効モンスター
				damage += _random.nextInt(20) + 1;
			}
		}
/*		不確定要素なのでコメントアウト
		if (_weaponMaterial == 17 || _weaponMaterial == 22) { // ミスリル・オリハルコン
			if (undead == 2) { // 悪魔系モンスター
				damage += _random.nextInt(3) + 1;
			}
		}
*/
		return damage;
	}

	// ●●●● 武器の祝福による追加ダメージ算出 ●●●●
	private int calcBlessDmg() {
		int damage = 0;
		int undead = _targetNpc.getNpcTemplate().getUndead();
		if (_weaponBless == 1 && (undead == 1 || undead == 2 || undead == 3)) { // 祝福武器、かつ、アンデッド系・悪魔系・アンデッド系ボス
			damage += _random.nextInt(4) + 1 + 2;
		}
		if (_pc.getWeapon() != null && _weaponType != 20 && _weaponType != 62
				&& weapon.getHolyDmgByMagic() != 0
				&& (undead == 1 || undead == 3)) {
			damage += weapon.getHolyDmgByMagic();
		}
		return damage;
	}

	// ●●●● 武器の属性強化による追加ダメージ算出 ●●●●
	private int calcAttrEnchantDmg() {
		int damage = 0;
		if (_weaponAttrEnchantLevel == 0) {
			return 0;
		}
		if (_weaponAttrEnchantLevel == 1) {
			damage = 1;
		} else if (_weaponAttrEnchantLevel == 2) {
			damage = 3;
		} else if (_weaponAttrEnchantLevel == 3) {
			damage = 5;
		} else if (_weaponAttrEnchantLevel == 4) {
			damage = 7;
		} else if (_weaponAttrEnchantLevel == 5) {
			damage = 9;
		}

		int attrFactor = _target.getAttrFactor(_weaponAttrEnchantKind); // 属性因子取得

		if (attrFactor <= -32) { // 属性因子32以下
			damage = (int) (damage * 2.0D);
		} else if (attrFactor <= -26) { // 属性因子-26以下
			damage = (int) (damage * 1.8D);
		} else if (attrFactor <= -20) { // 属性因子-20以下
			damage = (int) (damage * 1.6D);
		} else if (attrFactor <= -13) { // 属性因子-13以下
			damage = (int) (damage * 1.4D);
		} else if (attrFactor <= -7) { // 属性因子-7以下
			damage = (int) (damage * 1.2D);
		} else if (attrFactor <= 0) { // 属性因子0以下
//			damage = (int) (damage * 1.0D);
		} else if (attrFactor <= 6) { // 属性因子6以下
			damage = (int) (damage * 0.8D);
		} else if (attrFactor <= 12) { // 属性因子12以下
			damage = (int) (damage * 0.6D);
		} else if (attrFactor <= 19) { // 属性因子19以下
			damage = (int) (damage * 0.4D);
		} else if (attrFactor <= 25) { // 属性因子25以下
			damage = (int) (damage * 0.2D);
		} else { // 属性因子26以上
			 damage = 0;
		}
		return damage;
	}

	// ●●●● ＮＰＣのアンデッドの夜間攻撃力の変化 ●●●●
	private boolean isUndeadDamage() {
		boolean flag = false;
		int undead = _npc.getNpcTemplate().getUndead();
		boolean isNight = L1GameTimeClock.getInstance().currentTime().isNight();
		if (isNight && (undead == 1 || undead == 3 || undead == 4)) { // 18〜6時、かつ
			// 、
			// アンデッド系・アンデッド系ボス・弱点無効のアンデッド系
			flag = true;
		}
		return flag;
	}

	// ●●●● ＮＰＣの毒攻撃を付加 ●●●●
	private void addNpcPoisonAttack(L1Character attacker, L1Character target) {
		if (target.isThroughAttack()) {
			return;
		}
		if (_npc.getNpcTemplate().getPoisonAtk() != 0) { // 毒攻撃あり
			if (15 >= _random.nextInt(100) + 1) { // 15%の確率で毒攻撃
				if (_npc.getNpcTemplate().getPoisonAtk() == 1) { // 通常毒
					int interval = _npc.getNpcTemplate().getPoisonInterval();
					int poisonDamage = _npc.getNpcTemplate().getPoisonDamage();
					L1DamagePoison.doInfection(attacker, target, interval, poisonDamage);
				} else if (_npc.getNpcTemplate().getPoisonAtk() == 2) { // 沈黙毒
					L1SilencePoison.doInfection(target);
				} else if (_npc.getNpcTemplate().getPoisonAtk() == 4) { // 麻痺毒
					// interval後にpoisonDamage秒麻痺。
					int interval = _npc.getNpcTemplate().getPoisonInterval();
					int poisonTime = _npc.getNpcTemplate().getPoisonDamage() * 1000;
					L1ParalysisPoison.doInfection(target, interval, poisonTime);
				}
			}
		}
	}

	// ■■■■ マナスタッフ、鋼鉄のマナスタッフ、マナバゼラードのMP吸収量算出 ■■■■
	public void calcStaffOfMana() {
		if (_isMpDrain) { //
			int som_lvl = _weaponEnchant + 3; // 最大MP吸収量を設定
			if (som_lvl < 0) {
				som_lvl = 0;
			}
			// MP吸収量をランダム取得
			_drainMana = _random.nextInt(som_lvl) + 1;
			// 最大MP吸収量を9に制限
			if (_drainMana > Config.MANA_DRAIN_LIMIT_PER_SOM_ATTACK) {
				_drainMana = Config.MANA_DRAIN_LIMIT_PER_SOM_ATTACK;
			}
		}
	}

	// ■■■■■■■ HP吸収武器 ■■■■■■■
	private int calcHpDrain(int dmg) {

		if (!_isHpDrain) { // HP吸収武器でない
			return 0;
		}

		int damage = 0;

		if ((_random.nextInt(1000) + 1) < _hpDrainChance) {
			damage = (int) (dmg * 0.15D) + 1;
			if (damage <= 0) {
				damage = 0;
			}
		}
		_drainHp = damage;

		return damage;
	}

	// ■■■■ ＰＣの毒攻撃を付加 ■■■■
	public void addPcPoisonAttack(L1Character attacker, L1Character target) {
		int chance = _random.nextInt(100) + 1;
		if (((_weaponId == 13) || (_weaponId == 44 // FOD、古代のダークエルフソード
				) || ((_weaponId != 0) && _pc.hasSkillEffect(ENCHANT_VENOM))) // エンチャント
				// ベノム中
				&& (chance <= 10)) {
			// 通常毒、3秒周期、ダメージHP-5
			L1DamagePoison.doInfection(attacker, target, 3000, 5);
		} else {
			// マジックドール：ラミア
			if (L1MagicDoll.getEffectByDoll(attacker, ENCHANT_VENOM) == ENCHANT_VENOM) {
				L1DamagePoison.doInfection(attacker, target, 3000, 5);
			}
		}
	}

	/* ■■■■■■■■■■■■■■ 攻撃モーション送信 ■■■■■■■■■■■■■■ */

	public void action() {
		if (_calcType == PC_PC || _calcType == PC_NPC) {
			actionPc();
		} else if (_calcType == NPC_PC || _calcType == NPC_NPC) {
			actionNpc();
		}
	}

	// ●●●● プレイヤーの攻撃モーション送信 ●●●●
	private void actionPc() {
		boolean isFly = false;
		int attackGrfxId = -1;

//		_pc.setHeading(_pc.targetDirection(_targetX, _targetY)); // 向きのセット

		if (_weaponType == 20) { // 弓
			isFly = true;
			if (_arrow != null) { // 矢がある場合
				attackGrfxId = 66;
				_pc.getInventory().removeItem(_arrow, 1);
			} else if (_sayhaGfx != -1) { // 弓-矢が無くてサイハの場合
				attackGrfxId = _sayhaGfx;
			}

			if (_pc.getTempCharGfx() == 8719)
				attackGrfxId = 8721;
			if (_pc.getTempCharGfx() == 8900)
				attackGrfxId = 8904;
			if (_pc.getTempCharGfx() == 8913)
				attackGrfxId = 8916;
			if (_pc.getTempCharGfx() == 7959 || _pc.getTempCharGfx() == 7967 ||
				_pc.getTempCharGfx() == 7968 || _pc.getTempCharGfx() == 7969 ||
				_pc.getTempCharGfx() == 7970)
				attackGrfxId = 7972;

		} else if (_weaponType == 62 && _sting != null) { // ガントレット - スティング有
			isFly = true;
			attackGrfxId = 2989;
			_pc.getInventory().removeItem(_sting, 1);
		}

		if (isFly) { // 遠距離攻撃(弓、ガントレット)
			if (attackGrfxId != -1) {
				_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, attackGrfxId,
						_targetX, _targetY, _isHit));
				_pc.broadcastPacket(new S_UseArrowSkill(_pc, _targetId, attackGrfxId,
						_targetX, _targetY, _isHit));
				if (_isHit) {
					_target.broadcastPacketExceptTargetSight(new S_DoActionGFX(
							_targetId, ActionCodes.ACTION_Damage), _pc);
				}
			}
		} else { // 近距離攻撃
			if (_isHit) {
				_pc.sendPackets(new S_AttackPacket(_pc, _targetId, ActionCodes.ACTION_Attack));
				_pc.broadcastPacket(new S_AttackPacket(_pc, _targetId, ActionCodes.ACTION_Attack));
				_target.broadcastPacketExceptTargetSight(new S_DoActionGFX(
						_targetId, ActionCodes.ACTION_Damage), _pc);
			} else {
				if (_targetId > 0) {
					_pc.sendPackets(new S_AttackMissPacket(_pc, _targetId));
					_pc.broadcastPacket(new S_AttackMissPacket(_pc, _targetId));
				} else {
					_pc.sendPackets(new S_AttackPacket(_pc, 0,
							ActionCodes.ACTION_Attack));
					_pc.broadcastPacket(new S_AttackPacket(_pc, 0,
							ActionCodes.ACTION_Attack));
				}
			}
		}
	}

	// ●●●● ＮＰＣの攻撃モーション送信 ●●●●
	private void actionNpc() {
		int _npcObjectId = _npc.getId();
		int bowActId = 0;
		int actId = 0;

//		_npc.setHeading(_npc.targetDirection(_targetX, _targetY)); // 向きのセット

		// ターゲットとの距離が2以上あれば遠距離攻撃
		boolean isLongRange = (_npc.getLocation().getTileLineDistance(
				new Point(_targetX, _targetY)) > 1);
		bowActId = _npc.getNpcTemplate().getBowActId();

		if (getActId() > 0) {
			actId = getActId();
		} else {
			actId = ActionCodes.ACTION_Attack;
		}

		if (!_isHit) { // Miss
			_damage = 0;
		}

		// 距離が2以上、攻撃者の弓のアクションIDがある場合は遠攻撃
		if (isLongRange && (bowActId > 0)) {
			_npc.broadcastPacket(new S_UseArrowSkill(_npc, _targetId, bowActId,
					_targetX, _targetY, _isHit));
		} else {
			if (_isHit) {
				if (getGfxId() > 0) {
					_npc
							.broadcastPacket(new S_UseAttackSkill(_target,
									_npcObjectId, getGfxId(), _targetX,
									_targetY, actId));
					_target.broadcastPacketExceptTargetSight(new S_DoActionGFX(
							_targetId, ActionCodes.ACTION_Damage), _npc);
				} else {
					_npc.broadcastPacket(new S_AttackPacketForNpc(_target,
							_npcObjectId, actId));
					_target.broadcastPacketExceptTargetSight(new S_DoActionGFX(
							_targetId, ActionCodes.ACTION_Damage), _npc);
				}
			} else {
				if (getGfxId() > 0) {
					_npc.broadcastPacket(new S_UseAttackSkill(_target,
							_npcObjectId, getGfxId(), _targetX, _targetY,
							actId, 0));
				} else {
					_npc.broadcastPacket(new S_AttackMissPacket(_npc,
							_targetId, actId));
				}
			}
		}
	}

	/*
	 * // 飛び道具（矢、スティング）がミスだったときの軌道を計算 public void calcOrbit(int cx, int cy, int
	 * head) // 起点Ｘ 起点Ｙ 今向いてる方向 { float dis_x = Math.abs(cx - _targetX); //
	 * Ｘ方向のターゲットまでの距離 float dis_y = Math.abs(cy - _targetY); // Ｙ方向のターゲットまでの距離
	 * float dis = Math.max(dis_x, dis_y); // ターゲットまでの距離 float avg_x = 0; float
	 * avg_y = 0; if (dis == 0) { // 目標と同じ位置なら向いてる方向へ真っ直ぐ if (head == 1) { avg_x
	 * = 1; avg_y = -1; } else if (head == 2) { avg_x = 1; avg_y = 0; } else if
	 * (head == 3) { avg_x = 1; avg_y = 1; } else if (head == 4) { avg_x = 0;
	 * avg_y = 1; } else if (head == 5) { avg_x = -1; avg_y = 1; } else if (head
	 * == 6) { avg_x = -1; avg_y = 0; } else if (head == 7) { avg_x = -1; avg_y
	 * = -1; } else if (head == 0) { avg_x = 0; avg_y = -1; } } else { avg_x =
	 * dis_x / dis; avg_y = dis_y / dis; }
	 *
	 * int add_x = (int) Math.floor((avg_x 15) + 0.59f); // 上下左右がちょっと優先な丸め int
	 * add_y = (int) Math.floor((avg_y 15) + 0.59f); // 上下左右がちょっと優先な丸め
	 *
	 * if (cx > _targetX) { add_x *= -1; } if (cy > _targetY) { add_y *= -1; }
	 *
	 * _targetX = _targetX + add_x; _targetY = _targetY + add_y; }
	 */

	/* ■■■■■■■■■■■■■■■ 計算結果反映 ■■■■■■■■■■■■■■■ */

	public void commit() {
		if (_isHit) {
			if (_calcType == PC_PC || _calcType == NPC_PC) {
				commitPc();
			} else if (_calcType == PC_NPC || _calcType == NPC_NPC) {
				commitNpc();
			}
		}

		// ダメージ値及び命中率確認用メッセージ
		if ((_calcType == PC_PC || _calcType == PC_NPC) && !_pc.getAttackLog()) {
			return;
		}
		if ((_calcType == PC_PC || _calcType == NPC_PC) && !_targetPc.getAttackLog()) {
			return;
		}

		String msg0 = "";
		String msg1 = I18N_ATTACK_TO; // に
		String msg2 = "";
		String msg3 = "";
		String msg4 = "";
		if (_calcType == PC_PC || _calcType == PC_NPC) { // アタッカーがＰＣの場合
			msg0 = _pc.getName();
		} else if (_calcType == NPC_PC) { // アタッカーがＮＰＣの場合
			msg0 = _npc.getName();
		}

		if (_calcType == NPC_PC || _calcType == PC_PC) { // ターゲットがＰＣの場合
			msg4 = _targetPc.getName();
			msg2 = "HitR" + _hitRate + "% THP" + _targetPc.getCurrentHp();
		} else if (_calcType == PC_NPC) { // ターゲットがＮＰＣの場合
			msg4 = _targetNpc.getName();
			msg2 = "Hit" + _hitRate + "% Hp" + _targetNpc.getCurrentHp();
		}
		msg3 = _isHit ? String.format(I18N_ATTACK_DMG, _damage) : I18N_ATTACK_MISS;
		// %dのダメージを与えました。 : 攻撃をミスしました。

		if (_calcType == PC_PC || _calcType == PC_NPC) { // アタッカーがＰＣの場合
			//_pc.sendPackets(new S_ServerMessage(166, msg0, msg1, msg2, msg3, msg4));
			// \f1%0が%4%1%3 %2
			_pc.sendPackets(new S_SystemMessage(I18N_ATTACK_GAVE_TEXT_COLOR +
				MessageFormat.format(I18N_ATTACK_FORMAT, msg0, msg4, msg3, msg2)));
			// {0}が{1}に{2} {3}
		}
		if (_calcType == NPC_PC || _calcType == PC_PC) { // ターゲットがＰＣの場合
			// _targetPc.sendPackets(new S_ServerMessage(166, msg0, msg1, msg2, msg3, msg4));
			// \f1%0が%4%1%3 %2
			_targetPc.sendPackets(new S_SystemMessage(I18N_ATTACK_RECEIVED_TEXT_COLOR +
				MessageFormat.format(I18N_ATTACK_FORMAT, msg0, msg4, msg3, msg2)));
			// {0}が{1}に{2} {3}
		}
	}

	// ●●●● プレイヤーに計算結果を反映 ●●●●
	private void commitPc() {
		if (_calcType == PC_PC) {
			if (_drainMana > 0 && _targetPc.getCurrentMp() > 0) {
				if (_drainMana > _targetPc.getCurrentMp()) {
					_drainMana = _targetPc.getCurrentMp();
				}
				short newMp = (short) (_targetPc.getCurrentMp() - _drainMana);
				_targetPc.setCurrentMp(newMp);
				newMp = (short) (_pc.getCurrentMp() + _drainMana);
				_pc.setCurrentMp(newMp);
			}
			if (_drainHp > 0) { // HP吸収による回復
				short newHp = (short) (_pc.getCurrentHp() + _drainHp);
				_pc.setCurrentHp(newHp);
			}
			_targetPc.receiveDamage(_pc, _damage, false);
		} else if (_calcType == NPC_PC) {
			_targetPc.receiveDamage(_npc, _damage, false);
		}
	}

	// ●●●● ＮＰＣに計算結果を反映 ●●●●
	private void commitNpc() {
		if (_calcType == PC_NPC) {
			if (_drainMana > 0) {
				int drainValue = _targetNpc.drainMana(_drainMana);
				int newMp = _pc.getCurrentMp() + drainValue;
				_pc.setCurrentMp(newMp);
				if (drainValue > 0) {
					int newMp2 = _targetNpc.getCurrentMp() - drainValue;
					_targetNpc.setCurrentMpDirect(newMp2);
				}
			}
			if (_drainHp > 0) { // HP吸収による回復
				short newHp = (short) (_pc.getCurrentHp() + _drainHp);
				_pc.setCurrentHp(newHp);
			}
			damageNpcWeaponDurability(); // 武器を損傷させる。
			_targetNpc.receiveDamage(_pc, _damage);
		} else if (_calcType == NPC_NPC) {
			_targetNpc.receiveDamage(_npc, _damage);
		}
	}

	/*
	 * 武器を損傷させる。 対NPCの場合、損傷確率は5%とする。祝福武器は2%とする。
	 */
	private void damageNpcWeaponDurability() {
		int chance = 5;
		int bchance = 2;

		/*
		 * 損傷しないNPC、素手、損傷しない武器使用、SOF中の場合何もしない。
		 */
		if (_calcType != PC_NPC
				|| _targetNpc.getNpcTemplate().isHard() == false
				|| _weaponType == 0 || weapon.getCanBeDmg() == false
				|| _pc.hasSkillEffect(SOUL_OF_FLAME)) {
			return;
		}
		// 通常の武器・呪われた武器
		if ((_weaponBless == 0 || _weaponBless == 2)
				&& ((_random.nextInt(100) + 1) < chance)) {
			// \f1あなたの%0が損傷しました。
			_pc.sendPackets(new S_ServerMessage(268, weapon.getLogName()));
			_pc.getInventory().receiveDamage(weapon, 1);
		}
		// 祝福された武器
		if (_weaponBless == 1 && ((_random.nextInt(100) + 1) < bchance)) {
			// \f1あなたの%0が損傷しました。
			_pc.sendPackets(new S_ServerMessage(268, weapon.getLogName()));
			_pc.getInventory().receiveDamage(weapon, 1);
		}
	}

	private void sendKiringkuEffect(int skillId) {
		int castgfx = SkillTable.getInstance().findBySkillId(skillId).getCastGfx();
		_pc.sendPackets(new S_SkillSound(_pc.getId(), castgfx));
		_pc.broadcastPacket(new S_SkillSound(_pc.getId(), castgfx));
	}
}