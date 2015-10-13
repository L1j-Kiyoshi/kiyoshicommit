package jp.l1j.server.model.skill.uniqueskills.antharas;

import jp.l1j.server.datatables.SprTable;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.L1Location;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.skill.uniqueskills.L1UniqueSkillUtils;
import jp.l1j.server.packets.server.S_DoActionGFX;
import jp.l1j.server.packets.server.S_NpcChatPacket;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.utils.L1SpawnUtil;

public class L1Thisetol extends L1UniqueSkillUtils implements Runnable {

	private L1Character _user;

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	public L1Thisetol(L1Character user) {
		_user = user;
	}

	public void run() {
		try {
			_user.broadcastPacket(new S_NpcChatPacket((L1NpcInstance) _user, "$7948", 0));
			Thread.sleep(calcSleepTime(_user, 1000, MAGIC_SPEED));
			L1Location baseLocation = new L1Location(_user.getLocation());
			baseLocation.setX(_user.getLocation().getX() - 12);
			baseLocation.setY(_user.getLocation().getY() - 12);
			int x;
			int y;
			for (L1PcInstance target : L1World.getInstance().getVisiblePlayer(_user, -1)) {
				x = baseLocation.getX() + _random.nextInt(13) + 1;
				y = baseLocation.getY() + _random.nextInt(13) + 1;
				L1Teleport.teleport(target, x, y, target.getMapId(), target.getHeading(), true);
			}
			Thread.sleep(calcSleepTime(_user, 1000, MAGIC_SPEED));
			summonMonsters(91157, 3);
			_user.broadcastPacket(new S_SkillSound(_user.getId(), 761)); // 魔方陣の表示
			_user.broadcastPacket(new S_DoActionGFX(_user.getId(), 41));
			int sleepTime = SprTable.getInstance().getActionList(_user.getGfxId()).get(41);
			Thread.sleep(calcSleepTime(_user, sleepTime, MAGIC_SPEED)); // 1920
			Thread.sleep(80);
		} catch (Exception e) {

		}
	}

	private void summonMonsters(int summonId, int count) {
		for (int i = 0; i < count; i++) {
			L1SpawnUtil.summonMonster(_user, summonId);
		}
	}
}
