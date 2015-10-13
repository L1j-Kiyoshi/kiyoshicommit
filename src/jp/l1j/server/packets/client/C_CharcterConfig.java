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

package jp.l1j.server.packets.client;

import java.util.logging.Logger;
import jp.l1j.configure.Config;
import jp.l1j.server.ClientThread;
import jp.l1j.server.datatables.CharacterConfigTable;
import jp.l1j.server.model.instance.L1PcInstance;

// Referenced classes of package jp.l1j.server.clientpackets:
// ClientBasePacket, C_RequestDoors

public class C_CharcterConfig extends ClientBasePacket {

	private static Logger _log = Logger.getLogger(C_CharcterConfig.class
			.getName());
	private static final String C_CHARCTER_CONFIG = "[C] C_CharcterConfig";

	public C_CharcterConfig(byte abyte0[], ClientThread client)
			throws Exception {
		super(abyte0);
		if (Config.CHARACTER_CONFIG_IN_SERVER_SIDE) {
			L1PcInstance pc = client.getActiveChar();
			int length = readD() - 3;
			byte data[] = readByte();
			int count = CharacterConfigTable.getInstance()
					.countCharacterConfig(pc.getId());
			if (count == 0) {
				CharacterConfigTable.getInstance().storeCharacterConfig(pc
						.getId(), length, data);
			} else {
				CharacterConfigTable.getInstance().updateCharacterConfig(pc
						.getId(),length, data);
			}
		}
	}

	@Override
	public String getType() {
		return C_CHARCTER_CONFIG;
	}
}
