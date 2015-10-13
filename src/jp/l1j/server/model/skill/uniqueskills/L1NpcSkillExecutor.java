package jp.l1j.server.model.skill.uniqueskills;

import java.util.ArrayList;

import jp.l1j.server.model.L1Attack;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.L1Location;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1MonsterInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.instance.L1PetInstance;
import jp.l1j.server.model.instance.L1SummonInstance;
import jp.l1j.server.packets.server.S_DoActionGFX;
import jp.l1j.server.packets.server.S_EffectLocation;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1PhysicalAttack;

/** NPCの物理スキルを簡易的に発動させるためのクラス */
public class L1NpcSkillExecutor {

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private L1Character _attacker;
	private L1Character _target;
	private L1PhysicalAttack _pa;

	private L1Location _baseSkillLocation;
	private L1Location _minLoc;
	private L1Location _maxLoc;

	private ArrayList<L1Character> _targetList = new ArrayList<L1Character>();

	public L1NpcSkillExecutor(L1Character attacker, L1Character target, L1PhysicalAttack pa) {
		_attacker = attacker;
		_baseSkillLocation = new L1Location(_attacker.getLocation());
		_minLoc = new L1Location(_attacker.getLocation());
		_maxLoc = new L1Location(_attacker.getLocation());
		_target = target;
		_pa = pa;
	}

	public boolean usePhysicalAttack() {
		if (!_attacker.isPossibleAttack()){
			return false;
		}
		makeTargetList(); // ターゲットリストの作成
		calcDamage(); // ダメージの計算とコミット
		sendGfx(); // グラフィックの送信
		return true;
	}

	/**
	 * 物理攻撃スキルが発動可能かを返す。
	 * @return
	 * 		このスキルが発動可能ならtrueを返す。
	 */
	public boolean isPhysicalAttackSkill() {

		if (!_attacker.isPossibleAttack()){
			return false;
		}

		// 範囲外
		if (_attacker.getLocation().getTileLineDistance(_target.getLocation()) > _pa.getRange()) {
			return false;
		}

		// 障害物がある場合攻撃不可能
		if (!(_attacker.glanceCheck(_attacker.getX(), _attacker.getY(), _target.getX(), _target.getY())
				||  _attacker.glanceCheck(_target.getX(), _target.getY(), _attacker.getX(), _attacker.getY()))) {
			return false;
		}

		// 死んでいる場合は発動しない
		if (_target.isDead()) {
			return false;
		}

		// ゴースト状態は対象外
		if (_target instanceof L1PcInstance) {
			if (((L1PcInstance) _target).isGhost()) {
				return false;
			}
		}
		return true;
	}

	private void sendGfx() {
		if (_pa.getCastGfx() > 0) { // グラフィックの送信
			if (!_pa.isBaseLocOut()) { // スキル発動起点がずれない
				if (_pa.getType().equalsIgnoreCase("attack")) { // ターゲット指定型
					if (_target instanceof L1PcInstance) {
						((L1PcInstance) _target).sendPackets(new S_SkillSound(_target.getId(), _pa.getCastGfx()));
					}
					_target.broadcastPacket(new S_SkillSound(_target.getId(), _pa.getCastGfx()));
				} else { // 術者中心
					_attacker.broadcastPacket(new S_SkillSound(_attacker.getId(), _pa.getCastGfx()));
				}
			} else { // 起点がずれる場合
				for (L1Object obj : L1World.getInstance().getVisiblePoint(_baseSkillLocation, 20)) {
					if (!(obj instanceof L1PcInstance)) {
						continue;
					}
					((L1PcInstance) obj).sendPackets(new S_EffectLocation(_baseSkillLocation, _pa.getCastGfx()));
				}
			}
		}

		 // ターゲット未指定型の場合はActionIdがあればここで送信。
		if (_pa.getActionId() > 0 && !_pa.getType().equalsIgnoreCase("attack")) {
			_attacker.broadcastPacket(new S_DoActionGFX(_attacker.getId(), _pa.getActionId()));
		}
	}

