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
import jp.l1j.server.model.instance.L1PcInstance;

// Referenced classes of package jp.l1j.server.serverpackets:
// ServerBasePacket

public class S_OwnCharAttrDef extends ServerBasePacket {

	private static Logger _log = Logger.getLogger(S_OwnCharAttrDef
			.class.getName());
	private static final String S_OWNCHARATTRDEF = "[S] S_OwnCharAttrDef";
	private byte[] _byte = null;

	public S_OwnCharAttrDef(L1PcInstance pc) {
		buildPacket(pc);
	}

	private void buildPacket(L1PcInstance pc) {
		writeC(Opcodes.S_OPCODE_OWNCHARATTRDEF);
		writeC(pc.getAc());
		writeC(pc.getFire());
		writeC(pc.getWater());
		writeC(pc.getWind());
		writeC(pc.getEarth());
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
		return S_OWNCHARATTRDEF;
	}
}
