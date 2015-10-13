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

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.l1j.server.codes.Opcodes;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

// Referenced classes of package jp.l1j.server.serverpackets:
// ServerBasePacket

public class S_AuctionBoard extends ServerBasePacket {

	private static Logger _log = Logger.getLogger(S_AuctionBoard.class.
			getName());
	private static final String S_AUCTIONBOARD = "[S] S_AuctionBoard";
	private byte[] _byte = null;

	public S_AuctionBoard(L1NpcInstance board) {
		buildPacket(board);
	}

	private void buildPacket(L1NpcInstance board) {
		ArrayList<Integer> houseList = new ArrayList<Integer>();
		int houseId = 0;
		int count = 0;
		int[] id = null;
		String[] name = null;
		int[] area = null;
		int[] month = null;
		int[] day = null;
		int[] price = null;
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM auction_houses");
			rs = pstm.executeQuery();
			while (rs.next()) {
				houseId = rs.getInt("house_id");
				if (board.getX() == 33421 && board.getY() == 32823) { // 競売掲示板(ギラン)
					if (houseId >= 262145 && houseId <= 262189) {
						houseList.add(houseId);
						count++;
					}
				} else if (board.getX() == 33585 && board.getY() == 33235) { // 競売掲示板(ハイネ)
					if (houseId >= 327681 && houseId <= 327691) {
						houseList.add(houseId);
						count++;
					}
				} else if (board.getX() == 33959 && board.getY() == 33253) { // 競売掲示板(アデン)
					if (houseId >= 458753 && houseId <= 458819) {
						houseList.add(houseId);
						count++;
					}
				} else if (board.getX() == 32611 && board.getY() == 32775) { // 競売掲示板(グルーディン)
					if (houseId >= 524289 && houseId <= 524294) {
						houseList.add(houseId);
						count++;
					}
				}
			}
			id = new int[count];
			name = new String[count];
			area = new int[count];
			month = new int[count];
			day = new int[count];
			price = new int[count];

			for (int i = 0; i < count; ++i) {
				pstm = con.prepareStatement("SELECT * FROM auction_houses LEFT JOIN houses ON auction_houses.house_id=houses.id WHERE house_id=?");
				houseId = houseList.get(i);
				pstm.setInt(1, houseId);
				rs = pstm.executeQuery();
				while (rs.next()) {
					id[i] = rs.getInt("house_id");
					name[i] = rs.getString("name");
					area[i] = rs.getInt("area");
					Calendar cal = timestampToCalendar((Timestamp) rs.getObject("deadline"));
					month[i] = cal.get(Calendar.MONTH) + 1;
					day[i] = cal.get(Calendar.DATE);
					price[i] = rs.getInt("price");
				}
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(rs);
			SqlUtil.close(pstm);
			SqlUtil.close(con);
		}

		writeC(Opcodes.S_OPCODE_HOUSELIST);
		writeD(board.getId());
		writeH(count); // レコード数
		for (int i = 0; i < count; ++i) {
			writeD(id[i]); // アジトの番号
			writeS(name[i]); // アジトの名前
			writeH(area[i]); // アジトの広さ
			writeC(month[i]); // 締切月
			writeC(day[i]); // 締切日
			writeD(price[i]); // 現在の入札価格
		}
	}

	private Calendar timestampToCalendar(Timestamp ts) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(ts.getTime());
		return cal;
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
		return S_AUCTIONBOARD;
	}
}
