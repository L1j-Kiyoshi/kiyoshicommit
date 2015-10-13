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

public class S_SystemMessage extends ServerBasePacket {
	private static final String S_SYSTEM_MESSAGE = "[S] S_SystemMessage";

	private static Logger _log = Logger.getLogger(S_SystemMessage.class
			.getName());

	private byte[] _byte = null;
	private final String _msg;

	/**
	 * クライアントにデータの存在しないオリジナルのメッセージを表示する。
	 * メッセージにnameid($xxx)が含まれている場合はオーバーロードされたもう一方を使用する。
	 * 
	 * @param msg -
	 *            表示する文字列
	 */
	public S_SystemMessage(String msg) {
		_msg = msg;
		writeC(Opcodes.S_OPCODE_GLOBALCHAT);
		writeC(0x09);
		writeS(msg);
	}

	/**
	 * クライアントにデータの存在しないオリジナルのメッセージを表示する。
	 * 
	 * @param msg -
	 *            表示する文字列
	 * @param nameid -
	 *            文字列にnameid($xxx)が含まれている場合trueにする。
	 */
	public S_SystemMessage(String msg, boolean nameid) {
		_msg = msg;
		writeC(Opcodes.S_OPCODE_NPCSHOUT);
		writeC(2);
		writeD(0);
		writeS(msg);
		// NPCチャットパケットであればnameidが解釈されるためこれを利用する
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}

	@Override
	public String toString() {
		return String.format("%s: %s", S_SYSTEM_MESSAGE, _msg);
	}

	@Override
	public String getType() {
		return S_SYSTEM_MESSAGE;
	}
}
