package jp.l1j.server.model.skill.executor;

import static jp.l1j.server.model.skill.L1SkillId.*;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.instance.L1MonsterInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.instance.L1PetInstance;
import jp.l1j.server.model.instance.L1SummonInstance;
import jp.l1j.server.packets.server.S_OwnCharStatus;
import jp.l1j.server.packets.server.S_Paralysis;

public class L1StatusSleep extends L1BuffSkillExecutorImpl {

	@Override
	public void addEffect(L1Character user, L1Character target, int durationSeconds) {
		int time = (int) (_skill.getBuffDuration() * 1000.0D);
		if (target instanceof L1PcInstance) {
			L1PcInstance targetPc = (L1PcInstance) target;
			targetPc.setSkillEffect(FOG_OF_SLEEPING, time);
			targetPc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_SLEEP, true));
		} else if (target instanceof L1MonsterInstance
				|| target instanceof L1SummonInstance
				|| target instanceof L1PetInstance) {
			L1NpcInstance npc = (L1NpcInstance) target;
			npc.setSkillEffect(FOG_OF_SLEEPING, time);
			npc.setSleeped(true);
		}
	}

	@Override
	public void removeEffect(L1Character target) {
		target.setSleeped(false);
		if (target instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) target;
			pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_SLEEP, false));
			pc.sendPackets(new S_OwnCharStatus(pc));
		}
	}
}
