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

import static jp.l1j.locale.I18N.*;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_DoActionGFX;
import jp.l1j.server.packets.server.S_SystemMessage;

public class L1Action implements L1CommandExecutor {
	private static Logger _log = Logger.getLogger(L1Action.class.getName());

	private L1Action() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Action();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st = new StringTokenizer(arg);
			int actId = Integer.parseInt(st.nextToken(), 10);
			pc.sendPackets(new S_DoActionGFX(pc.getId(), actId));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(String.format(I18N_COMMAND_FORMAT_1,
					cmdName, I18N_ACT_ID)));
			// .%s %s の形式で入力してください。
		}
	}
}
