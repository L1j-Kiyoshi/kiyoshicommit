package jp.l1j.server.model.skill.executor;

import jp.l1j.server.datatables.SkillTable;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1Skill;
import static jp.l1j.server.model.skill.L1SkillId.*;

public class L1EvilTrick implements Runnable {

	private L1Character _user;
	private L1Character _target;
	L1Skill _skill = SkillTable.getInstance().findBySkillId(EVIL_TRICK);
	RandomGenerator _random = RandomGeneratorFactory.getSharedRandom();

	public L1EvilTrick(L1Character user, L1Character target) {
		_user = user;
		_target = target;
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
				commitTrick();
				Thread.sleep(1000);
			}
		} catch (Exception e) {
		} finally {
			_target.removeSkillEffect(CHASER);
		}
	}

	private void commitTrick() {
		int dmg = 0;
		for (int diceCount = _skill.getDamageDiceCount(); diceCount > 0; diceCount--) {
			dmg += _random.nextInt(_skill.getDamageDice()) + 1;
		}
		if (dmg <= 0) {
			dmg = 1;
		}
		if (_target instanceof L1PcInstance) {
			((L1PcInstance) _target).receiveManaDamage(_user, dmg);
		} else if (_target instanceof L1NpcInstance) {
			((L1NpcInstance) _target).ReceiveManaDamage(_user, dmg);
		}
		_user.setCurrentMp(_user.getCurrentMp() + dmg);
	}

	private void sendGfx() {
		if (_target instanceof L1PcInstance) {
			((L1PcInstance) _target).sendPackets(new S_SkillSound(_target.getId(), 8152));
		}
		_target.broadcastPacket(new S_SkillSound(_target.getId(), 8152));
	}
}
