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

import java.util.logging.Logger;
import jp.l1j.server.codes.Opcodes;

// Referenced classes of package jp.l1j.server.serverpackets:
// ServerBasePacket

public class S_Fishing extends ServerBasePacket {

	private static Logger _log = Logger.getLogger(S_Fishing.class.getName());
	private static final String S_FISHING = "[S] S_Fishing";
	private byte[] _byte = null;

	public S_Fishing() {
		buildPacket();
	}

	public S_Fishing(int objectId, int motionNum, int x, int y) {
		buildPacket(objectId, motionNum, x, y);
	}

	private void buildPacket() {
		writeC(Opcodes.S_OPCODE_DOACTIONGFX);
		writeC(0x37); // ?
		writeD(0x76002822); // ?
		writeH(0x8AC3); // ?
	}

	private void buildPacket(int objectId, int motionNum, int x, int y) {
		writeC(Opcodes.S_OPCODE_DOACTIONGFX);
		writeD(objectId);
		writeC(motionNum);
		writeH(x);
		writeH(y);
		writeD(0);
		writeH(0);
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
		return S_FISHING;
	}
}
