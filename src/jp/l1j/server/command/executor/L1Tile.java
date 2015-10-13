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

package jp.l1j.server.command.executor;

import java.util.logging.Level;
import java.util.logging.Logger;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.map.L1WorldMap;
import jp.l1j.server.packets.server.S_SystemMessage;

public class L1Tile implements L1CommandExecutor {
	private static Logger _log = Logger.getLogger(L1Tile.class.getName());

	private L1Tile() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Tile();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			int locX = pc.getX();
			int locY = pc.getY();
			short mapId = pc.getMapId();
			int tile0 = L1WorldMap.getInstance().getMap(mapId).getOriginalTile(locX, locY - 1);
			int tile1 = L1WorldMap.getInstance().getMap(mapId).getOriginalTile(locX + 1, locY - 1);
			int tile2 = L1WorldMap.getInstance().getMap(mapId).getOriginalTile(locX + 1, locY);
			int tile3 = L1WorldMap.getInstance().getMap(mapId).getOriginalTile(locX + 1, locY + 1);
			int tile4 = L1WorldMap.getInstance().getMap(mapId).getOriginalTile(locX, locY + 1);
			int tile5 = L1WorldMap.getInstance().getMap(mapId).getOriginalTile(locX - 1, locY + 1);
			int tile6 = L1WorldMap.getInstance().getMap(mapId).getOriginalTile(locX - 1, locY);
			int tile7 = L1WorldMap.getInstance().getMap(mapId).getOriginalTile(locX - 1, locY - 1);
			String msg = String.format("0:%d 1:%d 2:%d 3:%d 4:%d 5:%d 6:%d 7:%d",
					tile0, tile1, tile2, tile3, tile4, tile5, tile6, tile7);
			pc.sendPackets(new S_SystemMessage(msg));
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
