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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import jp.l1j.server.codes.Opcodes;
import jp.l1j.server.model.L1Character;

// Referenced classes of package jp.l1j.server.serverpackets:
// ServerBasePacket

public class S_RangeSkill extends ServerBasePacket {

	private static final String S_RANGE_SKILL = "[S] S_RangeSkill";
	private static Logger _log = Logger.getLogger(S_RangeSkill.class
			.getName());

	private static AtomicInteger _sequentialNumber = new AtomicInteger(0);

	private byte[] _byte = null;

	public static final int TYPE_NODIR = 0;

	public static final int TYPE_DIR = 8;

	public S_RangeSkill(L1Character cha, L1Character[] target, int spellgfx,
			int actionId, int type) {
		buildPacket(cha, target, spellgfx, actionId, type);
	}

	private void buildPacket(L1Character cha, L1Character[] target,
			int spellgfx, int actionId, int type) {
		writeC(Opcodes.S_OPCODE_RANGESKILLS);
		writeC(actionId);
		writeD(cha.getId());
		writeH(cha.getX());
		writeH(cha.getY());
		if (type == TYPE_NODIR) {
			writeC(cha.getHeading());
		} else if (type == TYPE_DIR) {
			int newHeading = calcheading(cha.getX(), cha.getY(),
					target[0].getX(), target[0].getY());
			cha.setHeading(newHeading);
			writeC(cha.getHeading());
		}
		writeD(_sequentialNumber.incrementAndGet()); // 番号がダブらないように送る。
		writeH(spellgfx);
		writeC(type); // 0:範囲 6:遠距離 8:範囲&遠距離
		writeH(0);
		writeH(target.length);
		for (int i = 0; i < target.length; i++) {
			if (target[i].getId() != cha.getId()) {
				writeD(target[i].getId());
				writeC(0x20); // 0:ダメージモーションあり 0以外:なし
			}
		}
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = _bao.toByteArray();
		}
		return _byte;
	}

	private static int calcheading(int myx, int myy, int tx, int ty) {
		int newheading = 0;
		if (tx > myx && ty > myy) {
			newheading = 3;
		}
		if (tx < myx && ty < myy) {
			newheading = 7;
		}
		if (tx > myx && ty == myy) {
			newheading = 2;
		}
		if (tx < myx && ty == myy) {
			newheading = 6;
		}
		if (tx == myx && ty < myy) {
			newheading = 0;
		}
		if (tx == myx && ty > myy) {
			newheading = 4;
		}
		if (tx < myx && ty > myy) {
			newheading = 5;
		}
		if (tx > myx && ty < myy) {
			newheading = 1;
		}
		return newheading;
	}

	@Override
	public String getType() {
		return S_RANGE_SKILL;
	}

}