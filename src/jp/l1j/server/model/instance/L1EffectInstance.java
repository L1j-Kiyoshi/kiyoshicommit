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
package jp.l1j.server.model.instance;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import jp.l1j.server.GeneralThreadPool;
import jp.l1j.server.codes.ActionCodes;
import jp.l1j.server.controller.timer.WarTimeController;
import jp.l1j.server.model.L1CastleLocation;
import jp.l1j.server.model.L1Magic;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;
import jp.l1j.server.packets.server.S_DoActionGFX;
import jp.l1j.server.packets.server.S_RemoveObject;
import jp.l1j.server.templates.L1Npc;

public class L1EffectInstance extends L1NpcInstance {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static Logger _log = Logger.getLogger(L1EffectInstance.class
			.getName());

	private ScheduledFuture<?> _effectFuture;
	private static final int FW_DAMAGE_INTERVAL = 1000;

	public L1EffectInstance(L1Npc template) {
		super(template);

		int npcId = getNpcTemplate().getNpcId();
		if (npcId == 81157) { // FW
			_effectFuture = GeneralThreadPool.getInstance().schedule(
					new FwDamageTimer(this), 0);
		}
	}

	@Override
	public void onAction(L1PcInstance pc) {
	}

	@Override
	public void deleteMe() {
		_destroyed = true;
		if (getInventory() != null) {
			getInventory().clearItems();
		}
		allTargetClear();
		_master = null;
		L1World.getInstance().removeVisibleObject(this);
		L1World.getInstance().removeObject(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			pc.removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		removeAllKnownObjects();
	}

	class FwDamageTimer implements Runnable {
		private L1EffectInstance _effect;

		public FwDamageTimer(L1EffectInstance effect) {
			_effect = effect;
		}

		@Override
		public void run() {
			while (!_destroyed) {
				try {
					for (L1Object objects : L1World.getInstance()
							.getVisibleObjects(_effect, 0)) {
						if (objects instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) objects;
							if (pc.isDead()) {
								continue;
							}
							if (pc.getZoneType() == 1) {
								boolean isNowWar = false;
								int castleId = L1CastleLocation
										.getCastleIdByArea(pc);
								if (castleId > 0) {
									isNowWar = WarTimeController.getInstance()
											.isNowWar(castleId);
								}
								if (!isNowWar) {
									continue;
								}
							}
							L1Magic magic = new L1Magic(_effect, pc);
							int damage = magic.calcPcFireWallDamage();
							if (damage == 0) {
								continue;
							}
							pc.sendPackets(new S_DoActionGFX(pc.getId(),
									ActionCodes.ACTION_Damage));
							pc.broadcastPacket(new S_DoActionGFX(pc.getId(),
									ActionCodes.ACTION_Damage));
							pc.receiveDamage(_effect, damage, false);
						} else if (objects instanceof L1MonsterInstance) {
							L1MonsterInstance mob = (L1MonsterInstance) objects;
							if (mob.isDead()) {
								continue;
							}
							L1Magic magic = new L1Magic(_effect, mob);
							int damage = magic.calcNpcFireWallDamage();
							if (damage == 0) {
								continue;
							}
							mob.broadcastPacket(new S_DoActionGFX(mob.getId(),
									ActionCodes.ACTION_Damage));
							mob.receiveDamage(_effect, damage);
						}
					}
					Thread.sleep(FW_DAMAGE_INTERVAL);
				} catch (InterruptedException ignore) {
					// ignore
				}
			}
		}
	}

	private L1PcInstance _pc;

	public void setUser(L1PcInstance pc) {
		_pc = pc;
	}

	public L1PcInstance getUser() {
		return _pc;
	}

	private int _skillId;

	public void setSkillId(int i) {
		_skillId = i;
	}

	public int getSkillId() {
		return _skillId;
	}

}
