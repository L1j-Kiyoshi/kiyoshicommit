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

import static jp.l1j.server.model.skill.L1SkillId.*;
import jp.l1j.server.codes.ActionCodes;
import jp.l1j.server.datatables.SkillTable;
import jp.l1j.server.datatables.WeaponSkillTable;
import jp.l1j.server.model.instance.L1GuardInstance;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1MonsterInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.instance.L1PetInstance;
import jp.l1j.server.model.instance.L1SummonInstance;
import jp.l1j.server.model.skill.L1SkillUse;
import jp.l1j.server.packets.server.S_EffectLocation;
import jp.l1j.server.packets.server.S_Paralysis;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.packets.server.S_UseAttackSkill;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1Skill;

// Referenced classes of package jp.l1j.server.model:
// L1PcInstance

public class L1WeaponSkill {
	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private int _weaponId;

	private int _probability;

	private int _probEnchant;

	private int _fixDamage;

	private int _randomDamage;

	private int _skillId;

	private int _chaserCount;

	private int _arrowType;

	private boolean _isHpDrain;

	private boolean _isMr;

	public L1WeaponSkill(int weaponId, int probability, int probEnchant,
			int fixDamage, int randomDamage, int chaserCount, int skillId,
			int arrowType, boolean isHpDrain, boolean isMr) {
		_weaponId = weaponId;
		_probability = probability;
		_probEnchant = probEnchant;
		_fixDamage = fixDamage;
		_randomDamage = randomDamage;
		_chaserCount = chaserCount;
		_skillId = skillId;
		_arrowType = arrowType;
		_isHpDrain = isHpDrain;
		_isMr = isMr;
	}

	public int getWeaponId() {
		return _weaponId;
	}

	public int getProbability() {
		return _probability;
	}

	public int getProbEnchant() {
		return _probEnchant;
	}

	public int getFixDamage() {
		return _fixDamage;
	}

	public int getRandomDamage() {
		return _randomDamage;
	}

	public int getChaserCount() {
		return _chaserCount;
	}

	public int getSkillId() {
		return _skillId;
	}

	public int getArrowType() {
		return _arrowType;
	}
	public boolean isHpDrain() {
		return _isHpDrain;
	}

	public boolean isMr() {
		return _isMr;
	}

