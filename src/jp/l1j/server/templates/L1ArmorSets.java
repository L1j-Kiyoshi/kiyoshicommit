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

package jp.l1j.server.templates;

import jp.l1j.server.model.L1PolyMorph;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.inventory.L1PcInventory;

public class L1ArmorSets {
	public L1ArmorSets() {
	}

	private int _id;

	public int getId() {
		return _id;
	}

	public void setId(int i) {
		_id = i;
	}

	private int _effectItemId;

	public int getEffectItemId() {
		return _effectItemId;
	}

	public void setEffectItemId(int i) {
		_effectItemId = i;
	}

	private int[] _sets;

	public int[] getSets() {
		return _sets;
	}

	public void setSets(int[] i) {
		_sets = i;
	}

	private int _polyId;

	public int getPolyId() {
		return _polyId;
	}

	public void setPolyId(int i) {
		_polyId = i;
	}

	private int _ac;

	public int getAc() {
		return _ac;
	}

	public void setAc(int i) {
		_ac = i;
	}

	private int _str;

	public int getStr() {
		return _str;
	}

	public void setStr(int i) {
		_str = i;
	}

	private int _dex;

	public int getDex() {
		return _dex;
	}

	public void setDex(int i) {
		_dex = i;
	}

	private int _con;

	public int getCon() {
		return _con;
	}

	public void setCon(int i) {
		_con = i;
	}

	private int _wis;

	public int getWis() {
		return _wis;
	}

	public void setWis(int i) {
		_wis = i;
	}

	private int _cha;

	public int getCha() {
		return _cha;
	}

	public void setCha(int i) {
		_cha = i;
	}

	private int _int;

	public int getInt() {
		return _int;
	}

	public void setInt(int i) {
		_int = i;
	}

	private int _hp;

	public int getHp() {
		return _hp;
	}

	public void setHp(int i) {
		_hp = i;
	}

	private int _hpr;

	public int getHpr() {
		return _hpr;
	}

	public void setHpr(int i) {
		_hpr = i;
	}

	private int _mp;

	public int getMp() {
		return _mp;
	}

	public void setMp(int i) {
		_mp = i;
	}

	private int _mpr;

	public int getMpr() {
		return _mpr;
	}

	public void setMpr(int i) {
		_mpr = i;
	}

	private int _sp;

	public int getSp() {
		return _sp;
	}

	public void setSp(int i) {
		_sp = i;
	}

	private int _mr;

	public int getMr() {
		return _mr;
	}

	public void setMr(int i) {
		_mr = i;
	}

	private int _damageReduction;

	public int getDamageReduction() {
		return _damageReduction;
	}

	public void setDamageReduction(int i) {
		_damageReduction = i;
	}

	private int _weightReduction;

	public int getWeightReduction() {
		return _weightReduction;
	}

	public void setWeightReduction(int i) {
		_weightReduction = i;
	}

	private int _hitModifier;

	public int getHitModifier() {
		return _hitModifier;
	}

	public void setHitModifier(int i) {
		_hitModifier =  i;
	}

	private int _dmgModifier;

	public int getDmgModifier() {
		return _dmgModifier;
	}

	public void setDmgModifer(int i) {
		_dmgModifier =  i;
	}

	private int _bowHitModifier;

	public int getBowHitModifier() {
		return _bowHitModifier;
	}

	public void setBowHitModifier(int i) {
		_bowHitModifier =  i;
	}

	private int _bowDmgModifier;

	public int getBowDmgModifier() {
		return _bowDmgModifier;
	}

	public void setBowDmgModifier(int i) {
		_bowDmgModifier =  i;
	}

	private int _defenseFire;

	public int getDefenseFire() {
		return _defenseFire;
	}

	public void setDefenseFire(int i) {
		_defenseFire = i;
	}

	private int _defenseWater;

	public int getDefenseWater() {
		return _defenseWater;
	}

	public void setDefenseWater(int i) {
		_defenseWater = i;
	}

	private int _defenseEarth;

	public int getDefenseEarth() {
		return _defenseEarth;
	}

