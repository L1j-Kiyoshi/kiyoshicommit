package jp.l1j.server.model.skill.uniqueskills.antharas;

import jp.l1j.server.datatables.SprTable;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.poison.L1ParalysisPoison;
import jp.l1j.server.model.skill.L1SkillUse;
import jp.l1j.server.model.skill.uniqueskills.L1UniqueSkillUtils;
import jp.l1j.server.packets.server.S_DoActionGFX;
import jp.l1j.server.packets.server.S_NpcChatPacket;

public class L1Ruota extends L1UniqueSkillUtils implements Runnable {

	private L1Character _user;
	private L1Character _target;

	public L1Ruota(L1Character user, L1Character target) {
		_user = user;
		_target = target;
	}

	public void run() {
		try {
			// オーブモーク！ルオタ
			int sleepTime = 0;
			_user.broadcastPacket(new S_NpcChatPacket((L1NpcInstance) _user, "$7863", 0));
			Thread.sleep(calcSleepTime(_user, 500, MAGIC_SPEED));
			// 毒ブレス（ダメージ）
			new L1SkillUse().handleCommands(null, 1037, _target.getId(), _target
					.getX(), _target.getX(), null, 0, L1SkillUse.TYPE_NORMAL, _user);
			sleepTime = SprTable.getInstance().getActionList(_user.getGfxId()).get(18);
			Thread.sleep(calcSleepTime(_user, sleepTime, MAGIC_SPEED)); // 2880
			// 毒ブレス（麻痺）
			for (L1Object obj : L1World.getInstance().getVisibleBoxObjects(_user, _user.getHeading(), 5, 5)) {
				if (!(obj instanceof L1PcInstance)) {
					continue;
				}
				// ４秒後に１２秒麻痺（適当）
				L1ParalysisPoison.doInfection((L1Character) obj, 4000, 12000);
			}
			_user.broadcastPacket(new S_DoActionGFX(_user.getId(), 18));
			sleepTime = SprTable.getInstance().getActionList(_user.getGfxId()).get(18);
			Thread.sleep(calcSleepTime(_user, sleepTime, MAGIC_SPEED)); // 2880
			// 予備
			Thread.sleep(240);
		} catch (NullPointerException npe) {
			return;
		} catch (Exception e) {

		}
	}
}
