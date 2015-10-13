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

public class S_Pledge extends ServerBasePacket {
	private static final String _S_Pledge = "[S] _S_Pledge";

	private byte[] _byte = null;

	public S_Pledge(String htmlid, int objid) {
		buildPacket(htmlid, objid, 0, "", "", "");
	}

	public S_Pledge(String htmlid, int objid, String clanname, String olmembers) {
		buildPacket(htmlid, objid, 1, clanname, "", olmembers);
	}

	public S_Pledge(String htmlid, int objid, String clanname,
			String olmembers, String allmembers) {

		buildPacket(htmlid, objid, 2, clanname, olmembers, allmembers);
	}

	private void buildPacket(String htmlid, int objid, int type,
			String clanname, String olmembers, String allmembers) {

		writeC(Opcodes.S_OPCODE_SHOWHTML);
		writeD(objid);
		writeS(htmlid);
		writeH(type);
		writeH(0x03);
		writeS(clanname); // clanname
		writeS(olmembers); // clanmember with a space in the end
		writeS(allmembers); // all clan members names with a space in the end
		// example: "player1 player2 player3 "
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = _bao.toByteArray();
		}
		return _byte;
	}

	@Override
	public String getType() {
		return _S_Pledge;
	}
}
