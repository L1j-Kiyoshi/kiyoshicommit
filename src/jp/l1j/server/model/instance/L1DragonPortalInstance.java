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

package jp.l1j.server.model.instance;

import static jp.l1j.server.model.skill.L1SkillId.*;
import jp.l1j.server.controller.DragonPortalController;
import jp.l1j.server.controller.raid.AntharasRaid;
import jp.l1j.server.controller.raid.DragonRaid;
import jp.l1j.server.controller.raid.FafurionRaid;
import jp.l1j.server.controller.raid.LindviorRaid;
import jp.l1j.server.datatables.NpcTalkDataTable;
import jp.l1j.server.model.L1NpcTalkData;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.packets.server.S_NpcTalkReturn;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.templates.L1Npc;

public class L1DragonPortalInstance extends L1NpcInstance {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @param template
	 */
	public L1DragonPortalInstance(L1Npc template) {
		super(template);
	}

	@Override
	public void onTalkAction(L1PcInstance pc) {
		int npcid = getNpcTemplate().getNpcId();
		DragonRaid raid = DragonPortalController.getInstance().getDragonRaid(getDragonRaidMapId());
		int X = 0;
		int Y = 0;
		int objid = getId();
		L1NpcTalkData talking = NpcTalkDataTable.getInstance().getTemplate(npcid);
		String htmlid = null;
		String[] htmldata = null;
		if (npcid >= 91051 && npcid <= 91054) {
			if (raid == null) {
				return;
			}
			if (raid.getAllPlayerList().size() >= 32) {
				pc.sendPackets(new S_ServerMessage(1536)); // 定員に達したため、入場できません。
			} else if (raid.isDragonAwaked()) {
				pc.sendPackets(new S_ServerMessage(1537)); // ドラゴンが目覚めたため、今は入場できません。
			} else {
				if (raid instanceof AntharasRaid) { // ドラゴンポータル(地)
					if (pc.hasSkillEffect(BLOODSTAIN_OF_ANTHARAS)) {
						pc.sendPackets(new S_ServerMessage(1626));
						// 全身からドラゴンの血の匂いが漂っています。それが消えるまでは、ドラゴンポータルに入場できません。
						return;
					}
					X = 32599;
					Y = 32742;
				} else if (raid instanceof FafurionRaid) { // ドラゴンポータル(水)
					if (pc.hasSkillEffect(BLOODSTAIN_OF_FAFURION)) {
						pc.sendPackets(new S_ServerMessage(1626));
						// 全身からドラゴンの血の匂いが漂っています。それが消えるまでは、ドラゴンポータルに入場できません。
						return;
					}
					X = 32927;
					Y = 32741;
				} else if (raid instanceof LindviorRaid) { // ドラゴンポータル(風)
					if (pc.hasSkillEffect(BLOODSTAIN_OF_LINDVIOR)) {
						pc.sendPackets(new S_ServerMessage(1626));
						// 全身からドラゴンの血の匂いが漂っています。それが消えるまでは、ドラゴンポータルに入場できません。
						return;
					}
					X = 32673;
					Y = 32926;
				//} else if (portalNumber < 24) { // ドラゴンポータル(火)(未実装)
				//	if (pc.hasSkillEffect(BLOODSTAIN_OF_VALAKAS)) {
				//		pc.sendPackets(new S_ServerMessage(1626));
				//		// 全身からドラゴンの血の匂いが漂っています。それが消えるまでは、ドラゴンポータルに入場できません。
				//		return;
				//	}
				//	X = ;
				//	Y = ;
				}
				raid.addAllPlayer(pc);
				L1Teleport.teleport(pc, X, Y, (short) raid.getMapId(), 2, true);
			}
		} else if (npcid == 91066) { // ドラゴンポータル(隠された竜の地)
			int level = pc.getLevel();
			if (level >= 30 && level <= 51) {
				htmlid = "dsecret1";
			} else if (level >= 52) {
				htmlid = "dsecret2";
			} else {
				htmlid = "dsecret3";
			}
		}

		if (htmlid != null) {
			pc.sendPackets(new S_NpcTalkReturn(objid, htmlid, htmldata));
		} else {
			pc.sendPackets(new S_NpcTalkReturn(talking, objid, 1));
		}
	}
}
