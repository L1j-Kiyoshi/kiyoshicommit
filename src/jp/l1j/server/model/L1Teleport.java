/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.l1j.server.model;

import java.util.logging.Logger;

import jp.l1j.configure.Config;
import jp.l1j.server.datatables.MapTable;
import jp.l1j.server.datatables.MapTimerTable;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.map.L1Map;
import jp.l1j.server.packets.server.S_Paralysis;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.packets.server.S_Teleport;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.utils.Teleportation;

public class L1Teleport {

	private static Logger _log = Logger.getLogger(L1Teleport.class.getName());

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	// テレポートスキルの種類
	public static final int TELEPORT = 0;

	public static final int CHANGE_POSITION = 1;

	public static final int ADVANCED_MASS_TELEPORT = 2;

	public static final int CALL_CLAN = 3;

	// 順番にteleport(白), change position e(青), ad mass teleport e(赤), call clan(緑)
	public static final int[] EFFECT_SPR = { 169, 2235, 2236, 2281 };

	public static final int[] EFFECT_TIME = { 280, 440, 440, 1120 };

	private L1Teleport() {
	}

	public static boolean teleport(L1PcInstance pc, L1Location loc, int head,
			boolean effectable) {
		return teleport(pc, loc.getX(), loc.getY(), (short) loc.getMapId(), head,
				effectable, TELEPORT);
	}

	public static boolean teleport(L1PcInstance pc, L1Location loc, int head,
			boolean effectable, int skillType) {
		return teleport(pc, loc.getX(), loc.getY(), (short) loc.getMapId(), head,
				effectable, skillType);
	}

	public static boolean teleport(L1PcInstance pc, int x, int y, short mapid,
			int head, boolean effectable) {
		return teleport(pc, x, y, mapid, head, effectable, TELEPORT);
	}

	public static boolean teleport(L1PcInstance pc, int x, int y, short mapId,
			int head, boolean effectable, int skillType) {

		int areaId = MapTable.getInstance().getAreaId(mapId);
		if (areaId != -1 && pc.getMapTimer().get(areaId) != null) {
			int reminingTime = pc.getMapTimer().get(areaId);
			if (reminingTime <= 0) {
				pc.sendPackets(MapTable.getInstance().getLimitServerMessage(areaId));
				return false;
			}
		}

		pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK, false));

		// エフェクトの表示
		if (effectable && (skillType >= 0 && skillType <= EFFECT_SPR.length)) {
			S_SkillSound packet = new S_SkillSound(pc.getId(),
					EFFECT_SPR[skillType]);
			pc.sendPackets(packet);
			pc.broadcastPacket(packet);

			// テレポート以外のsprはキャラが消えないので見た目上送っておきたいが
			// 移動中だった場合クラ落ちすることがある
			// if (skillType != TELEPORT) {
			// pc.sendPackets(new S_DeleteNewObject(pc));
			// pc.broadcastPacket(new S_DeleteObjectFromScreen(pc));
			// }

			try {
				Thread.sleep((int) (EFFECT_TIME[skillType] * 0.7));
			} catch (Exception e) {
			}
		}

		pc.setTeleportX(x);
		pc.setTeleportY(y);
		pc.setTeleportMapId(mapId);
		pc.setTeleportHeading(head);
		if (Config.SEND_PACKET_BEFORE_TELEPORT) {
			pc.sendPackets(new S_Teleport(pc));
			return true;
		} else {
			return Teleportation.Teleportation(pc);
		}
	}

	/*
	 * targetキャラクターのdistanceで指定したマス分前にテレポートする。指定されたマスがマップでない場合何もしない。
	 */
	public static void teleportToTargetFront(L1Character cha,
			L1Character target, int distance) {
		int locX = target.getX();
		int locY = target.getY();
		int heading = target.getHeading();
		L1Map map = target.getMap();
		short mapId = target.getMapId();

		// ターゲットの向きからテレポート先の座標を決める。
		switch (heading) {
		case 1:
			locX += distance;
			locY -= distance;
			break;

		case 2:
			locX += distance;
			break;

		case 3:
			locX += distance;
			locY += distance;
			break;

		case 4:
			locY += distance;
			break;

		case 5:
			locX -= distance;
			locY += distance;
			break;

		case 6:
			locX -= distance;
			break;

		case 7:
			locX -= distance;
			locY -= distance;
			break;

		case 0:
			locY -= distance;
			break;

		default:
			break;

		}

		if (map.isPassable(locX, locY)) {
			if (cha instanceof L1PcInstance) {
				teleport((L1PcInstance) cha, locX, locY, mapId, cha
						.getHeading(), true);
			} else if (cha instanceof L1NpcInstance) {
				((L1NpcInstance) cha).teleport(locX, locY, cha.getHeading());
			}
		}
	}

	public static void randomTeleport(L1PcInstance pc, boolean effectable) {
		// まだ本サーバのランテレ処理と違うところが結構あるような・・・
		L1Location newLocation = pc.getLocation().randomLocation(200, true);
		int newX = newLocation.getX();
		int newY = newLocation.getY();
		short mapId = (short) newLocation.getMapId();

		L1Teleport.teleport(pc, newX, newY, mapId, 5, effectable);
	}
}
