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

import static jp.l1j.server.model.skill.L1SkillId.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.server.codes.ActionCodes;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.skill.L1SkillUse;
import jp.l1j.server.packets.server.S_DoActionGFX;
import jp.l1j.server.packets.server.S_SkillSound;

public class L1Chaser extends Thread implements Runnable {
	private static Logger _log = Logger.getLogger(L1Chaser.class.getName());

	private final L1PcInstance _pc;
	private final L1Character _cha;
	private final int _skillId;
	private final int _chaserCount;
	private final int _gfxId;
	private final boolean _isHpDrain;

	public L1Chaser(L1PcInstance pc, L1Character cha, int chaserCount,
							int skillId, int gfxId, boolean isHpDrain) {
		_cha = cha;
		_pc = pc;
		_skillId = skillId;
		_chaserCount = chaserCount;
		_gfxId = gfxId;
		_isHpDrain = isHpDrain;
	}

	@Override
	public void run() {
		try {
			if (_cha == null || _cha.isDead()) {
				_cha.removeSkillEffect(CHASER);
				return;
			}
			if (_cha.hasSkillEffect(CHASER)) {
				return;
			}
			_cha.setSkillEffect(CHASER, 0);
			Thread.sleep(500);
			for (int i = _chaserCount; i > 0; i--) {
				commitDamage();
				sendGraphics();
				Thread.sleep(1000);
			}
			_cha.removeSkillEffect(CHASER);
		} catch (Throwable e) {
			_cha.removeSkillEffect(CHASER);
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	private void commitDamage() {
		L1SkillUse l1skilluse = new L1SkillUse();
		l1skilluse.handleCommands(_pc, _cha, _skillId, _isHpDrain, true,  _cha.getId(),
									 	_cha.getX(), _cha.getY(), null, 0, L1SkillUse.TYPE_WEAPONSKILL);
	}
	private void sendGraphics() {
		if (_cha instanceof L1PcInstance) {
			((L1PcInstance) _cha).sendPackets(new S_SkillSound(_cha.getId(), _gfxId));
			((L1PcInstance) _cha).sendPackets(new S_DoActionGFX(_cha.getId(), ActionCodes.ACTION_Damage));
		}
		_cha.broadcastPacket(new S_SkillSound(_cha.getId(), _gfxId));
		_cha.broadcastPacket(new S_DoActionGFX(_cha.getId(), ActionCodes.ACTION_Damage));
	}
}
