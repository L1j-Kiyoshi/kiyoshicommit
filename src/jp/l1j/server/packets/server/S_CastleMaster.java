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

import jp.l1j.server.codes.Opcodes;

// Referenced classes of package jp.l1j.server.serverpackets:
// ServerBasePacket

public class S_CastleMaster extends ServerBasePacket {

	private static final String _S__08_CASTLEMASTER = "[S] S_CastleMaster";

	private byte[] _byte = null;

	public S_CastleMaster(int type, int objecId) {
		buildPacket(type, objecId);
	}

	private void buildPacket(int type, int objecId) {
		writeC(Opcodes.S_OPCODE_CASTLEMASTER);
		writeC(type);
		writeD(objecId);
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
		return _S__08_CASTLEMASTER;
	}

}
