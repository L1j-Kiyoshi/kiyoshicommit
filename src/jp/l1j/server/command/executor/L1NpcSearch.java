/*
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

package jp.l1j.server.command.executor;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jp.l1j.locale.I18N.*;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_OutputRawString;
import jp.l1j.server.packets.server.S_SystemMessage;

public class L1NpcSearch implements L1CommandExecutor {
	private static Logger _log = Logger.getLogger(L1NpcSearch.class.getName());

	private L1NpcSearch() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1NpcSearch();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		StringBuilder title = new StringBuilder("");
		StringBuilder msg = new StringBuilder("");
		try {
			StringTokenizer st = new StringTokenizer(arg);
			int npcId = Integer.parseInt(st.nextToken(), 10);
			L1NpcInstance npc = null;
			for (L1Object obj : L1World.getInstance().getObject()) {
				if (!(obj instanceof L1NpcInstance)) {
					continue;
				}
				npc = (L1NpcInstance) obj;
				if (npc.getNpcTemplate().getNpcId() != npcId) {
					continue;
				}
				if (title.toString().equalsIgnoreCase("")) {
					title.append(npc.getName() + "の検索結果");
				}
				msg.append("(" + npc.getLocation().getX() + ", " + npc.getLocation().getY()
							+ ", " + npc.getLocation().getMapId() + ")　　");
			}
			if (!msg.toString().equalsIgnoreCase("")) {
				pc.sendPackets(new S_OutputRawString(pc.getId(), title.toString(), msg.toString()));
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			pc.sendPackets(new S_SystemMessage(String.format(I18N_COMMAND_FORMAT_1,
					cmdName, I18N_NPC_ID)));
			// .%s %s の形式で入力してください。
		}
	}
}
