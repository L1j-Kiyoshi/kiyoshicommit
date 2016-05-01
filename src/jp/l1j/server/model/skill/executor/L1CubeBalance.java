package jp.l1j.server.model.skill.executor;

import static jp.l1j.server.model.skill.L1SkillId.*;

import java.util.ArrayList;

import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1EffectInstance;
import jp.l1j.server.model.instance.L1MonsterInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.templates.L1MagicDoll;

public class L1CubeBalance implements Runnable {

	private L1PcInstance _user = null;
	private L1EffectInstance _cube = null;

	private ArrayList<L1Character> _targetList = new ArrayList<L1Character>();

	public L1CubeBalance(L1PcInstance user, L1EffectInstance effect) {
		_user = user;
		_cube = effect;
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < 5; i++) {
				Thread.sleep(4000);
				setTargetList();
				addMagic();
			}
		} catch (Exception e) {
		}
	}

	private void addMagic() {
		L1PcInstance pc;
		int hpDown = 25;
		for (L1Character cha : _targetList) {
			if (cha.hasSkillEffect(IMMUNE_TO_HARM)) {
				hpDown /= 2;
			}
			int newMp = cha.getCurrentMp() + 5;
			if (newMp < 0) {
				newMp = 0;
			}
			cha.setCurrentMp(newMp);

			if (cha instanceof L1PcInstance) {
				pc = (L1PcInstance) cha;
				if (!(L1MagicDoll.getDamageEvasionByDoll(pc) > 0)) {
					pc.receiveDamage(_user, hpDown, false);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6727));
				}
			} else if (cha instanceof L1MonsterInstance) {
				L1MonsterInstance mob = (L1MonsterInstance) cha;
				mob.receiveDamage(_user, hpDown);
			}
			cha.broadcastPacket(new S_SkillSound(cha.getId(), 6727));
		}
	}

	private void setTargetList() {
		L1Character cha;
		L1PcInstance targetPc;
		for (L1Object obj : L1World.getInstance().getVisibleObjects(_cube, 3)) {
			if (!(obj instanceof L1PcInstance) && !(obj instanceof L1MonsterInstance)) {
				continue;
			}
			cha = (L1Character) obj;

			if (cha.isThroughAttack()) {
				continue;
			}
			if (cha.isDead()) {
				continue;
			}
			if (cha instanceof L1PcInstance) {
				targetPc = (L1PcInstance) cha;
				if (targetPc.getZoneType() == 1) { // ターゲットがセーフティゾーン
					if (!_user.isInParty()) { // 術者がパーティを組んでいない
						continue;
					}
					if (!targetPc.isInParty()) { // 対象がパーティを組んでいない
						continue;
					}
					if (!_user.getParty().isMember(targetPc)) { // 同じパーティではない
						continue;
					}
				}
			}
			_targetList.add(cha);
		}
	}
}
