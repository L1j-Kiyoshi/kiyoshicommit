package jp.l1j.server.model;

import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.packets.server.S_SystemMessage;

public class DeadTimer extends Thread {

	private L1Character _attacker;
	private L1Character _target;
	private int _deadTime;
	private boolean _isDestroy = false;

	/**
	 * このキャラクターが死ぬまでの時間を設定する。
	 * @param cha 死ぬ宣告を受けたキャラクター
	 * @param deadTime 死ぬまでの時間(秒)
	 */
	public DeadTimer(L1Character attacker, L1Character target, int deadTime) {
		_attacker = attacker;
		_target = target;
		_deadTime = deadTime;
	}


	@Override
	public void run() {
		try {
			if (_target instanceof L1PcInstance) {
				((L1PcInstance) _target).sendPackets(new S_SystemMessage("死の宣告を受けました。" +
						"死を迎えるまで残り" + _deadTime + "秒です。"));
			}
			while (_deadTime >= 0) {
				if (_isDestroy) {
					break;
				}
				Thread.sleep(1000);
				_deadTime--;
			}
			if (!_isDestroy) {
				if (_target instanceof L1PcInstance) {
					((L1PcInstance) _target).receiveDamage(_attacker, _target.getCurrentHp(), false);
				} else if (_target instanceof L1NpcInstance) {
					((L1NpcInstance) _target).receiveDamage(_attacker, _target.getCurrentHp());
				}
			}
		} catch(Exception e) {

		} finally {
			_deadTime = 0;
		}
	}

	public void setDestroy(boolean flag) {
		_isDestroy = flag;
	}

}
