package jp.l1j.server.model.skill.executor;

import static jp.l1j.server.model.skill.L1SkillId.*;
import jp.l1j.server.codes.ActionCodes;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.L1Magic;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_AttackMissPacket;
import jp.l1j.server.packets.server.S_DoActionGFX;
import jp.l1j.server.packets.server.S_SkillSound;

public class L1CounterBarrier {

	private L1Character _user = null;
	private L1Character _target = null;

	/**
	 * @param user バリア術者
	 * @param target バリア攻撃者
	 */
	public L1CounterBarrier(L1Character user, L1Character target) {
		_user = user;
		_target = target;
	}

	/**
	 * カウンターバリアの発動条件を満たすか。
	 * 測定はtargetの武器・発動確率。
	 * @return 満たすならtrue
	 */
	public boolean isCounterBarrier() {
		if (!_user.hasSkillEffect(COUNTER_BARRIER)) {
			return false;
		}
		if (_target instanceof L1PcInstance) { // 攻撃してきたのがPCの場合
			L1PcInstance target = (L1PcInstance) _target;
			if (target.getWeapon() != null) {
				int weaponType = target.getWeapon().getItem().getType1();
				int weaponType2 = target.getWeapon().getItem().getType();
				if (weaponType == 20 || weaponType == 62 || weaponType2 == 14) { // 弓・ガント・キリンク
					return false;
				}
			}
		} else if (_target instanceof L1NpcInstance) { // NPCが攻撃してきた場合
			L1NpcInstance target = (L1NpcInstance) _target;
			int bowId = target.getNpcTemplate().getBowActId();
			if (bowId > 0) { // 遠距離攻撃
				return false;
			}
		}

		L1Magic magic = new L1Magic(_user, _target);
		if (!magic.calcProbabilityMagic(COUNTER_BARRIER)){
			return false;
		}
		return true;
	}

	/**
	 * カウンターバリアの術者がNPCの場合の発動条件。
	 * @return
	 */
	public boolean isCounterBarrierNpc() {
		if (!_user.hasSkillEffect(KNIGHTVALD_COUNTER_BARRIER)) {
			return false;
		}
		L1Magic magic = new L1Magic(_user, _target);
		if (!magic.calcProbabilityMagic(KNIGHTVALD_COUNTER_BARRIER)) {
			return false;
		}
		return true;
	}

	/**
	 * カウンターバリアの効果を反映する。
	 * グラフィック送信等すべて含まれる。
	 */
	public void commitCounterBarrier() {
		int damage = calcCounterBarrierDamage();

		if (_target instanceof L1PcInstance) {
			((L1PcInstance) _target).receiveDamage(_user, damage, false);
		} else if (_target instanceof L1NpcInstance) {
			((L1NpcInstance) _target).receiveDamage(_user, damage);
		}
		sendCounterBarrierAction();
	}

	/**
	 * カウンターバリア発動時のモーションを送信。
	 */
	private void sendCounterBarrierAction() {

		if (_target instanceof L1PcInstance) {
			L1PcInstance target = (L1PcInstance) _target;
			target.sendPackets(new S_AttackMissPacket(target, _user.getId()));
			target.sendPackets(new S_DoActionGFX(target.getId(), ActionCodes.ACTION_Damage));
		}
		if (_user instanceof L1PcInstance) {
			((L1PcInstance) _user).sendPackets(new S_SkillSound(_user.getId(), 10710));
		}
		_target.setHeading(_target.targetDirection(_user.getX(), _user.getY())); // 向きのセット
		_target.broadcastPacket(new S_AttackMissPacket(_target, _user.getId()));
		_target.broadcastPacket(new S_DoActionGFX(_target.getId(), ActionCodes.ACTION_Damage));
		_user.broadcastPacket(new S_SkillSound(_user.getId(), 10710));
	}

	private int calcCounterBarrierDamage() {
		int damage = 0;
		if (_user instanceof L1PcInstance) {
			L1ItemInstance weapon = ((L1PcInstance) _user).getWeapon();
			if (weapon != null) {
				if (weapon.getItem().getType() == 2) { // 両手剣(BIG最大ダメージ+強化数+追加ダメージ)*2
					damage = (weapon.getItem().getDmgLarge()
							+ weapon.getEnchantLevel() + weapon.getItem().getDmgModifier()) * 2;
				}
			}
		} else if (_user instanceof L1NpcInstance) {
			L1NpcInstance npc = (L1NpcInstance) _user;
			damage = npc.getNpcTemplate().getLevel() * 2 + npc.getStr();
		}
		if (_target.hasSkillEffect(IMMUNE_TO_HARM)) {
			damage /= 2;
		}
		return damage;
	}
}
