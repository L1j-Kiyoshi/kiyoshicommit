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
import jp.l1j.server.model.instance.L1AuctionBoardInstance;
import jp.l1j.server.model.instance.L1BoardInstance;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;

// Referenced classes of package jp.l1j.server.clientpackets:
// ClientBasePacket, C_Board

public class C_Board extends ClientBasePacket {

	private static final String C_BOARD = "[C] C_Board";
	private static Logger _log = Logger.getLogger(C_Board.class.getName());

	private boolean isBoardInstance(L1Object obj) {
		return (obj instanceof L1BoardInstance
				|| obj instanceof L1AuctionBoardInstance);
	}

	public C_Board(byte abyte0[], ClientThread client) {
		super(abyte0);
		int objectId = readD();
		L1Object obj = L1World.getInstance().findObject(objectId);
		if (!isBoardInstance(obj)) {
			return; // 不正クライアントでなければあり得ないが・・・
		}
		obj.onAction(client.getActiveChar());
	}

	@Override
	public String getType() {
		return C_BOARD;
	}

}