	private void calcDamage() {
		L1Attack attack;
		int damage = 0;

		for (int diceCount = _pa.getDamageDiceCount(); 0 < diceCount; diceCount--) {
			damage += _random.nextInt(_pa.getDamageDice()) + 1;
		}
		damage += _pa.getDamageValue();

		for (L1Character target : _targetList) {
			System.out.println("target : " + target.getName());
			attack = new L1Attack(_attacker, target);
			attack.setActId(_pa.getActionId());
			attack.setAddDamage(damage);
			if (attack.calcHit()) {
				attack.calcDamage();
			}
			if (!_pa.isBaseLocOut() && _pa.getType().equalsIgnoreCase("attack") &&
					_target.getId() == target.getId()) { // 起点がずれない、ターゲット指定型スキルの場合はモーション送信
				attack.action();
			}
			attack.commit();
			if (_pa.getAreaEffect() > 0) {
				if (target instanceof L1PcInstance) {
					((L1PcInstance) target).sendPackets(new S_SkillSound(target.getId(), _pa.getAreaEffect()));
				}
				target.broadcastPacket(new S_SkillSound(target.getId(), _pa.getAreaEffect()));
			}
		}
	}

	private void makeTargetList() {
		calcBaseLocation(); // スキルの範囲中心点の算出
		calcAreaLocations(); // スキルの特殊範囲の算出
		if (_pa.getArea() != 0) { // 純粋な範囲攻撃
			if (!_pa.isBaseLocOut()) { // 起点がずれないスキル
				L1Character baseTarget;
				if (_pa.getType().equalsIgnoreCase("attack")) { // ターゲット中心範囲
					baseTarget = _target;
				} else {
					baseTarget = _attacker;
				}
				for (L1Object obj : L1World.getInstance().getVisibleObjects(baseTarget, _pa.getArea())) {
					if (!(obj instanceof L1Character)) { // L1Character以外は対象外
						continue;
					}
					if (obj.getId() == _attacker.getId()) { // 術者は対象外
						continue;
					}
					if (isTarget((L1Character) obj)) {
						if (!_targetList.contains((L1Character) obj)) {
							_targetList.add((L1Character) obj);
						}
					}
				}
			} else { // 起点がずれる純粋範囲系スキル
				for (L1Object obj : L1World.getInstance().getVisiblePoint(_baseSkillLocation, _pa.getArea())) {
					if (!(obj instanceof L1Character)) { // L1Character以外は対象外
						continue;
					}
					if (obj.getId() == _attacker.getId()) { // 術者は対象外
						continue;
					}
					if (isTarget((L1Character) obj)) {
						if (!_targetList.contains((L1Character) obj)) {
							_targetList.add((L1Character) obj);
						}
					}
				}
			}
		} else if (_pa.isIrregularArea()) { // 特殊範囲系
			int max = Math.max(Math.abs(_pa.getAreaFront()), Math.abs(_pa.getAreaBack()));
			max = Math.max(Math.abs(_pa.getAreaLeft()), max);
			max = Math.max(Math.abs(_pa.getAreaRight()), max);

			if (!_pa.isBaseLocOut()) { // 起点がずれないスキル
				if (_pa.getType().equalsIgnoreCase("attack")) { // ターゲット中心範囲
					for (L1Object obj : L1World.getInstance().getVisibleObjects(_attacker, max)) {
						if (!(obj instanceof L1Character)) { // L1Character以外は対象外
							continue;
						}
						if (obj.getId() == _attacker.getId()) { // 術者は対象外
							continue;
						}
						if (!obj.getLocation().isRangeLocation(_minLoc, _maxLoc)) { // 特殊範囲外の場合は対象外
							continue;
						}
						if (isTarget((L1Character) obj)) {
							if (!_targetList.contains((L1Character) obj)) {
								_targetList.add((L1Character) obj);
							}
						}
					}
				} else if (_pa.getType().equalsIgnoreCase("none")) { // 術者中心の範囲
					for (L1Object obj : L1World.getInstance().getVisibleObjects(_target, max)) {
						if (!(obj instanceof L1Character)) { // L1Character以外は対象外
							continue;
						}
						if (obj.getId() == _attacker.getId()) { // 術者は対象外
							continue;
						}
						if (!obj.getLocation().isRangeLocation(_minLoc, _maxLoc)) { // 特殊範囲外の場合は対象外
							continue;
						}
						if (isTarget((L1Character) obj)) {
							if (!_targetList.contains((L1Character) obj)) {
								_targetList.add((L1Character) obj);
							}
						}
					}
				}
			} else { // 起点がずれるスキル
				for (L1Object obj : L1World.getInstance().getVisiblePoint(_baseSkillLocation, max)) {
					if (!(obj instanceof L1Character)) { // L1Character以外は対象外
						continue;
					}
					if (obj.getId() == _attacker.getId()) { // 術者は対象外
						continue;
					}
					if (!obj.getLocation().isRangeLocation(_minLoc, _maxLoc)) { // 特殊範囲外の場合は対象外
						continue;
					}
					if (isTarget((L1Character) obj)) {
						if (!_targetList.contains((L1Character) obj)) {
							_targetList.add((L1Character) obj);
						}
					}
				}
			}
		} else { // 単体攻撃スキル
			if (isTarget(_target)) {
				if (!_targetList.contains(_target)) {
					_targetList.add(_target);
				}
			}
		}
	}

