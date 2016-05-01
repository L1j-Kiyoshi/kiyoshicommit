package jp.l1j.server.model.skill.executor;

import static jp.l1j.server.model.skill.L1SkillId.*;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.L1Magic;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_SkillSound;

public class L1EvilReverse implements Runnable {

	private L1Character _user;
	private L1Character _target;
	private L1Magic _magic;

	public L1EvilReverse(L1Character user, L1Character target) {
		_user = user;
		_target = target;
		_magic = new L1Magic(user, target);
	}

	@Override
	public void run() {
		try {
			if (_target.hasSkillEffect(CHASER)) {
				return;
			}
			_target.setSkillEffect(CHASER, 0);
			Thread.sleep(1000);
			sendGfx();
			for (int i = 0; i < 3; i++) {
				commitReverse();
				Thread.sleep(1000);
			}
		} catch (Exception e) {
		} finally {
			_target.removeSkillEffect(CHASER);
		}
	}

	private void commitReverse() {
		int dmg = _magic.calcMagicDamage(EVIL_REVERSE);
		if (dmg <= 0) {
			dmg = 1;
		}
		if (_target instanceof L1PcInstance) {
			((L1PcInstance) _target).receiveDamage(_user, dmg, true);
		} else if (_target instanceof L1NpcInstance) {
			((L1NpcInstance) _target).receiveDamage(_user, dmg);
		}
		_user.setCurrentHp(_user.getCurrentHp() + dmg);
	}

	private void sendGfx() {
		if (_target instanceof L1PcInstance) {
			((L1PcInstance) _target).sendPackets(new S_SkillSound(_target.getId(), 8150));
		}
		_target.broadcastPacket(new S_SkillSound(_target.getId(), 8150));
	}
}
