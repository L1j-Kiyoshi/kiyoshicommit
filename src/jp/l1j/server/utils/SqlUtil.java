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

package jp.l1j.server.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlUtil {
	public static SQLException rollback(Connection con) {
		try {
			con.rollback();
		} catch (SQLException e) {
			return e;
		}
		return null;
	}

	public static SQLException close(Connection con) {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException e) {
			return e;
		}
		return null;
	}

	public static SQLException close(Statement ps) {
		try {
			if (ps != null) {
				ps.close();
			}
		} catch (SQLException e) {
			return e;
		}
		return null;
	}

	public static SQLException close(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			return e;
		}
		return null;
	}

	public static void close(ResultSet rs, Statement pstm, Connection con) {
		close(rs);
		close(pstm);
		close(con);
	}
}