	private void calcAreaLocations() {
		L1Location locFront = new L1Location(_baseSkillLocation);
		L1Location locBack = new L1Location(_baseSkillLocation);
		L1Location locRight = new L1Location(_baseSkillLocation);
		L1Location locLeft = new L1Location(_baseSkillLocation);

		if (_pa.getAreaFront() > 0) { // 前方向の範囲
			for (int i = 0; i < _pa.getAreaFront(); i++) {
				locFront.forward(_attacker.getHeading());
			}
		} else if (_pa.getAreaFront() < 0) { // 起点からずれた範囲指定
			for (int i = 0; i > _pa.getAreaFront(); i--) {
				locFront.forward(_attacker.getHeading());
			}
		}
		_minLoc.set(Math.min(_minLoc.getX(), locFront.getX()), Math.min(_minLoc.getY(), locFront.getY()));
		_maxLoc.set(Math.max(_maxLoc.getX(), locFront.getX()), Math.max(_maxLoc.getY(), locFront.getY()));

		if (_pa.getAreaBack() > 0) { // 後ろ方向の範囲
			for (int i = 0; i < _pa.getAreaBack(); i++) {
				locBack.backward(_attacker.getHeading());
			}
		} else if (_pa.getAreaBack() < 0) { // 起点からずれた範囲指定
			for (int i = 0; i > _pa.getAreaBack(); i--) {
				locBack.backward(_attacker.getHeading());
			}
		}
		_minLoc.set(Math.min(_minLoc.getX(), locBack.getX()), Math.min(_minLoc.getY(), locBack.getY()));
		_maxLoc.set(Math.max(_maxLoc.getX(), locBack.getX()), Math.max(_maxLoc.getY(), locBack.getY()));

		if (_pa.getAreaRight() > 0) { // 右方向の範囲
			for (int i = 0; i < _pa.getAreaRight(); i++) {
				locRight.rightward(_attacker.getHeading());
			}
		} else if (_pa.getAreaRight() < 0) { // 起点からずれた範囲指定
			for (int i = 0; i > _pa.getAreaRight(); i--) {
				locRight.rightward(_attacker.getHeading());
			}
		}
		_minLoc.set(Math.min(_minLoc.getX(), locRight.getX()), Math.min(_minLoc.getY(), locRight.getY()));
		_maxLoc.set(Math.max(_maxLoc.getX(), locRight.getX()), Math.max(_maxLoc.getY(), locRight.getY()));

		if (_pa.getAreaLeft() > 0) { // 左方向の範囲
			for (int i = 0; i < _pa.getAreaLeft(); i++) {
				locLeft.leftward(_attacker.getHeading());
			}
		} else if (_pa.getAreaLeft() < 0) { // 起点からずれた範囲指定
			for (int i = 0; i > _pa.getAreaLeft(); i--) {
				locLeft.leftward(_attacker.getHeading());
			}
		}
		_minLoc.set(Math.min(_minLoc.getX(), locLeft.getX()), Math.min(_minLoc.getY(), locLeft.getY()));
		_maxLoc.set(Math.max(_maxLoc.getX(), locLeft.getX()), Math.max(_maxLoc.getY(), locLeft.getY()));
	}

