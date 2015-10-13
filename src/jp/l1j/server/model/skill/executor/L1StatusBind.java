package jp.l1j.server.model.skill.executor;

import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.instance.L1MonsterInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.instance.L1PetInstance;
import jp.l1j.server.model.instance.L1SummonInstance;
import jp.l1j.server.packets.server.S_Paralysis;
import jp.l1j.server.packets.server.S_Poison;

public class L1StatusBind extends L1BuffSkillExecutorImpl {

	@Override
	public void addEffect(L1Character user, L1Character target, int durationSeconds) {
		if (target instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) target;
			pc.sendPackets(new S_Poison(pc.getId(), 2));
			pc.broadcastPacket(new S_Poison(pc.getId(), 2));
			pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_FREEZE, true));
		} else if (target instanceof L1MonsterInstance
				|| target instanceof L1SummonInstance
				|| target instanceof L1PetInstance) {
			L1NpcInstance npc = (L1NpcInstance) target;
			npc.broadcastPacket(new S_Poison(npc.getId(), 2));
			npc.setParalyzed(true);
			npc.setParalysisTime((int) (_skill.getBuffDuration() * 1000.0D));
		}
	}

	@Override
	public void removeEffect(L1Character target) {
		if (target instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) target;
			pc.sendPackets(new S_Poison(pc.getId(), 0));
			pc.broadcastPacket(new S_Poison(pc.getId(), 0));
			pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_FREEZE, false));
		} else if (target instanceof L1MonsterInstance
				|| target instanceof L1SummonInstance
				|| target instanceof L1PetInstance) {
			L1NpcInstance npc = (L1NpcInstance) target;
			npc.broadcastPacket(new S_Poison(npc.getId(), 0));
			npc.setParalyzed(false);
		}
	}
}