	public void setDefenseEarth(int i) {
		_defenseEarth = i;
	}

	private int _defenseWind;

	public int getDefenseWind() {
		return _defenseWind;
	}

	public void setDefenseWind(int i) {
		_defenseWind = i;
	}

	private int _defenseLight;

	public int getDefenseLight() {
		return _defenseLight;
	}

	public void setDefenseLight(int i) {
		_defenseLight = i;
	}

	private int _resistStun;

	public int getResistStun() {
		return _resistStun;
	}

	public void setResistStun(int i) {
		_resistStun = i;
	}

	private int _resistStone;

	public int getResistStone() {
		return _resistStone;
	}

	public void setResistStone(int i) {
		_resistStone = i;
	}

	private int _resistSleep;

	public int getResistSleep() {
		return _resistSleep;
	}

	public void setResistSleep(int i) {
		_resistSleep = i;
	}

	private int _resistFreeze;

	public int getResistFreeze() {
		return _resistFreeze;
	}

	public void setResistFreeze(int i) {
		_resistFreeze = i;
	}

	private int _resistHold;

	public int getResistHold() {
		return _resistHold;
	}

	public void setResistHold(int i) {
		_resistHold = i;
	}

	private int _resistBlind;

	public int getResistBlind() {
		return _resistBlind;
	}

	public void setResistBlind(int i) {
		_resistBlind = i;
	}

	private boolean _isHaste;

	public boolean getIsHaste() {
		return _isHaste;
	}

	public void setIsHaste(boolean i) {
		_isHaste = i;
	}

	private int _expBonus;

	public int getExpBonus() {
		return _expBonus;
	}

	public void setExpBonus(int i) {
		_expBonus = i;
	}

	private int _potionRecoveryRate;

	public int getPotionRecoveryRate() {
		return _potionRecoveryRate;
	}

	public void setPotionRecoveryRate(int i) {
		_potionRecoveryRate = i;
	}