	private void calcBaseLocation() {
		_attacker.setHeading(_attacker.targetDirection(_target.getX(), _target.getY())); // 向きのセット
		int heading = _attacker.getHeading();

		if (_pa.getType().equalsIgnoreCase("attack")) {
			_baseSkillLocation.set(_target.getLocation());
		}

		if (_pa.getBaseFB() > 0) { // 前方向
			for (int i = 0; i < _pa.getBaseFB(); i++) {
				_baseSkillLocation.forward(heading);
			}
		} else if (_pa.getBaseFB() < 0) { // 後ろ方向
			for (int i = 0; i > _pa.getBaseFB(); i--) {
				_baseSkillLocation.backward(heading);
			}
		}

		if (_pa.getBaseRL() > 0) { // 右方向
			for (int i = 0; i < _pa.getBaseRL(); i++) {
				_baseSkillLocation.rightward(heading);
			}
		} else if (_pa.getBaseRL() < 0) { // 左方向
			for (int i = 0; i > _pa.getBaseRL(); i--) {
				_baseSkillLocation.leftward(heading);
			}
		}
		_minLoc.set(_baseSkillLocation);
		_maxLoc.set(_baseSkillLocation);
	}

	/**
	 * targetが範囲攻撃の対象になるかどうか
	 */
	private boolean isTarget(L1Character target) {

		// 障害物がある場合攻撃不可能
		if (_pa.getType().equalsIgnoreCase("none")) { // 中心点が術者
			if (!(_attacker.glanceCheck(_attacker.getX(), _attacker.getY(), target.getX(), target.getY())
					||  _attacker.glanceCheck(target.getX(), target.getY(), _attacker.getX(), _attacker.getY()))) {
				System.out.println("obj is no Target1 : " + target.getName());
				return false;
			}
		} else {
			if (!(_target.glanceCheck(_target.getX(), _target.getY(), target.getX(), target.getY())
					||  _target.glanceCheck(target.getX(), target.getY(), _target.getX(), _target.getY()))) {
				System.out.println("obj is no Target2 : " + target.getName());
				return false;
			}
		}
		// 死んでいる場合は発動しない
		if (target.isDead()) {
			System.out.println("obj is no Target3 : " + target.getName());
			return false;
		}
		// 凍結状態
		if (target.isThroughAttack()) {
			System.out.println("obj is no Target4 : " + target.getName());
			return false;
		}
		// ゴースト状態は対象外
		if (target instanceof L1PcInstance) {
			if (((L1PcInstance) target).isGhost()) {
				System.out.println("obj is no Target5 : " + target.getName());
				return false;
			}
		}
		// 対PCの場合対象はPC,サモン,ペットのみ
		if (_target instanceof L1PcInstance || _target instanceof L1PetInstance ||
			_target instanceof L1SummonInstance) {
			if (!(target instanceof L1PcInstance || target instanceof L1PetInstance ||
					target instanceof L1SummonInstance)) {
				System.out.println("obj is no Target6 : " + target.getName());
				return false;
			}
		}
		// 対モンスターの場合対象はモンスターのみ
		if (_target instanceof L1MonsterInstance) {
			if (!(target instanceof L1MonsterInstance)) {
				System.out.println("obj is no Target7 : " + target.getName());
				return false;
			}
		}
		return true;
	}
}
