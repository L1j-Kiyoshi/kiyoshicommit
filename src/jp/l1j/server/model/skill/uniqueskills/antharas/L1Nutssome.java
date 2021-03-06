package jp.l1j.server.model.skill.uniqueskills.antharas;

import jp.l1j.server.datatables.SprTable;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.skill.L1SkillUse;
import jp.l1j.server.model.skill.uniqueskills.L1UniqueSkillUtils;
import jp.l1j.server.packets.server.S_NpcChatPacket;

public class L1Nutssome extends L1UniqueSkillUtils implements Runnable {

	private L1Character _user;
	private L1Character _target;

	public L1Nutssome(L1Character user, L1Character target) {
		_user = user;
		_target = target;
	}

	public void run() {
		try {
			// オーブモーク！ナッツサム…
			int sleepTime = 0;
			_user.broadcastPacket(new S_NpcChatPacket((L1NpcInstance) _user, "$7909", 0));
			sleepTime = calcSleepTime(_user, 500, MAGIC_SPEED);
			Thread.sleep(sleepTime);
			// 範囲キャンセレーション
			new L1SkillUse().handleCommands(null, 1042, _target.getId(), _target
					.getX(), _target.getX(), null, 0, L1SkillUse.TYPE_NORMAL, _user);
			Thread.sleep(calcSleepTime(_user, 2000, MAGIC_SPEED)); // 2000

			// 広範囲スタン
			new L1SkillUse().handleCommands(null, 1045, _target.getId(), _target
					.getX(), _target.getX(), null, 0, L1SkillUse.TYPE_NORMAL, _user);
			Thread.sleep(calcSleepTime(_user, 2500, MAGIC_SPEED)); // 2500

			// ダークメテオ（狭）
			new L1SkillUse().handleCommands(null, 1047, _target.getId(), _target
					.getX(), _target.getX(), null, 0, L1SkillUse.TYPE_NORMAL, _user);
			sleepTime = SprTable.getInstance().getActionList(_user.getGfxId()).get(30);
			Thread.sleep(calcSleepTime(_user, sleepTime, MAGIC_SPEED)); // 3360
			Thread.sleep(140); // 予備

		} catch (NullPointerException npe) {
			return;
		} catch (Exception e) {

		}
	}
}
