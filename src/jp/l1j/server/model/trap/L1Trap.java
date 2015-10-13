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
package jp.l1j.server.model.trap;

import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;
import jp.l1j.server.packets.server.S_EffectLocation;
import jp.l1j.server.storage.TrapStorage;

public abstract class L1Trap {
	protected final int _id;
	protected final int _gfxId;
	protected final boolean _isDetectionable;

	public L1Trap(TrapStorage storage) {
		_id = storage.getInt("id");
		_gfxId = storage.getInt("gfx_id");
		_isDetectionable = storage.getBoolean("is_detectionable");
	}

	public L1Trap(int id, int gfxId, boolean detectionable) {
		_id = id;
		_gfxId = gfxId;
		_isDetectionable = detectionable;
	}

	public int getId() {
		return _id;
	}

	public int getGfxId() {
		return _gfxId;
	}

	protected void sendEffect(L1Object trapObj) {
		if (getGfxId() == 0) {
			return;
		}
		S_EffectLocation effect = new S_EffectLocation(trapObj.getLocation(),
				getGfxId());

		for (L1PcInstance pc : L1World.getInstance()
				.getRecognizePlayer(trapObj)) {
			pc.sendPackets(effect);
		}
	}

	public abstract void onTrod(L1PcInstance trodFrom, L1Object trapObj);

	public void onDetection(L1PcInstance caster, L1Object trapObj) {
		if (_isDetectionable) {
			sendEffect(trapObj);
		}
	}

	public static L1Trap newNull() {
		return new L1NullTrap();
	}
}

class L1NullTrap extends L1Trap {
	public L1NullTrap() {
		super(0, 0, false);
	}

	@Override
	public void onTrod(L1PcInstance trodFrom, L1Object trapObj) {
	}
}
