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
package jp.l1j.server.packets.server;

import jp.l1j.server.GameServer;
import jp.l1j.server.codes.Opcodes;

public class S_MessageYN extends ServerBasePacket {

	private byte[] _byte = null;

	public S_MessageYN(int type) {
		buildPacket(type, null, null, null, 1);
	}
	
	public S_MessageYN(int type, String msg1) {
		buildPacket(type, msg1, null, null, 1);
	}

	public S_MessageYN(int type, String msg1, String msg2) {
		buildPacket(type, msg1, msg2, null, 2);
	}

	public S_MessageYN(int type, String msg1, String msg2, String msg3) {
		buildPacket(type, msg1, msg2, msg3, 3);
	}

	private void buildPacket(int type, String msg1, String msg2, String msg3,
			int check) {
		writeC(Opcodes.S_OPCODE_YES_NO);
		writeH(0x0000);
		writeD(GameServer.getYesNoCount());
		writeH(type);
		if (check == 1) {
			writeS(msg1);
		} else if (check == 2) {
			writeS(msg1);
			writeS(msg2);
		} else if (check == 3) {
			writeS(msg1);
			writeS(msg2);
			writeS(msg3);
		}
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}

	@Override
	public String getType() {
		return "[S] S_MessageYN";
	}
}
