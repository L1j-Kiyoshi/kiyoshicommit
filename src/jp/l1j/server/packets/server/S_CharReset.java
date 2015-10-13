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

package jp.l1j.server.packets.server;

import java.util.logging.Logger;
import jp.l1j.server.codes.Opcodes;
import jp.l1j.server.model.instance.L1PcInstance;

// Referenced classes of package jp.l1j.server.serverpackets:
// ServerBasePacket

public class S_CharReset extends ServerBasePacket {

	private static Logger _log = Logger.getLogger(S_CharReset.class.getName());
	private static final String S_CHAR_RESET = "[S] S_CharReset";
	private byte[] _byte = null;

	/**
	 * 重置升級能力更新 [Server] opcode = 43 0000: 2b /02/ 01 2d/ 0f 00/ 04 00/ 0a 00
	 * /0c 0c 0c 0c 12 09 +..-............
	 */
	public S_CharReset(L1PcInstance pc, int lv, int hp,int mp, int ac, int str,
			int intel, int wis, int dex, int con, int cha) {
		writeC(Opcodes.S_OPCODE_CHARRESET);
		writeC(0x02);
		writeC(lv);
		writeC(pc.getTempMaxLevel()); // max lv
		writeH(hp);
		writeH(mp);
		writeH(ac);
		writeC(str);
		writeC(intel);
		writeC(wis);
		writeC(dex);
		writeC(con);
		writeC(cha);
	}

	public S_CharReset(int point) {
		writeC(Opcodes.S_OPCODE_CHARRESET);
		writeC(0x03);
		writeC(point);
	}

	/**
	 * 45及腰精進入崇志 [Server] opcode = 43 0000: 2b 01 0f 00 04 00 0a 2d 56法進入崇志
	 * [Server] opcode = 43 0000: 2b 01 0c 00 06 00 0a 38
	 */
	public S_CharReset(L1PcInstance pc) {
		writeC(Opcodes.S_OPCODE_CHARRESET);
		writeC(0x01);
		if (pc.isCrown()) {
			writeH(14);
			writeH(2);
		} else if (pc.isKnight()) {
			writeH(16);
			writeH(1);
		} else if (pc.isElf()) {
			writeH(15);
			writeH(4);
		} else if (pc.isWizard()) {
			writeH(12);
			writeH(6);
		} else if (pc.isDarkelf()) {
			writeH(12);
			writeH(3);
		} else if (pc.isDragonKnight()) {
			writeH(15);
			writeH(4);
		} else if (pc.isIllusionist()) {
			writeH(15);
			writeH(4);
		}
		writeC(0x0a); // AC
		writeC(pc.getTempMaxLevel()); // Lv
		/**
		 * 0000: 2b 04 60 04 06 01 07 1e 不知道幹麻用的
		 */
		// }else if(type == 4){
		// writeC(Opcodes.S_OPCODE_CHARRESET);
		// writeC(4);
		// writeC(0x60);
		// writeC(0x04);
		// writeC(0x09);
		// writeC(0x01);
		// writeC(0x07);
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
		return S_CHAR_RESET;
	}
}
