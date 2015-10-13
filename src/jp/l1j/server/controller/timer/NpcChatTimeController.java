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

package jp.l1j.server.controller.timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;
import jp.l1j.configure.Config;
import jp.l1j.server.datatables.NpcChatTable;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.templates.L1NpcChat;

public class NpcChatTimeController implements Runnable {
	private static Logger _log = Logger.getLogger(NpcChatTimeController.class.getName());

	private static NpcChatTimeController _instance;

	public static NpcChatTimeController getInstance() {
		if (_instance == null) {
			_instance = new NpcChatTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				checkNpcChatTime(); // チャット開始時間をチェック
				Thread.sleep(60000);
			}
		} catch (Exception e1) {
			_log.warning(e1.getMessage());
		}
	}

	private void checkNpcChatTime() {
		for (L1NpcChat npcChat : NpcChatTable.getInstance().getAllGameTime()) {
			if (isChatTime(npcChat.getGameTime())) {
				int npcId = npcChat.getNpcId();
				for (L1Object obj : L1World.getInstance().getObject()) {
					if (!(obj instanceof L1NpcInstance)) {
						continue;
					}
					L1NpcInstance npc = (L1NpcInstance) obj;
					if (npc.getNpcTemplate().getNpcId() == npcId) {
						npc.startChat(L1NpcInstance.CHAT_TIMING_GAME_TIME);
					}
				}
			}
		}
	}

	private boolean isChatTime(int chatTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
		Calendar realTime = getRealTime();
		int nowTime = Integer.valueOf(sdf.format(realTime.getTime()));
		return (nowTime == chatTime);
	}

	private static Calendar getRealTime() {
		TimeZone _tz = TimeZone.getTimeZone(Config.TIME_ZONE);
		Calendar cal = Calendar.getInstance(_tz);
		return cal;
	}
}