	public static double getWeaponSkillDamage(L1PcInstance pc, L1Character cha) {
		if (pc.getWeapon() == null) {
			return 0;
		}
		L1ItemInstance weapon = pc.getWeapon();
		L1WeaponSkill weaponSkill = WeaponSkillTable.getInstance().getTemplate(weapon.getItemId());
		if (pc == null || cha == null || weaponSkill == null) {
			return 0;
		}

		int rand = _random.nextInt(100) + 1;
		int chance = weaponSkill.getProbability() + (weaponSkill.getProbEnchant() * weapon.getEnchantLevel());
		if (rand > chance) {
			return 0;
		}

		int skillId = weaponSkill.getSkillId();
		if (skillId == 0) { // スキル未設定の場合
			return 0;
		}

		L1Skill skill = SkillTable.getInstance().findBySkillId(skillId);
		if (skill == null) { // スキルが無い
			return 0;
		}

		double damage = 0;
		boolean isChaser = false;

		if (weaponSkill.getFixDamage() != 0) { // 固定ダメージの場合
			damage = weaponSkill.getFixDamage();
			if (weaponSkill.getRandomDamage() != 0) {
				damage += _random.nextInt(weaponSkill.getRandomDamage());
			}
			if (!weaponSkill.isMr()) {
				L1Magic magic = new L1Magic(pc, cha);
				damage = magic.calcWeaponSkillDamage((int) damage, skill.getAttr());
			}
			if (skill.getArea() != 0) { // 範囲攻撃
				int castgfx2 = skill.getCastGfx2();
				L1Character radiusTarget = null;
				if (skill.getTarget().equals("none")) {
					radiusTarget = pc;
				} else {
					radiusTarget = cha;
				}
				double dmg = weaponSkill.getFixDamage();
				if (weaponSkill.getRandomDamage() != 0) {
					dmg += _random.nextInt(weaponSkill.getRandomDamage());
				}
				L1Character target;
				for (L1Object obj : L1World.getInstance().getVisibleObjects(radiusTarget, skill.getArea())) {
					if (!(obj instanceof L1Character)) {
						continue;
					}
					target = (L1Character) obj;

					// 攻撃者は対象外
					if (pc.getId() == target.getId()) {
						continue;
					}

					// 攻撃対象がモンスターの場合、範囲攻撃もモンスターのみ有効。
					if (cha instanceof L1MonsterInstance) {
						if (!(target instanceof L1MonsterInstance)) {
							continue;
						}
					}

					// 攻撃対象がPC、ペット、サモンの場合、範囲攻撃はPC、ペット、サモン、ガード、モンスターに有効。
					if (cha instanceof L1PcInstance || cha instanceof L1PetInstance ||
															cha instanceof L1SummonInstance) {
						if (!(target instanceof L1PcInstance) && !(target instanceof L1PetInstance) &&
								!(target instanceof L1SummonInstance) && !(target instanceof L1MonsterInstance) &&
								!(target instanceof L1GuardInstance)) {
							continue;
						}
					}

					// 攻撃対象はL1Attackで処理するのでターゲット外
					if (cha.getId() == target.getId()) {
						continue;
					}

					// 範囲対象がPCの場合、ゾーンを判定。
					if ((target instanceof L1PcInstance) && target.getZoneType() == 1) {
						continue;
					}

					// 対象が死んでいる、攻撃無効状態、カウンターマジック発動
					if (target.isDead() || target.isThroughAttack() || target.isCounterMagic()) {
						continue;
					}

					// ダメージを反映
					if (!weaponSkill.isMr()) {
						L1Magic magic = new L1Magic(pc, target);
						dmg = magic.calcWeaponSkillDamage((int) dmg, skill.getAttr());
					}
					if (target instanceof L1PcInstance) {
						((L1PcInstance) target).receiveDamage(pc, (int) dmg, true);
						if (castgfx2 != -1) {
							((L1PcInstance) target).sendPackets(new S_SkillSound(target.getId(), castgfx2));
						}
					} else {
						((L1NpcInstance) target).receiveDamage(pc, (int) dmg);
					}
					if (castgfx2 != -1) {
						target.broadcastPacket(new S_SkillSound(target.getId(), castgfx2));
					}
				}
			}
		} else if (weaponSkill.getChaserCount() > 1) { // チェイサー系魔法
			isChaser = true;
			L1Chaser chaser = new L1Chaser(pc, cha, weaponSkill.getChaserCount(),
									skillId, skill.getCastGfx(), weaponSkill.isHpDrain());
			chaser.start();
		} else {
			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(pc, cha, skillId, weaponSkill.isHpDrain(), false,
					cha.getId(), cha.getX(), cha.getY(), null, (int) skill.getBuffDuration(),
					L1SkillUse.TYPE_WEAPONSKILL);
		}

		if (skill.getType() == L1Skill.TYPE_PROBABILITY || skill.getType() == L1Skill.TYPE_CURSE) {
			if (cha.isThroughAttack()) {
				return 0;
			}
		}

		if (cha.isThroughAttack()) {
			damage = 0;
		}

		// ■■■■■■■■■■■■■■■ 攻撃対象者へのエフェクト送信 ■■■■■■■■■■■■■■■
		if (skill.getCastGfx() != -1 && !isChaser) { // 表示するグラがあり、チェイサー以外の場合
			int chaId = 0;
			if (skill.getTarget().equalsIgnoreCase("none")) {
				chaId = pc.getId();
			} else {
				chaId = cha.getId();
			}
			int isArrowType = weaponSkill.getArrowType();
			if (isArrowType == 0) { // 通常の単発魔法
				pc.sendPackets(new S_SkillSound(chaId, skill.getCastGfx()));
				pc.broadcastPacket(new S_SkillSound(chaId, skill.getCastGfx()));
			} else if (isArrowType == 1) { // ルナロングなどの弓系魔法
				S_UseAttackSkill packet = new S_UseAttackSkill(pc, cha.getId(),
						skill.getCastGfx(), cha.getX(), cha.getY(),
						ActionCodes.ACTION_Attack, false);
				pc.sendPackets(packet);
				pc.broadcastPacket(packet);
			} else if (isArrowType == 2) { // イラプション等の特殊魔法
				S_EffectLocation packet = new S_EffectLocation(cha.getX(), cha.getY(), skill.getCastGfx());
				pc.sendPackets(packet);
				pc.broadcastPacket(packet);
			} else if (isArrowType == 3) { // 通常の単発魔法(自分が起点になるエフェクト)
				pc.sendPackets(new S_SkillSound(pc.getId(), skill.getCastGfx()));
				pc.broadcastPacket(new S_SkillSound(pc.getId(), skill.getCastGfx()));
			}
		}
		return damage;
	}