	/**
	 * 引数itemIdがこのセットに含まれるかどうか。
	 * @param itemId
	 * @return
	 * 		引数itemIdが含まれるならtrue
	 */
	public boolean isPartOfSet(int itemId) {
		for (int setItemId : _sets) {
			if (itemId == setItemId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * PCがこのセット装備を全部装備しているか。
	 * @param pc
	 * @return
	 * 		全て装備しているならtrue
	 */
	public boolean isAllEquipment(L1PcInstance pc) {
		return pc.getInventory().checkEquipped(_sets);
	}
	public boolean isEquippedRingOfArmorSet(L1PcInstance pc) {
		L1PcInventory pcInventory = pc.getInventory();
		L1ItemInstance armor = null;
		boolean isSetContainRing = false;

		// セット装備にリングが含まれているか調べる
		for (int id : _sets) {
			armor = pcInventory.findItemId(id);
			if (armor.getItem().getType2() == 2
					&& armor.getItem().getType() == 11) { // ring
				isSetContainRing = true;
				break;
			}
		}

		// リングを2つ装備していて、それが両方セット装備か調べる
		if (armor != null && isSetContainRing) {
			int itemId = armor.getItem().getItemId();
			if (pcInventory.getTypeEquipped(2, 11) == 2) {
				L1ItemInstance ring[] = new L1ItemInstance[2];
				ring = pcInventory.getRingEquipped();
				if (ring[0].getItem().getItemId() == itemId
						&& ring[1].getItem().getItemId() == itemId) {
					return true;
				}
			}
		}
		return false;
	}

	public void addPolyEffect(L1PcInstance pc) {
		if (getPolyId() == -1) {
			return;
		}
		int gfxId = getPolyId();
		if (gfxId == 6080 || gfxId == 6094) {
			if (pc.getSex() == 0) {
				gfxId = 6094;
			} else {
				gfxId = 6080;
			}
			if (!isRemainderOfCharge(pc)) { // 残チャージ数なし
				return;
			}
		}
		pc.setPolyItemEquipped(true);
		L1PolyMorph.doPoly(pc, getPolyId(), 0, L1PolyMorph.MORPH_BY_ITEMMAGIC);
	}

	private boolean isRemainderOfCharge(L1PcInstance pc) {
		boolean isRemainderOfCharge = false;
		if (pc.getInventory().checkItem(20383, 1)) {
			L1ItemInstance item = pc.getInventory().findItemId(20383);
			if (item != null) {
				if (item.getChargeCount() != 0) {
					isRemainderOfCharge =true;
				}
			}
		}
		return isRemainderOfCharge;
	}
	public void removePolyEffect(L1PcInstance pc) {
		int gfxId = getPolyId();
		if (gfxId == 6080) {
			if (pc.getSex() == 0) {
				gfxId = 6094;
			}
		}
		if (pc.getTempCharGfx() != gfxId) {
			return;
		}
		pc.setPolyItemEquipped(false);
		L1PolyMorph.undoPoly(pc);
	}

	public void addEffect(L1PcInstance pc) {
		pc.addAc(getAc());
		pc.addMaxHp(getHp());
		pc.addHpr(getHpr());
		pc.addMaxMp(getMp());
		pc.addMpr(getMpr());
		pc.addStr(getStr());
		pc.addDex(getDex());
		pc.addCon(getCon());
		pc.addWis(getWis());
		pc.addCha(getCha());
		pc.addInt(getInt());
		pc.addSp(getSp());
		pc.addMr(getMr());
		pc.addDamageReductionByArmor(getDamageReduction());
		pc.addWeightReduction(getWeightReduction());
		pc.addHitModifierByArmor(getHitModifier());
		pc.addDmgModifierByArmor(getDmgModifier());
		pc.addBowHitModifierByArmor(getBowHitModifier());
		pc.addBowDmgModifierByArmor(getBowDmgModifier());
		pc.addEarth(getDefenseEarth());
		pc.addFire(getDefenseFire());
		pc.addWater(getDefenseWater());
		pc.addWind(getDefenseWind());
		pc.addLight(getDefenseLight());
		pc.addResistStun(getResistStun());
		pc.addResistStone(getResistStone());
		pc.addResistSleep(getResistSleep());
		pc.addResistFreeze(getResistFreeze());
		pc.addResistHold(getResistHold());
		pc.addResistBlind(getResistBlind());
		pc.addExpBonusPct(getExpBonus());
		pc.addPotionRecoveryRatePct(getPotionRecoveryRate());
	}

	public void removeEffect(L1PcInstance pc) {
		pc.addAc(-getAc());
		pc.addMaxHp(-getHp());
		pc.addHpr(-getHpr());
		pc.addMaxMp(-getMp());
		pc.addMpr(-getMpr());
		pc.addStr(-getStr());
		pc.addDex(-getDex());
		pc.addCon(-getCon());
		pc.addWis(-getWis());
		pc.addCha(-getCha());
		pc.addInt(-getInt());
		pc.addSp(-getSp());
		pc.addMr(-getMr());
		pc.addDamageReductionByArmor(-getDamageReduction());
		pc.addWeightReduction(-getWeightReduction());
		pc.addHitModifierByArmor(-getHitModifier());
		pc.addDmgModifierByArmor(-getDmgModifier());
		pc.addBowHitModifierByArmor(-getBowHitModifier());
		pc.addBowDmgModifierByArmor(-getBowDmgModifier());
		pc.addEarth(-getDefenseEarth());
		pc.addFire(-getDefenseFire());
		pc.addWater(-getDefenseWater());
		pc.addWind(-getDefenseWind());
		pc.addLight(-getDefenseLight());
		pc.addResistStun(-getResistStun());
		pc.addResistStone(-getResistStone());
		pc.addResistSleep(-getResistSleep());
		pc.addResistFreeze(-getResistFreeze());
		pc.addResistHold(-getResistHold());
		pc.addResistBlind(-getResistBlind());
		pc.addExpBonusPct(-getExpBonus());
		pc.addPotionRecoveryRatePct(-getPotionRecoveryRate());
	}
}
