package jp.l1j.server.model.skill.uniqueskills.antharas;

import jp.l1j.server.datatables.PhysicalSkillsTable;
import jp.l1j.server.datatables.SprTable;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.skill.L1SkillUse;
import jp.l1j.server.model.skill.uniqueskills.L1NpcSkillExecutor;
import jp.l1j.server.model.skill.uniqueskills.L1UniqueSkillUtils;
import jp.l1j.server.packets.server.S_NpcChatPacket;
import jp.l1j.server.templates.L1PhysicalAttack;

public class L1KenesiS extends L1UniqueSkillUtils implements Runnable {

	private L1Character _user;
	private L1Character _target;

	public L1KenesiS(L1Character user, L1Character target) {
		_user = user;
		_target = target;
	}

	public void run() {
		try {
			// オーブモーク！ケネシ…
			int sleepTime = 0;
			_user.broadcastPacket(new S_NpcChatPacket((L1NpcInstance) _user, "$7861", 0));
			sleepTime = calcSleepTime(_user, 500, MAGIC_SPEED);
			Thread.sleep(sleepTime);
			// 単体キャンセレーション
			new L1SkillUse().handleCommands(null, 1005, _target.getId(), _target
					.getX(), _target.getX(), null, 0, L1SkillUse.TYPE_NORMAL, _user);
			sleepTime = SprTable.getInstance().getActionList(_user.getGfxId()).get(1);
			Thread.sleep(calcSleepTime(_user, sleepTime, MAGIC_SPEED)); // 2000
			// 右爪
			L1PhysicalAttack rightClaw = PhysicalSkillsTable.getInstance().findBySkillId(1000);
			new L1NpcSkillExecutor(_user, _target, rightClaw).usePhysicalAttack();
			sleepTime = SprTable.getInstance().getActionList(_user.getGfxId()).get(rightClaw.getActionId());
			Thread.sleep(calcSleepTime(_user, sleepTime, ATTACK_SPEED)); // 1320
			// 毒ブレス
			new L1SkillUse().handleCommands(null, 1037, _target.getId(), _target
					.getX(), _target.getX(), null, 0, L1SkillUse.TYPE_NORMAL, _user);
			sleepTime = SprTable.getInstance().getActionList(_user.getGfxId()).get(18);
			Thread.sleep(calcSleepTime(_user, sleepTime, MAGIC_SPEED)); // 2880
			// 左爪
			L1PhysicalAttack leftClaw = PhysicalSkillsTable.getInstance().findBySkillId(998);
			new L1NpcSkillExecutor(_user, _target, leftClaw).usePhysicalAttack();
			sleepTime = SprTable.getInstance().getActionList(_user.getGfxId()).get(leftClaw.getActionId());
			Thread.sleep(calcSleepTime(_user, sleepTime, ATTACK_SPEED)); // 1320
			// 尻尾
			L1PhysicalAttack tail = PhysicalSkillsTable.getInstance().findBySkillId(1004);
			new L1NpcSkillExecutor(_user, _target, tail).usePhysicalAttack();
			sleepTime = SprTable.getInstance().getActionList(_user.getGfxId()).get(tail.getActionId());
			Thread.sleep(calcSleepTime(_user, sleepTime, ATTACK_SPEED)); // 1880
			Thread.sleep(100); // 予備

		} catch (NullPointerException npe) {
			return;
		} catch (Exception e) {

		}
	}
}
