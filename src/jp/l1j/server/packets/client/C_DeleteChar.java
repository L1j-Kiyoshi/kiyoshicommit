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

import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.l1j.configure.Config;
import jp.l1j.server.ClientThread;
import jp.l1j.server.datatables.CharacterTable;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.L1Clan;
import jp.l1j.server.model.L1World;
import jp.l1j.server.packets.server.S_DeleteCharOK;

// Referenced classes of package jp.l1j.server.clientpackets:
// ClientBasePacket, C_DeleteChar

public class C_DeleteChar extends ClientBasePacket {

	private static final String C_DELETE_CHAR = "[C] RequestDeleteChar";

	private static Logger _log = Logger.getLogger(C_DeleteChar.class.getName());

	public C_DeleteChar(byte decrypt[], ClientThread client) throws Exception {
		super(decrypt);
		String name = readS();

		try {
			L1PcInstance pc = CharacterTable.getInstance().restoreCharacter(
					name);
			if (pc != null && pc.getLevel() >= 30
					&& Config.DELETE_CHARACTER_AFTER_7DAYS) {
				if (pc.getType() < 32) {
					if (pc.isCrown()) {
						pc.setType(32);
					} else if (pc.isKnight()) {
						pc.setType(33);
					} else if (pc.isElf()) {
						pc.setType(34);
					} else if (pc.isWizard()) {
						pc.setType(35);
					} else if (pc.isDarkelf()) {
						pc.setType(36);
					} else if (pc.isDragonKnight()) {
						pc.setType(37);
					} else if (pc.isIllusionist()) {
						pc.setType(38);
					}
					Timestamp deleteTime = new Timestamp(System
							.currentTimeMillis() + 604800000); // 7日後
					pc.setDeleteTime(deleteTime);
					pc.save(); // DBにキャラクター情報を書き込む
				} else {
					if (pc.isCrown()) {
						pc.setType(0);
					} else if (pc.isKnight()) {
						pc.setType(1);
					} else if (pc.isElf()) {
						pc.setType(2);
					} else if (pc.isWizard()) {
						pc.setType(3);
					} else if (pc.isDarkelf()) {
						pc.setType(4);
					} else if (pc.isDragonKnight()) {
						pc.setType(5);
					} else if (pc.isIllusionist()) {
						pc.setType(6);
					}
					pc.setDeleteTime(null);
					pc.save(); // DBにキャラクター情報を書き込む
				}
				client.sendPacket(new S_DeleteCharOK(
						S_DeleteCharOK.DELETE_CHAR_AFTER_7DAYS));
				return;
			}

			if (pc != null) {
				L1Clan clan = L1World.getInstance().getClan(pc.getClanName());
				if (clan != null) {
					clan.delMemberName(name);
				}
			}
			CharacterTable.getInstance().deleteCharacter(
					client.getAccount().getId(), name);
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			client.close();
			return;
		}
		client.sendPacket(new S_DeleteCharOK(S_DeleteCharOK.DELETE_CHAR_NOW));
	}

	@Override
	public String getType() {
		return C_DELETE_CHAR;
	}

}
