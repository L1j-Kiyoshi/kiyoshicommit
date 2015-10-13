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

package jp.l1j.server.packets.client;

import java.util.logging.Logger;
import jp.l1j.configure.Config;
import jp.l1j.server.ClientThread;
import jp.l1j.server.codes.Opcodes;
import jp.l1j.server.datatables.ChatLogTable;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.L1World;
import jp.l1j.server.packets.server.S_ChatPacket;
import jp.l1j.server.packets.server.S_ServerMessage;

// Referenced classes of package jp.l1j.server.clientpackets:
// ClientBasePacket

public class C_ChatWhisper extends ClientBasePacket {

	private static final String C_CHAT_WHISPER = "[C] C_ChatWhisper";
	private static Logger _log = Logger
			.getLogger(C_ChatWhisper.class.getName());

	public C_ChatWhisper(byte abyte0[], ClientThread client) throws Exception {
		super(abyte0);
		String targetName = readS();
		String text = readS();
		L1PcInstance whisperFrom = client.getActiveChar();
		// チャット禁止中の場合
		if (whisperFrom.hasSkillEffect(1005)) {
			whisperFrom.sendPackets(new S_ServerMessage(242)); // 現在チャット禁止中です。
			return;
		}
		// ウィスパー可能なLv未満の場合
		if (whisperFrom.getLevel() < Config.WHISPER_CHAT_LEVEL) {
			whisperFrom.sendPackets(new S_ServerMessage(404, String
					.valueOf(Config.WHISPER_CHAT_LEVEL))); // %0レベル以下ではウィスパー、パーティーチャットは使用できません。
			return;
		}
		L1PcInstance whisperTo = L1World.getInstance().getPlayer(targetName);
		// ワールドにいない場合
		if (whisperTo == null) {
			whisperFrom.sendPackets(new S_ServerMessage(73, targetName)); // \f1%0はゲームをしていません。
			return;
		}
		// 自分自身に対するwisの場合
		if (whisperTo.equals(whisperFrom)) {
			return;
		}
		// 遮断されている場合
		if (whisperTo.getExcludingList().contains(whisperFrom.getName())) {
			whisperFrom.sendPackets(new S_ServerMessage(117, whisperTo
					.getName())); // %0があなたを遮断しました。
			return;
		}
		// ゲームオプションでOFFにしている場合
		if (!whisperTo.isCanWhisper()) {
			whisperFrom.sendPackets(new S_ServerMessage(205, whisperTo
					.getName()));
			return;
		}

		ChatLogTable.getInstance().storeChat(whisperFrom, whisperTo, text, 1);
		whisperFrom.sendPackets(new S_ChatPacket(whisperTo, text,
				Opcodes.S_OPCODE_GLOBALCHAT, 9));
		whisperTo.sendPackets(new S_ChatPacket(whisperFrom, text,
				Opcodes.S_OPCODE_WHISPERCHAT, 16));
	}

	@Override
	public String getType() {
		return C_CHAT_WHISPER;
	}
}
