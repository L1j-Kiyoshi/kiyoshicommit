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

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.server.model.instance.L1EffectInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1MagicDoll;
import jp.l1j.server.types.Point;

public class HpRegeneration extends TimerTask {

	private static Logger _log = Logger.getLogger(HpRegeneration.class
			.getName());

	private final L1PcInstance _pc;

	private int _regenMax = 0;

	private int _regenPoint = 0;

	private int _curPoint = 4;

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	public HpRegeneration(L1PcInstance pc) {
		_pc = pc;

		updateLevel();
	}

	public void setState(int state) {
		if (_curPoint < state) {
			return;
		}

		_curPoint = state;
	}

	@Override
	public void run() {
		try {
			if (_pc.isDead()) {
				return;
			}

			_regenPoint += _curPoint;
			_curPoint = 4;

			synchronized (this) {
				if (_regenMax <= _regenPoint) {
					_regenPoint = 0;
					regenHp();
				}
			}
		} catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void updateLevel() {
		final int lvlTable[] = new int[] { 30, 25, 20, 16, 14, 12, 11, 10, 9,
				3, 2 };

		int regenLvl = Math.min(10, _pc.getLevel());
		if (30 <= _pc.getLevel() && _pc.isKnight()) {
			regenLvl = 11;
		}

		synchronized (this) {
			_regenMax = lvlTable[regenLvl - 1] * 4;
		}
	}

	public void regenHp() {
		if (_pc.isDead()) {
			return;
		}

		int maxBonus = 1;

		// CONボーナス
		if (11 < _pc.getLevel() && 14 <= _pc.getCon()) {
			maxBonus = _pc.getCon() - 12;
			if (25 < _pc.getCon()) {
				maxBonus = 14;
			}
		}

		int equipHpr = 0;
		equipHpr += _pc.getHpr();
		int bonus = _random.nextInt(maxBonus) + 1;

		if (_pc.hasSkillEffect(NATURES_TOUCH)) {
			bonus += 15;
		}
		if (L1HouseLocation.isInHouse(_pc.getX(), _pc.getY(), _pc.getMapId())) {
			bonus += 5;
		}
		if (_pc.getMapId() == 16384 || _pc.getMapId() == 16896
				|| _pc.getMapId() == 17408 || _pc.getMapId() == 17920
				|| _pc.getMapId() == 18432 || _pc.getMapId() == 18944
				|| _pc.getMapId() == 19968 || _pc.getMapId() == 19456
				|| _pc.getMapId() == 20480 || _pc.getMapId() == 20992
				|| _pc.getMapId() == 21504 || _pc.getMapId() == 22016
				|| _pc.getMapId() == 22528 || _pc.getMapId() == 23040
				|| _pc.getMapId() == 23552 || _pc.getMapId() == 24064
				|| _pc.getMapId() == 24576 || _pc.getMapId() == 25088) { // 宿屋
			bonus += 5;
		}
		// マザーツリー
		if ((_pc.getLocation().isInScreen(new Point(33055, 32336))
				&& _pc.getMapId() == 4 && _pc.isElf())) {
			bonus += 5;
		}
		// シルベリア（シーレン加護）・ベヒモス（アウラキア加護）
		if ((_pc.getLocation().isInScreen(new Point(32801, 32863)) && (_pc
				.getMapId() == 1000 || _pc.getMapId() == 1001))) {
			bonus += 5;
		}
		// 傲慢の塔 休憩階(x6階)
		if ((_pc.getLocation().isInScreen(new Point(33806, 32863)) && _pc.getMapId() == 106)
				|| (_pc.getLocation().isInScreen(new Point(32782, 32863))
				&& (_pc.getMapId() == 106 || _pc.getMapId() == 116
				|| _pc.getMapId() == 126 || _pc.getMapId() == 136
				|| _pc.getMapId() == 146))
				|| (_pc.getLocation().isInScreen(new Point(32784, 32798))
				&& (_pc.getMapId() == 156 || _pc.getMapId() == 166
				|| _pc.getMapId() == 176 || _pc.getMapId() == 186
				|| _pc.getMapId() == 196))) {
			bonus += 3;
		}
		if (_pc.hasSkillEffect(COOKING_1_5_N)
				|| _pc.hasSkillEffect(COOKING_1_5_S)) {
			bonus += 3;
		}
		if (_pc.hasSkillEffect(COOKING_2_4_N)
				|| _pc.hasSkillEffect(COOKING_2_4_S)
				|| _pc.hasSkillEffect(COOKING_3_6_N)
				|| _pc.hasSkillEffect(COOKING_3_6_S)) {
			bonus += 2;
		}
		if (_pc.getOriginalHpr() > 0) { // オリジナルCON HPR補正
			bonus += _pc.getOriginalHpr();
		}
		bonus += L1MagicDoll.getNatHprByDoll(_pc); // マジックドールによるHPR補正

		boolean inLifeStream = false;
		if (isPlayerInLifeStream(_pc)) {
			inLifeStream = true;
			// 古代の空間、魔族の神殿ではHPR+3はなくなる？
			bonus += 3;
		}

		// 空腹と重量のチェック
		if (_pc.getFood() < 3 || isOverWeight(_pc)
				|| _pc.hasSkillEffect(BERSERKERS)) {
			bonus = 0;
			// 装備によるＨＰＲ増加は満腹度、重量によってなくなるが、 減少である場合は満腹度、重量に関係なく効果が残る
			if (equipHpr > 0) {
				equipHpr = 0;
			}
		}

		int newHp = _pc.getCurrentHp();
		newHp += bonus + equipHpr;

		if (newHp < 1) {
			newHp = 1; // ＨＰＲ減少装備によって死亡はしない
		}
		// 水中での減少処理
		// ライフストリームで減少をなくせるか不明
		if (isUnderwater(_pc)) {
			newHp -= 20;
			if (newHp < 1) {
				if (_pc.isGm()) {
					newHp = 1;
				} else {
					_pc.death(null); // 窒息によってＨＰが０になった場合は死亡する。
				}
			}
		}
		// Lv50クエストの古代の空間1F2Fでの減少処理
		if (isLv50Quest(_pc) && !inLifeStream) {
			newHp -= 10;
			if (newHp < 1) {
				if (_pc.isGm()) {
					newHp = 1;
				} else {
					_pc.death(null); // ＨＰが０になった場合は死亡する。
				}
			}
		}
		// 魔族の神殿での減少処理
		if (_pc.getMapId() == 410 && !inLifeStream) {
			newHp -= 10;
			if (newHp < 1) {
				if (_pc.isGm()) {
					newHp = 1;
				} else {
					_pc.death(null); // ＨＰが０になった場合は死亡する。
				}
			}
		}

		if (!_pc.isDead()) {
			_pc.setCurrentHp(Math.min(newHp, _pc.getMaxHp()));
		}
	}

	private boolean isUnderwater(L1PcInstance pc) {
		// ウォーターブーツ装備時か、 エヴァの祝福状態、修理された装備セットであれば水中では無いとみなす。
		if (pc.getInventory().checkEquipped(20207)) {
			return false;
		}
		if (pc.hasSkillEffect(STATUS_UNDERWATER_BREATH)) {
			return false;
		}
		if (pc.getInventory().checkEquipped(21048)
				&& pc.getInventory().checkEquipped(21049)
				&& pc.getInventory().checkEquipped(21050)) {
			return false;
		}
		// マジックドール：マーメイド
		if (L1MagicDoll.isBlessOfEva(pc)) {
			return false;
		}

		return pc.getMap().isUnderwater();
	}

	private boolean isOverWeight(L1PcInstance pc) {
		// エキゾチックバイタライズ状態、アディショナルファイアー状態か
		// ゴールデンウィング装備時であれば、重量オーバーでは無いとみなす。
		if (pc.hasSkillEffect(EXOTIC_VITALIZE)
				|| pc.hasSkillEffect(ADDITIONAL_FIRE)
				|| pc.hasSkillEffect(AWAKEN_FAFURION)) {
			return false;
		}
		if (pc.getInventory().checkEquipped(20049)) {
			return false;
		}

		return (120 <= pc.getInventory().getWeight240()) ? true : false;
	}

	private boolean isLv50Quest(L1PcInstance pc) {
		int mapId = pc.getMapId();
		return (mapId == 2000 || mapId == 2001) ? true : false;
	}

	/**
	 * 指定したPCがライフストリームの範囲内にいるかチェックする
	 *
	 * @param pc
	 *            PC
	 * @return true PCがライフストリームの範囲内にいる場合
	 */
	private static boolean isPlayerInLifeStream(L1PcInstance pc) {
		for (L1Object object : pc.getKnownObjects()) {
			if (object instanceof L1EffectInstance == false) {
				continue;
			}
			L1EffectInstance effect = (L1EffectInstance) object;
			if (effect.getNpcId() == 81169
					&& effect.getLocation().getTileLineDistance(
							pc.getLocation()) < 4) {
				return true;
			}
		}
		return false;
	}
}
