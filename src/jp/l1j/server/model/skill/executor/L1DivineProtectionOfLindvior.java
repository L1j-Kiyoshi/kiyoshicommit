package jp.l1j.server.model.skill.executor;

import jp.l1j.server.codes.ActionCodes;
import jp.l1j.server.model.L1Attack;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_AttackMissPacket;
import jp.l1j.server.packets.server.S_DoActionGFX;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.packets.server.S_UseAttackSkill;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import static jp.l1j.server.model.skill.L1SkillId.*;

public class L1DivineProtectionOfLindvior {

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private L1Character _user = null;
	private L1Character _target = null;
	private L1Attack _attack = null;

	/**
	 * @param user 装備者
	 * @param target 攻撃者
	 * @param L1Attack
	 * 			attackerがリンドビオル加護への攻撃者、targetがリンドビオル加護発動者<br>
	 * 			を格納しているL1Attackクラス。
	 */
	public L1DivineProtectionOfLindvior(L1Character user, L1Character target, L1Attack attack) {
		_user = user;
		_target = target;
		_attack = attack;
	}


	/**
	 * リンドビオルの加護の発動条件を満たすか。
	 * 測定はtargetの武器・発動確率。
	 * @return 満たすならtrue
	 */
	public boolean isDivineProtectionOfLindovior() {
		// 加護を受けているか。
		if (!_user.hasSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR)) {
			return false;
		}
		if (_target instanceof L1PcInstance) { // 攻撃してきたのがPCの場合
			L1PcInstance target = (L1PcInstance) _target;
			if (target.getWeapon() != null) {
				int weaponType = target.getWeapon().getItem().getType1();
				if (weaponType != 20 && weaponType != 62) { // 弓・ガント以外
					return false;
				}
			}
		} else if (_target instanceof L1NpcInstance) { // NPCが攻撃してきた場合
			L1NpcInstance target = (L1NpcInstance) _target;
			int bowId = target.getNpcTemplate().getBowActId();
			if (bowId == 0) { // 近接攻撃
				return false;
			}
		}

		if ((_random.nextInt(1000) + 1) > 100) { // 発動率判定
			return false;
		}

		return true;
	}

	/**
	 * リンドビオルの加護の効果を反映する。
	 * グラフィック送信等すべて含まれる。
	 */
	public void commitDivineProtectionOfLindovior() {
		int dmg = _attack.calcDamage();
		if (_target instanceof L1PcInstance) {
			((L1PcInstance) _target).receiveDamage(_user, (dmg * 2), false);
		} else if (_target instanceof L1NpcInstance) {
			((L1NpcInstance) _target).receiveDamage(_user, (int) (dmg * 2));
		}
		sendDivineProtectionOfLindoviorAction();
	}

	/**
	 * リンドビオルの加護発動時のモーションを送信。
	 */
	private void sendDivineProtectionOfLindoviorAction() {

		if (_user instanceof L1PcInstance) {
			((L1PcInstance) _user).sendPackets(new S_SkillSound(_user.getId(), 10419));
		}
		if (_target instanceof L1PcInstance) {
			L1PcInstance target = (L1PcInstance) _target;
			target.sendPackets(new S_AttackMissPacket(target, _user.getId()));
			target.sendPackets(new S_DoActionGFX(target.getId(), ActionCodes.ACTION_Damage));
		}
		if (_target instanceof L1NpcInstance) {
			L1NpcInstance target = (L1NpcInstance) _target;
				int actId = ActionCodes.ACTION_BowAttack;
				if (target.getNpcTemplate().getBowActId() > 0) {
					target.broadcastPacket(new S_UseAttackSkill(_target, _user.getId(),
							target.getNpcTemplate().getBowActId(), _user.getX(), _user.getY(), actId, 0));
				} else {
					target.broadcastPacket(new S_AttackMissPacket(_target, _user.getId(), actId));
				}
		}
		_target.setHeading(_target.targetDirection(_user.getX(), _user.getY())); // 向きのセット
		_target.broadcastPacket(new S_AttackMissPacket(_target, _user.getId()));
		_target.broadcastPacket(new S_DoActionGFX(_target.getId(), ActionCodes.ACTION_Damage));
		_user.broadcastPacket(new S_SkillSound(_user.getId(), 10419));
	}

}
