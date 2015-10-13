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
import jp.l1j.server.model.instance.L1SignboardInstance;

// Referenced classes of package jp.l1j.server.serverpackets:
// ServerBasePacket, S_SignboardPack

public class S_SignboardPack extends ServerBasePacket {

	private static Logger _log = Logger.getLogger(S_SignboardPack.class
			.getName());
	private static final String S_SIGNBOARD_PACK = "[S] S_SignboardPack";

	private static final int STATUS_POISON = 1;
	private static final int STATUS_INVISIBLE = 2;
	private static final int STATUS_PC = 4;
	private static final int STATUS_FREEZE = 8;
	private static final int STATUS_BRAVE = 16;
	private static final int STATUS_ELFBRAVE = 32;
	private static final int STATUS_FASTMOVABLE = 64;
	private static final int STATUS_GHOST = 128;

	private byte[] _byte = null;

	public S_SignboardPack(L1SignboardInstance signboard) {
		writeC(Opcodes.S_OPCODE_CHARPACK);
		writeH(signboard.getX());
		writeH(signboard.getY());
		writeD(signboard.getId());
		writeH(signboard.getGfxId());
		writeC(0);
		writeC(getDirection(signboard.getHeading()));
		writeC(0);
		writeC(0);
		writeD(0);
		writeH(0);
		writeS(null);
		writeS(signboard.getName());
		int status = 0;
		if (signboard.getPoison() != null) { // 毒状態
			if (signboard.getPoison().getEffectId() == 1) {
				status |= STATUS_POISON;
			}
		}
		writeC(status);
		writeD(0);
		writeS(null);
		writeS(null);
		writeC(0);
		writeC(0xFF);
		writeC(0);
		writeC(0);
		writeC(0);
		writeC(0xFF);
		writeC(0xFF);
	}

	private int getDirection(int heading) {
		int dir = 0;
		switch (heading) {
		case 2:
			dir = 1;
			break;
		case 3:
			dir = 2;
			break;
		case 4:
			dir = 3;
			break;
		case 6:
			dir = 4;
			break;
		case 7:
			dir = 5;
			break;
		}
		return dir;
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
		return S_SIGNBOARD_PACK;
	}

}
