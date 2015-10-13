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
import jp.l1j.server.ClientThread;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.L1ChatParty;
import jp.l1j.server.model.L1World;
import jp.l1j.server.packets.server.S_Party;
import jp.l1j.server.packets.server.S_ServerMessage;

// Referenced classes of package jp.l1j.server.clientpackets:
// ClientBasePacket

public class C_ChatParty extends ClientBasePacket {

	private static final String C_CHAT_PARTY = "[C] C_ChatParty";
	private static Logger _log = Logger.getLogger(C_ChatParty.class.getName());

	public C_ChatParty(byte abyte0[], ClientThread clientthread) {
		super(abyte0);

		L1PcInstance pc = clientthread.getActiveChar();
		if (pc.isGhost()) {
			return;
		}

		int type = readC();
		if (type == 0) { // /chatbanishコマンド
			String name = readS();

			if (!pc.isInChatParty()) {
				// パーティーに加入していません。
				pc.sendPackets(new S_ServerMessage(425));
				return;
			}
			if (!pc.getChatParty().isLeader(pc)) {
				// パーティーのリーダーのみが追放できます。
				pc.sendPackets(new S_ServerMessage(427));
				return;
			}
			L1PcInstance targetPc = L1World.getInstance().getPlayer(name);
			if (targetPc == null) {
				// %0という名前の人はいません。
				pc.sendPackets(new S_ServerMessage(109));
				return;
			}
			if (pc.getId() == targetPc.getId()) {
				return;
			}

			for (L1PcInstance member : pc.getChatParty().getMembers()) {
				if (member.getName().toLowerCase().equals(name.toLowerCase())) {
					pc.getChatParty().kickMember(member);
					return;
				}
			}
			// 見つからなかった
			// %0はパーティーメンバーではありません。
			pc.sendPackets(new S_ServerMessage(426, name));
		} else if (type == 1) { // /chatoutpartyコマンド
			if (pc.isInChatParty()) {
				pc.getChatParty().leaveMember(pc);
			}
		} else if (type == 2) { // /chatpartyコマンド
			L1ChatParty chatParty = pc.getChatParty();
			if (pc.isInChatParty()) {
				pc.sendPackets(new S_Party("party", pc.getId(), chatParty
						.getLeader().getName(), chatParty
						.getMembersNameList()));
			} else {
				pc.sendPackets(new S_ServerMessage(425)); // パーティーに加入していません。
// pc.sendPackets(new S_Party("party", pc.getId()));
			}
		}
	}

	@Override
	public String getType() {
		return C_CHAT_PARTY;
	}

}