	public static double getDiceDaggerDamage(L1PcInstance pc,
			L1PcInstance targetPc, L1ItemInstance weapon) {
		double dmg = 0;
		int chance = _random.nextInt(100) + 1;
		if (3 >= chance) {
			dmg = targetPc.getCurrentHp() * 2 / 3;
			if (targetPc.getCurrentHp() - dmg < 0) {
				dmg = 0;
			}
			String msg = weapon.getLogName();
			pc.sendPackets(new S_ServerMessage(158, msg));
			// \f1%0が蒸発してなくなりました。
			pc.getInventory().removeItem(weapon, 1);
		}
		return dmg;
	}

	public static void giveFettersEffect(L1PcInstance pc, L1Character cha) {
		int fettersTime = 8000;
		if (cha.isThroughAttack()) { // 凍結状態orカウンターマジック中
			return;
		}
		if ((_random.nextInt(100) + 1) <= 2) {
			L1EffectSpawn.getInstance().spawnEffect(81182, fettersTime,
					cha.getX(), cha.getY(), cha.getMapId());
			if (cha instanceof L1PcInstance) {
				L1PcInstance targetPc = (L1PcInstance) cha;
				targetPc.setSkillEffect(THUNDER_GRAB, fettersTime);
				targetPc.sendPackets(new S_SkillSound(targetPc.getId(), 4184));
				targetPc.broadcastPacket(new S_SkillSound(targetPc.getId(), 4184));
				targetPc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND, true));
			} else if (cha instanceof L1MonsterInstance
					|| cha instanceof L1SummonInstance
					|| cha instanceof L1PetInstance) {
				L1NpcInstance npc = (L1NpcInstance) cha;
				npc.setSkillEffect(THUNDER_GRAB, fettersTime);
				npc.broadcastPacket(new S_SkillSound(npc.getId(), 4184));
			}
		}
	}

	public static double calcDamageReduction(L1PcInstance pc, L1Character cha,
			double dmg, int attr) {
		// 凍結状態orカウンターマジック中
		if (cha.isThroughAttack()) {
			return 0;
		}

		// MRによるダメージ軽減
		int mr = cha.getMr();
		double mrFloor = 0;
		if (mr <= 100) {
			mrFloor = Math.floor((mr - pc.getOriginalMagicHit()) / 2);
		} else if (mr >= 100) {
			mrFloor = Math.floor((mr - pc.getOriginalMagicHit()) / 10);
		}
		double mrCoefficient = 0;
		if (mr <= 100) {
			mrCoefficient = 1 - 0.01 * mrFloor;
		} else if (mr >= 100) {
			mrCoefficient = 0.6 - 0.01 * mrFloor;
		}
		dmg *= mrCoefficient;

		if (cha.hasSkillEffect(ERASE_MAGIC)) {
			cha.removeSkillEffect(ERASE_MAGIC);
			cha.killSkillEffectTimer(ERASE_MAGIC);
		}

		// 属性によるダメージ軽減
		int resist = 0;
		if (attr == L1Skill.ATTR_EARTH) {
			resist = cha.getEarth();
		} else if (attr == L1Skill.ATTR_FIRE) {
			resist = cha.getFire();
		} else if (attr == L1Skill.ATTR_WATER) {
			resist = cha.getWater();
		} else if (attr == L1Skill.ATTR_WIND) {
			resist = cha.getWind();
		}
		int resistFloor = (int) (0.32 * Math.abs(resist));
		if (resist >= 0) {
			resistFloor *= 1;
		} else {
			resistFloor *= -1;
		}
		double attrDeffence = resistFloor / 32.0;
		dmg = (1.0 - attrDeffence) * dmg;

		return dmg;
	}

}
