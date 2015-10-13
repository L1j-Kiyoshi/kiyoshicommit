package jp.l1j.server.model.skill.executor;

import static jp.l1j.server.model.skill.L1SkillId.THUNDER_GRAB;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.L1EffectSpawn;
import jp.l1j.server.model.instance.L1MonsterInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.instance.L1PetInstance;
import jp.l1j.server.model.instance.L1SummonInstance;
import jp.l1j.server.packets.server.S_Paralysis;
import jp.l1j.server.packets.server.S_SkillSound;

public class L1StatusHold extends L1BuffSkillExecutorImpl {

	@Override
	public void addEffect(L1Character user, L1Character target, int durationSeconds) {
		L1EffectSpawn.getInstance().spawnEffect(81182, durationSeconds, target.getX(), target.getY(), target.getMapId());
		if (target instanceof L1PcInstance) {
			L1PcInstance targetPc = (L1PcInstance) target;
			targetPc.setSkillEffect(THUNDER_GRAB, durationSeconds);
			targetPc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND, true));
			targetPc.broadcastPacket(new S_SkillSound(targetPc.getId(), 4184));
		} else if (target instanceof L1MonsterInstance
				|| target instanceof L1SummonInstance
				|| target instanceof L1PetInstance) {
			L1NpcInstance npc = (L1NpcInstance) target;
			npc.setSkillEffect(THUNDER_GRAB, durationSeconds);
			npc.broadcastPacket(new S_SkillSound(npc.getId(), 4184));
		}
	}

	@Override
	public void removeEffect(L1Character target) {
		if (target instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) target;
			pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND, false));
		} else if (target instanceof L1MonsterInstance
				|| target instanceof L1SummonInstance
				|| target instanceof L1PetInstance) {
			L1NpcInstance npc = (L1NpcInstance) target;
			npc.setParalyzed(false);
		}
	}
}
