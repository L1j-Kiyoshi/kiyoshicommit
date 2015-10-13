package jp.l1j.server.model.skill.uniqueskills;

import jp.l1j.server.model.L1Character;
import static jp.l1j.server.model.skill.L1SkillId.*;

public class L1UniqueSkillUtils {

	protected int ATTACK_SPEED = 1;
	protected int MAGIC_SPEED = 1;

	protected int calcSleepTime(L1Character cha, int sleepTime, int type) {
		hasSkillsAdjustment(cha, sleepTime);
		switch (cha.getMoveSpeed()) {
		case 0: // 通常
			break;
		case 1: // ヘイスト
			sleepTime -= (sleepTime * 0.25);
			break;
		case 2: // スロー
			sleepTime *= 2;
			break;
		}
		if (cha.getBraveSpeed() == 1) {
			sleepTime -= (sleepTime * 0.25);
		}
		if (cha.hasSkillEffect(WIND_SHACKLE)) {
			if (type == ATTACK_SPEED || type == MAGIC_SPEED) {
				sleepTime += (sleepTime * 2);// 0.25
			}
		}
		if (cha.hasSkillEffect(AREA_WIND_SHACKLE)) {
			if (type == ATTACK_SPEED || type == MAGIC_SPEED) {
				sleepTime += (sleepTime * 2);// 0.25
			}
		}
		return sleepTime;
	}

	protected void hasSkillsAdjustment(L1Character cha, int skillTime) {
		if (cha.hasSkillEffect(MASS_SLOW)) {
			skillTime = cha.getSkillEffectTimeSec(MASS_SLOW) * 1000;
			skillTime += _totalSleepTime;
			cha.setSkillEffect(MASS_SLOW, skillTime);
		}
		if (cha.hasSkillEffect(SLOW)) {
			skillTime = cha.getSkillEffectTimeSec(SLOW) * 1000;
			skillTime += _totalSleepTime;
			cha.setSkillEffect(SLOW, skillTime);
		}
		if (cha.hasSkillEffect(WIND_SHACKLE)) {
			skillTime = cha.getSkillEffectTimeSec(WIND_SHACKLE) * 1000;
			skillTime += _totalSleepTime;
			cha.setSkillEffect(WIND_SHACKLE, skillTime);
		}
		if (cha.hasSkillEffect(AREA_WIND_SHACKLE)) {
			skillTime = cha.getSkillEffectTimeSec(AREA_WIND_SHACKLE) * 1000;
			skillTime += _totalSleepTime;
			cha.setSkillEffect(AREA_WIND_SHACKLE, skillTime);
		}
	}

	private int _totalSleepTime = 0;

	public void setTotalSleepTime(int i) {
		_totalSleepTime = i;
	}

	public int getTotalSleepTime() {
		return _totalSleepTime;
	}
}
