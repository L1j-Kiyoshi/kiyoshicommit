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

public class S_WhoCharinfo extends ServerBasePacket {
	private static final String S_WHO_CHARINFO = "[S] S_WhoCharinfo";
	private static Logger _log = Logger
			.getLogger(S_WhoCharinfo.class.getName());

	private byte[] _byte = null;

	public S_WhoCharinfo(L1PcInstance pc) {
		_log.fine("Who charpack for : " + pc.getName());

		String lawfulness = "";
		int lawful = pc.getLawful();
		if (lawful < 0) {
			lawfulness = "(Chaotic)";
		} else if (lawful >= 0 && lawful < 500) {
			lawfulness = "(Neutral)";
		} else if (lawful >= 500) {
			lawfulness = "(Lawful)";
		}

		writeC(Opcodes.S_OPCODE_GLOBALCHAT);
		writeC(0x08);

		String title = "";
		String clan = "";

		if (pc.getTitle().equalsIgnoreCase("") == false) {
			title = pc.getTitle() + " ";
		}

		if (pc.getClanId() > 0) {
			clan = "[" + pc.getClanName() + "]";
		}

		writeS(title + pc.getName() + " " + lawfulness + " " + clan);
		// writeD(0x80157FE4);
		writeD(0);
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
		return S_WHO_CHARINFO;
	}
}
