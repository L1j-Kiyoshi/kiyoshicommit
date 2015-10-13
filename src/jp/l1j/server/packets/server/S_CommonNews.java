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

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import jp.l1j.server.codes.Opcodes;

// Referenced classes of package jp.l1j.server.serverpackets:
// ServerBasePacket

public class S_CommonNews extends ServerBasePacket {

	private static final String _S__0B_COMMONNEWS = "[S] S_CommonNews";

	private static Logger _log = Logger.getLogger(S_CommonNews.class.getName());

	private ArrayList<String> _announcements;

	public S_CommonNews() {
		_announcements = new ArrayList<String>();
		loadAnnouncements();
		writeC(Opcodes.S_OPCODE_COMMONNEWS);
		String message = "";
		for (int i = 0; i < _announcements.size(); i++) {
			message = (new StringBuilder()).append(message).append(
					_announcements.get(i).toString()).append("\n").toString();
		}
		writeS(message);
	}

	public S_CommonNews(String s) {
		writeC(Opcodes.S_OPCODE_COMMONNEWS);
		writeS(s);
	}

	private void loadAnnouncements() {
		_announcements.clear();
		File file = new File("data/announcements.txt");
		if (file.exists()) {
			readFromDisk(file);
		}
	}

	private void readFromDisk(File file) {
		LineNumberReader lnr = null;
		try {
			String line = null;
			lnr = new LineNumberReader(new FileReader(file));
			do {
				if ((line = lnr.readLine()) == null) {
					break;
				}
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (st.hasMoreTokens()) {
					String announcement = st.nextToken();
					_announcements.add(announcement);
				} else {
					_announcements.add(" ");
				}
			} while (true);
		} catch (Exception e) {
		}
	}

	@Override
	public byte[] getContent() {
		return getBytes();
	}

	@Override
	public String getType() {
		return "[S] S_CommonNews";
	}

}
