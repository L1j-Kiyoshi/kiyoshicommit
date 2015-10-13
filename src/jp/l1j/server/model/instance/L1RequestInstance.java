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

import java.util.logging.Logger;
import jp.l1j.server.datatables.NpcTalkDataTable;
import jp.l1j.server.model.L1NpcTalkData;
import jp.l1j.server.packets.server.S_NpcTalkReturn;
import jp.l1j.server.templates.L1Npc;

public class L1RequestInstance extends L1NpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger _log = Logger.getLogger(L1RequestInstance.class
			.getName());

	public L1RequestInstance(L1Npc template) {
		super(template);
	}

	@Override
	public void onAction(L1PcInstance player) {
		int objid = getId();

		L1NpcTalkData talking = NpcTalkDataTable.getInstance().getTemplate(
				getNpcTemplate().getNpcId());

		if (talking != null) {
			if (player.getLawful() < -1000) { // プレイヤーがカオティック
				player.sendPackets(new S_NpcTalkReturn(talking, objid, 2));
			} else {
				player.sendPackets(new S_NpcTalkReturn(talking, objid, 1));
			}
		} else {
			_log.finest("No actions for npc id : " + objid);
		}
	}

	@Override
	public void onFinalAction(L1PcInstance player, String action) {

	}

	public void doFinalAction(L1PcInstance player) {

	}
}
