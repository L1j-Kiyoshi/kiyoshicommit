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
package jp.l1j.server.model;

import jp.l1j.server.GeneralThreadPool;
import jp.l1j.server.model.instance.L1MonsterInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.skill.L1SkillId;
import jp.l1j.server.packets.server.S_Paralysis;
import jp.l1j.server.packets.server.S_ServerMessage;
import static jp.l1j.server.model.skill.L1SkillId.*;

/*
 * L1ParalysisPoisonと被るコードが多い。特にタイマー。何とか共通化したいが難しい。
 */
public class L1CurseParalysis extends L1Paralysis {
	private final L1Character _target;
	private final int _delay;
	private final int _time;

	private Thread _timer;

	private class ParalysisDelayTimer extends Thread {
		@Override
		public void run() {
			_target.setSkillEffect(STATUS_CURSE_PARALYZING, 0);

			try {
				Thread.sleep(_delay); // 麻痺するまでの猶予時間を待つ。
			} catch (InterruptedException e) {
				_target.killSkillEffectTimer(STATUS_CURSE_PARALYZING);
				return;
			}

			if (_target instanceof L1PcInstance) {
				L1PcInstance player = (L1PcInstance) _target;
				if (!player.isDead()) {
					player.sendPackets(new S_Paralysis(1, true)); // 麻痺状態にする
				}
			}
			_target.setParalyzed(true);
			_timer = new ParalysisTimer();
			GeneralThreadPool.getInstance().execute(_timer); // 麻痺タイマー開始
			if (isInterrupted()) {
				_timer.interrupt();
			}
		}
	}

	private class ParalysisTimer extends Thread {
		@Override
		public void run() {
			_target.killSkillEffectTimer(STATUS_CURSE_PARALYZING);
			_target.setSkillEffect(STATUS_CURSE_PARALYZED, 0);
			try {
				Thread.sleep(_time);
			} catch (InterruptedException e) {
			}

			_target.killSkillEffectTimer(STATUS_CURSE_PARALYZED);
			if (_target instanceof L1PcInstance) {
				L1PcInstance player = (L1PcInstance) _target;
				if (!player.isDead()) {
					player.sendPackets(new S_Paralysis(1, false)); // 麻痺状態を解除する
				}
			}
			_target.setParalyzed(false);
			cure(); // 解呪処理
		}
	}

	private L1CurseParalysis(L1Character cha, int delay, int time) {
		_target = cha;
		_delay = delay;
		_time = time;

		curse();
	}

	private void curse() {
		if (_target instanceof L1PcInstance) {
			L1PcInstance player = (L1PcInstance) _target;
			player.sendPackets(new S_ServerMessage(212));
		}

		_target.setPoisonEffect(2);

		_timer = new ParalysisDelayTimer();
		GeneralThreadPool.getInstance().execute(_timer);
	}

	public static boolean curse(L1Character cha, int delay, int time) {
		if (!(cha instanceof L1PcInstance || cha instanceof L1MonsterInstance)) {
			return false;
		}
		if (cha.hasSkillEffect(STATUS_CURSE_PARALYZING)
				|| cha.hasSkillEffect(STATUS_CURSE_PARALYZED)) {
			return false; // 既に麻痺している
		}

		cha.setParalaysis(new L1CurseParalysis(cha, delay, time));
		return true;
	}

	@Override
	public int getEffectId() {
		return 2;
	}

	@Override
	public void cure() {
		if (_timer != null) {
			_timer.interrupt(); // 麻痺タイマー解除
		}

		_target.setPoisonEffect(0);
		_target.setParalaysis(null);
	}
}
