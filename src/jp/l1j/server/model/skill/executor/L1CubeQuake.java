package jp.l1j.server.model.skill.executor;

import java.util.ArrayList;

import jp.l1j.configure.Config;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1EffectInstance;
import jp.l1j.server.model.instance.L1MonsterInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_OwnCharAttrDef;
import jp.l1j.server.packets.server.S_Paralysis;
import jp.l1j.server.packets.server.S_SkillSound;

import static jp.l1j.server.model.skill.L1SkillId.*;

public class L1CubeQuake implements Runnable {

	private L1PcInstance _user = null;
	private L1EffectInstance _cube = null;

	private ArrayList<L1Character> _enemyList = new ArrayList<L1Character>();
	private ArrayList<L1Character> _allyList = new ArrayList<L1Character>();

	public L1CubeQuake(L1PcInstance user, L1EffectInstance effect) {
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

		for (L1Character cha : _enemyList) {
			if (cha instanceof L1PcInstance) {
				L1PcInstance pc = (L1PcInstance) cha;
				pc.setSkillEffect(THUNDER_GRAB, 1000);
				pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND, true));
			} else if (cha instanceof L1MonsterInstance) {
				L1MonsterInstance mob = (L1MonsterInstance) cha;
				mob.setSkillEffect(THUNDER_GRAB, 1000);
				mob.setParalyzed(true);
			}
		}
		for (L1Character cha : _allyList) {
			if (cha.hasSkillEffect(CUBE_QUAKE_ALLY)) {
				cha.removeSkillEffect(CUBE_QUAKE_ALLY);
			}
			cha.addEarth(30);
			if (cha instanceof L1PcInstance) {
				L1PcInstance pc = (L1PcInstance) cha;
				pc.sendPackets(new S_OwnCharAttrDef(pc));
				pc.sendPackets(new S_SkillSound(pc.getId(), 6714));
			}
			cha.broadcastPacket(new S_SkillSound(cha.getId(), 6714));
			cha.setSkillEffect(CUBE_QUAKE_ALLY, 8000);
		}
	}

	private void setTargetList() {
		L1PcInstance targetPc;
		for (L1Object obj : L1World.getInstance().getVisibleObjects(_cube, 3)) {
			if (obj instanceof L1PcInstance) {
				targetPc = (L1PcInstance) obj;
				if (_user.getId() == targetPc.getId()) { // 術者
					_allyList.add(targetPc);
					continue;
				}
				if (_user.isInParty() && _user.getParty().isMember(targetPc)) {
					_allyList.add(targetPc);
					continue;
				} else if (_user.getClanId() != 0 && _user.getClanId() == targetPc.getClanId()) {
					_allyList.add(targetPc);
					continue;
				} else if (targetPc.getZoneType() == 1) {
					continue;
				} else if (!Config.ALT_NONPVP) { // None-PvP
					if (targetPc.getZoneType() == 0) {
						continue;
					}
				}
				if (targetPc.isThroughAttack()) {
					continue;
				}
				if (targetPc.isDead()) {
					continue;
				}
				_enemyList.add(targetPc);
			} else if (obj instanceof L1MonsterInstance) {
				if (!((L1Character) obj).isThroughAttack()) {
					_enemyList.add((L1Character) obj);
				}
			}
		}
	}
}
