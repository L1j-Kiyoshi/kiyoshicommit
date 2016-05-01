package jp.l1j.server.model.skill.executor;

import static jp.l1j.server.model.skill.L1SkillId.*;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.L1EffectSpawn;
import jp.l1j.server.model.instance.L1MonsterInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.instance.L1PetInstance;
import jp.l1j.server.model.instance.L1SummonInstance;
import jp.l1j.server.packets.server.S_Paralysis;

public class L1StatusStun extends L1BuffSkillExecutorImpl {

	@Override
	public void addEffect(L1Character user, L1Character target, int durationSeconds) {
		if (_skill.getSkillId() == BONE_BREAK) {
			L1EffectSpawn.getInstance().spawnEffect(91208, durationSeconds, target.getX(), target.getY(), target.getMapId());
		} else {
			L1EffectSpawn.getInstance().spawnEffect(81162, durationSeconds, target.getX(), target.getY(), target.getMapId());
		}
		if (target instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) target;
			pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_STUN, true));
		} else if (target instanceof L1MonsterInstance
				|| target instanceof L1SummonInstance
				|| target instanceof L1PetInstance) {
			L1NpcInstance npc = (L1NpcInstance) target;
			npc.setParalyzed(true);
			npc.setParalysisTime(durationSeconds);
		}
		target.setSkillEffect(SHOCK_STUN, durationSeconds);
	}

	@Override
	public void removeEffect(L1Character target) {
		if (target instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) target;
			pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_STUN, false));
		} else if (target instanceof L1MonsterInstance
				|| target instanceof L1SummonInstance
				|| target instanceof L1PetInstance) {
			L1NpcInstance npc = (L1NpcInstance) target;
			npc.setParalyzed(false);
		}
	}
}
