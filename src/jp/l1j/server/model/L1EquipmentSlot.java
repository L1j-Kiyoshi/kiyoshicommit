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

package jp.l1j.server.model;

import static jp.l1j.server.model.skill.L1SkillId.*;

import java.util.ArrayList;
import java.util.logging.Logger;

import jp.l1j.server.datatables.ArmorSetTable;
import jp.l1j.server.datatables.SkillTable;
import jp.l1j.server.model.instance.L1DollInstance;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.inventory.L1PcInventory;
import jp.l1j.server.packets.server.S_Ability;
import jp.l1j.server.packets.server.S_AddSkill;
import jp.l1j.server.packets.server.S_DelSkill;
import jp.l1j.server.packets.server.S_Invis;
import jp.l1j.server.packets.server.S_RemoveObject;
import jp.l1j.server.packets.server.S_SpMr;
import jp.l1j.server.packets.server.S_SkillBrave;
import jp.l1j.server.packets.server.S_SkillHaste;
import jp.l1j.server.packets.server.S_SystemMessage;
import jp.l1j.server.templates.L1ArmorSets;
import jp.l1j.server.templates.L1Item;

public class L1EquipmentSlot {
	private static Logger _log = Logger.getLogger(L1EquipmentSlot.class.getName());

	private L1PcInstance _owner;

	/**
	 * 効果中のセットアイテム
	 */
	private ArrayList<L1ArmorSets> _currentArmorSet;

	private L1ItemInstance _weapon;
	private ArrayList<L1ItemInstance> _armors;

	public L1EquipmentSlot(L1PcInstance owner) {
		_owner = owner;

		_armors = new ArrayList<L1ItemInstance>();
		_currentArmorSet = new ArrayList<L1ArmorSets>();
	}

	private void setWeapon(L1ItemInstance weapon) {
		_owner.setWeapon(weapon);
		_owner.setCurrentWeapon(weapon.getItem().getType1());
		weapon.startChargeTimer(_owner);
		_weapon = weapon;
	}

	public L1ItemInstance getWeapon() {
		return _weapon;
	}

	private void setArmorSets(L1ItemInstance armor) {
		L1ItemInstance mainItem = null;
		for (L1ArmorSets armorSets : ArmorSetTable.getInstance().getArmorSetsList()) {
			if (armorSets.isPartOfSet(armor.getItemId()) && armorSets.isAllEquipment(_owner)) { // セット効果条件
				if (armor.getItemId() == armorSets.getEffectItemId()) {
					mainItem = armor;
				} else {
					for (L1ItemInstance checkItem : _armors) {
						if (checkItem.getItemId() == armorSets.getEffectItemId()) {
							mainItem = checkItem;
							break;
						}
					}
				}
				if (mainItem == null) {
					if(!_currentArmorSet.contains(armorSets)) { // リングを2つ装備していて、それが両方セット装備か調べる
						_currentArmorSet.add(armorSets);
						armorSets.addEffect(_owner);
					}
				} else if (mainItem.getArmorSets() == null) {
					 // baseItemIdがある場合はアイテムにセット効果を上乗せ。重複しない。
					mainItem.setArmorSets(armorSets);
					if (armor.getItemId() != armorSets.getEffectItemId()) {
						// 装備したものと別の装備に効果が付与する場合は効果を意図的に追加する。
						armorSets.addEffect(_owner);
					}
					_owner.getInventory().updateItem(mainItem);
				}
				armorSets.addPolyEffect(_owner);
			}
		}
	}

	private void setArmor(L1ItemInstance armor) {
		L1Item item = armor.getItem();
		int itemId = item.getItemId();

		armor.startChargeTimer(_owner);

		if (armor.getItem().getType() < 10) { // 防具
			_owner.addAc(item.getAc() + armor.getAc() - armor.getEnchantLevel() - armor.getAcByMagic());
		} else if (armor.getItem().getType() >= 10
				&& armor.getItem().getType() <= 18) { // アクセサリー、文様、タリスマン
			_owner.addAc(item.getAc() + armor.getAc() - armor.getAcByMagic());
		}

		_owner.addDamageReductionByArmor(item.getDamageReduction() + armor.getDamageReduction());
		_owner.addWeightReduction(item.getWeightReduction() + armor.getWeightReduction());
		_owner.addHitModifierByArmor(item.getHitModifierByArmor() + armor.getHitModifier());
		_owner.addDmgModifierByArmor(item.getDmgModifierByArmor() + armor.getDmgModifier());
		_owner.addBowHitModifierByArmor(item.getBowHitModifierByArmor() + armor.getBowHitModifier());
		_owner.addBowDmgModifierByArmor(item.getBowDmgModifierByArmor() + armor.getBowDmgModifier());
		_armors.add(armor);

		if (itemId == 20077 || itemId == 20062 || itemId == 120077) {
			if (!_owner.hasSkillEffect(INVISIBILITY)) {
				_owner.killSkillEffectTimer(BLIND_HIDING);
				_owner.setSkillEffect(INVISIBILITY, 0);
				_owner.sendPackets(new S_Invis(_owner.getId(), 1));
				_owner.broadcastPacketForFindInvis(new S_RemoveObject(_owner), false);
				for (L1DollInstance doll : _owner.getDollList().values()) {
					doll.deleteDoll();
				}
			}
		}
		if (itemId == 20288) { // ROTC
			_owner.sendPackets(new S_Ability(1, true));
		}
		if (itemId == 20383) { // 騎馬用ヘルム
			if (armor.getChargeCount() != 0) {
				armor.setChargeCount(armor.getChargeCount() - 1);
				_owner.getInventory().updateItem(armor,
						L1PcInventory.COL_CHARGE_COUNT);
			}
		}
		if (itemId >= 21176 && itemId <= 21179) { // パプリオンハイドロシリーズ
			_owner.setSkillEffect(DIVINE_PROTECTION_OF_FAFURION, 0);
			_owner.startRegularlyEffect(DIVINE_PROTECTION_OF_FAFURION);
		}
		if (itemId == 21180) { // リンドビオルストームプレートメイル
			_owner.setSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_PLATE, 0);
			_owner.startRegularlyEffect(DIVINE_PROTECTION_OF_LINDVIOR_PLATE);
		}
		if (itemId == 21181) { // リンドビオルストームローブ
			_owner.setSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_ROBE, 0);
			_owner.startRegularlyEffect(DIVINE_PROTECTION_OF_LINDVIOR_ROBE);
		}
		if (itemId == 21182) { // リンドビオルストームレザーアーマー
			_owner.setSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_LEATHER, 0);
			_owner.startRegularlyEffect(DIVINE_PROTECTION_OF_LINDVIOR_LEATHER);
		}
		if (itemId == 21183) { // リンドビオルストームスケイルメイル
			_owner.setSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_SCALE, 0);
			_owner.startRegularlyEffect(DIVINE_PROTECTION_OF_LINDVIOR_SCALE);
		}
		if (itemId == 21337) { // 反逆者のシールド
			_owner.setSkillEffect(SHIELD_OF_TEAIROR, 0);
			_owner.setTeairorChance(armor.getEnchantLevel() * 2);
		}
		if (itemId == 21355) { // ドラゴニックハロウィンアーマー
			_owner.setSkillEffect(DIVINE_PROTECTION_OF_HALLOWEEN, 0);
			_owner.startRegularlyEffect(DIVINE_PROTECTION_OF_HALLOWEEN);
		}
		if (itemId == 21352 && armor.getEnchantLevel() >= 5) { // ルームティス レッド イアリング
			_owner.setSkillEffect(EARRING_OF_ROOMTIS_RED, 0);
			_owner.setRedEarringChance(armor.getEnchantLevel() - 3);
		}
	}

	public ArrayList<L1ItemInstance> getArmors() {
		return _armors;
	}

	private void removeWeapon(L1ItemInstance weapon) {
		_owner.setWeapon(null);
		_owner.setCurrentWeapon(0);
		weapon.stopChargeTimer();
		_weapon = null;
		if (_owner.hasSkillEffect(COUNTER_BARRIER)) {
			_owner.removeSkillEffect(COUNTER_BARRIER);
		}
	}

	private void removeArmorSets(L1ItemInstance armor) {
		L1ItemInstance mainItem = null;
		for (L1ArmorSets armorSets : ArmorSetTable.getInstance().getArmorSetsList()) {
			if (armorSets.isPartOfSet(armor.getItemId()) && !armorSets.isAllEquipment(_owner)) { // セット効果条件
				if (armor.getItemId() == armorSets.getEffectItemId()) {
					mainItem = armor;
				} else {
					for (L1ItemInstance checkItem : _armors) {
						if (checkItem.getItemId() == armorSets.getEffectItemId()) {
							mainItem = checkItem;
							break;
						}
					}
				}
				if (mainItem == null) {
					if(_currentArmorSet.contains(armorSets)) { //
						_currentArmorSet.remove(armorSets);
						armorSets.removeEffect(_owner);
					}
				} else if (mainItem.getArmorSets() != null) {
					 // baseItemIdがある場合はアイテムにセット効果を上乗せ。重複しない。
					mainItem.setArmorSets(null);
					if (armor.getItemId() != armorSets.getEffectItemId()) {
						// 装備したものと別の装備に効果が付与する場合は効果を意図的に削除する。
						armorSets.removeEffect(_owner);
					}
					_owner.getInventory().updateItem(mainItem);
				}
				armorSets.removePolyEffect(_owner);
			}
		}
	}

	private void removeArmor(L1ItemInstance armor) {
		L1Item item = armor.getItem();
		int itemId = armor.getItem().getItemId();

		if (armor.getItem().getType() < 10) { // 防具
			_owner.addAc(-(item.getAc() + armor.getAc() - armor.getEnchantLevel() - armor.getAcByMagic()));
		} else if (armor.getItem().getType() >= 10
				&& armor.getItem().getType() <= 18) { // アクセサリー、文様、タリスマン
			_owner.addAc(-(item.getAc() + armor.getAc() - armor.getAcByMagic()));
		}
		_owner.addDamageReductionByArmor(-(item.getDamageReduction() + armor.getDamageReduction()));
		_owner.addWeightReduction(-(item.getWeightReduction() + armor.getWeightReduction()));
		_owner.addHitModifierByArmor(-(item.getHitModifierByArmor() + armor.getHitModifier()));
		_owner.addDmgModifierByArmor(-(item.getDmgModifierByArmor() + armor.getDmgModifier()));
		_owner.addBowHitModifierByArmor(-(item.getBowHitModifierByArmor() + armor.getBowHitModifier()));
		_owner.addBowDmgModifierByArmor(-(item.getBowDmgModifierByArmor() + armor.getBowDmgModifier()));
		armor.stopChargeTimer();

		if (itemId == 20077 || itemId == 20062 || itemId == 120077) {
			_owner.delInvis(); // インビジビリティ状態解除
		}
		if (itemId == 20288) { // ROTC
			_owner.sendPackets(new S_Ability(1, false));
		}
		if (itemId >= 21176 && itemId <= 21179) { // パプリオンハイドロシリーズ
			_owner.removeSkillEffect(DIVINE_PROTECTION_OF_FAFURION);
			_owner.stopRegularlyEffect(DIVINE_PROTECTION_OF_FAFURION);
		}
		if (itemId == 21180) { // リンドビオルストームプレートメイル
			_owner.removeSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_PLATE);
			_owner.stopRegularlyEffect(DIVINE_PROTECTION_OF_LINDVIOR_PLATE);
		}
		if (itemId == 21181) { // リンドビオルストームローブ
			_owner.removeSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_ROBE);
			_owner.stopRegularlyEffect(DIVINE_PROTECTION_OF_LINDVIOR_ROBE);
		}
		if (itemId == 21182) { // リンドビオルストームレザーアーマー
			_owner.removeSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_LEATHER);
			_owner.stopRegularlyEffect(DIVINE_PROTECTION_OF_LINDVIOR_LEATHER);
		}
		if (itemId == 21183) { // リンドビオルストームスケイルメイル
			_owner.removeSkillEffect(DIVINE_PROTECTION_OF_LINDVIOR_SCALE);
			_owner.stopRegularlyEffect(DIVINE_PROTECTION_OF_LINDVIOR_SCALE);
		}
		if (itemId == 21337) { // 反逆者のシールド
			_owner.removeSkillEffect(SHIELD_OF_TEAIROR);
			_owner.setTeairorChance(0);
		}
		if (itemId == 21355) { // ドラゴニックハロウィンアーマー
			_owner.removeSkillEffect(DIVINE_PROTECTION_OF_HALLOWEEN);
			_owner.stopRegularlyEffect(DIVINE_PROTECTION_OF_HALLOWEEN);
		}
		if (itemId == 21352) { // ルームティス レッド イアリング
			_owner.removeSkillEffect(EARRING_OF_ROOMTIS_RED);
			_owner.setRedEarringChance(0);
		}
		_armors.remove(armor);
	}

	public void set(L1ItemInstance equipment) {
		L1Item item = equipment.getItem();
		if (item.getType2() == 0) {
			return;
		} else if (item.getType2() == 2) { // 防具
			setArmorSets(equipment);
		}

		_owner.addStr(item.getStr() + equipment.getStr());
		_owner.addCon(item.getCon() + equipment.getCon());
		_owner.addDex(item.getDex() + equipment.getDex());
		_owner.addInt(item.getInt() + equipment.getInt());
		_owner.addWis(item.getWis() + equipment.getWis());
		if (item.getWis() != 0) {
			_owner.resetBaseMr();
		}
		_owner.addCha(item.getCha() + equipment.getCha());

		_owner.addMaxHp(equipment.getHp());
		_owner.addMaxMp(equipment.getMp());
		_owner.addHpr(equipment.getHpr());
		_owner.addMpr(equipment.getMpr());

		int addMr = 0;
		addMr += equipment.getMr();
		if (item.getItemId() == 20236 && _owner.isElf()) {
			addMr += 5;
		}

		_owner.addMr(addMr);
		_owner.sendPackets(new S_SpMr(_owner));
		_owner.addSp(equipment.getSp());
		_owner.sendPackets(new S_SpMr(_owner));

		_owner.addEarth(equipment.getDefenseEarth());
		_owner.addWind(equipment.getDefenseWind());
		_owner.addWater(equipment.getDefenseWater());
		_owner.addFire(equipment.getDefenseFire());

		_owner.addResistStun(item.getResistStun() + equipment.getResistStun());
		_owner.addResistStone(item.getResistStone() + equipment.getResistStone());
		_owner.addResistSleep(item.getResistSleep() + equipment.getResistSleep());
		_owner.addResistFreeze(item.getResistFreeze() + equipment.getResistFreeze());
		_owner.addResistHold(item.getResistHold() + equipment.getResistHold());
		_owner.addResistBlind(item.getResistBlind() + equipment.getResistBlind());

		_owner.addExpBonusPct(item.getExpBonus() + equipment.getExpBonus());
		_owner.addPotionRecoveryRatePct(item.getPotionRecoveryRate() + equipment.getPotionRecoveryRate());

		if (item.isHaste()) {
			_owner.addHasteItemEquipped(1);
			_owner.removeHasteSkillEffect();
			if (_owner.getMoveSpeed() != 1) {
				_owner.setMoveSpeed(1);
				_owner.sendPackets(new S_SkillHaste(_owner.getId(), 1, -1));
				_owner.broadcastPacket(new S_SkillHaste(_owner.getId(), 1, 0));
			}
		}
		if (item.getItemId() == 20383) { // 騎馬用ヘルム
			if (_owner.hasSkillEffect(STATUS_BRAVE)) {
				_owner.killSkillEffectTimer(STATUS_BRAVE);
				_owner.sendPackets(new S_SkillBrave(_owner.getId(), 0, 0));
				_owner.broadcastPacket(new S_SkillBrave(_owner.getId(), 0, 0));
				_owner.setBraveSpeed(0);
			}
		}
		_owner.getEquipSlot().setMagicHelm(equipment);

		if (item.getType2() == 1) {
			setWeapon(equipment);
		} else if (item.getType2() == 2) {
			setArmor(equipment);
			_owner.sendPackets(new S_SpMr(_owner));
		}
	}

	public void remove(L1ItemInstance equipment) {
		L1Item item = equipment.getItem();
		if (item.getType2() == 0) {
			return;
		}

		_owner.addStr(-(item.getStr() + equipment.getStr()));
		_owner.addCon(-(item.getCon() + equipment.getCon()));
		_owner.addDex(-(item.getDex() + equipment.getDex()));
		_owner.addInt(-(item.getInt() + equipment.getInt()));
		_owner.addWis(-(item.getWis() + equipment.getWis()));
		if (item.getWis() != 0) {
			_owner.resetBaseMr();
		}
		_owner.addCha(-(item.getCha() + equipment.getCha()));

		_owner.addMaxHp(-(equipment.getHp()));
		_owner.addMaxMp(-(equipment.getMp()));
		_owner.addHpr(-(equipment.getHpr()));
		_owner.addMpr(-(equipment.getMpr()));

		int addMr = 0;
		addMr -= equipment.getMr();
		if (item.getItemId() == 20236 && _owner.isElf()) {
			addMr -= 5;
		}

		_owner.addMr(addMr);
		_owner.sendPackets(new S_SpMr(_owner));
		_owner.addSp(-(equipment.getSp()));
		_owner.sendPackets(new S_SpMr(_owner));

		_owner.addEarth(-(equipment.getDefenseEarth()));
		_owner.addWind(-(equipment.getDefenseWind()));
		_owner.addWater(-(equipment.getDefenseWater()));
		_owner.addFire(-(equipment.getDefenseFire()));

		_owner.addResistStun(-(item.getResistStun() + equipment.getResistStun()));
		_owner.addResistStone(-(item.getResistStone() + equipment.getResistStone()));
		_owner.addResistSleep(-(item.getResistSleep() + equipment.getResistSleep()));
		_owner.addResistFreeze(-(item.getResistFreeze() + equipment.getResistFreeze()));
		_owner.addResistHold(-(item.getResistHold() + equipment.getResistHold()));
		_owner.addResistBlind(-(item.getResistBlind() + equipment.getResistBlind()));

		_owner.addExpBonusPct(-(item.getExpBonus() + equipment.getExpBonus()));
		_owner.addPotionRecoveryRatePct(-(item.getPotionRecoveryRate() + equipment.getPotionRecoveryRate()));

		if (item.isHaste()) {
			_owner.addHasteItemEquipped(-1);
			if (_owner.getHasteItemEquipped() == 0) {
				_owner.setMoveSpeed(0);
				_owner.sendPackets(new S_SkillHaste(_owner.getId(), 0, 0));
				_owner.broadcastPacket(new S_SkillHaste(_owner.getId(), 0, 0));
			}
		}
		_owner.getEquipSlot().removeMagicHelm(_owner.getId(), equipment);

		if (item.getType2() == 1) {
			removeWeapon(equipment);
		} else if (item.getType2() == 2) {
			removeArmor(equipment);
			removeArmorSets(equipment);
		}
	}

	public void setMagicHelm(L1ItemInstance item) {
		switch (item.getItemId()) {
		case 20013:
			_owner.setSkillMastery(PHYSICAL_ENCHANT_DEX);
			_owner.setSkillMastery(HASTE);
			_owner.sendPackets(new S_AddSkill(0, 0, 0, 2, 0, 4, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			break;
		case 20014:
			_owner.setSkillMastery(HEAL);
			_owner.setSkillMastery(EXTRA_HEAL);
			_owner.sendPackets(new S_AddSkill(1, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			break;
		case 20015:
			_owner.setSkillMastery(ENCHANT_WEAPON);
			_owner.setSkillMastery(DETECTION);
			_owner.setSkillMastery(PHYSICAL_ENCHANT_STR);
			_owner.sendPackets(new S_AddSkill(0, 24, 0, 0, 0, 2, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			break;
		case 20008:
			_owner.setSkillMastery(HASTE);
			_owner.sendPackets(new S_AddSkill(0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			break;
		case 20023:
			_owner.setSkillMastery(GREATER_HASTE);
			_owner.sendPackets(new S_AddSkill(0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			break;
		}
	}

	public void removeMagicHelm(int objectId, L1ItemInstance item) {
		switch (item.getItemId()) {
		case 20013: // 魔法のヘルム：迅速
			if (!SkillTable.getInstance().spellCheck(objectId,
					PHYSICAL_ENCHANT_DEX)) {
				_owner.removeSkillMastery(PHYSICAL_ENCHANT_DEX);
				_owner.sendPackets(new S_DelSkill(0, 0, 0, 2, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			if (!SkillTable.getInstance().spellCheck(objectId, HASTE)) {
				_owner.removeSkillMastery(HASTE);
				_owner.sendPackets(new S_DelSkill(0, 0, 0, 0, 0, 4, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			break;
		case 20014: // 魔法のヘルム：治癒
			if (!SkillTable.getInstance().spellCheck(objectId, HEAL)) {
				_owner.removeSkillMastery(HEAL);
				_owner.sendPackets(new S_DelSkill(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			if (!SkillTable.getInstance().spellCheck(objectId, EXTRA_HEAL)) {
				_owner.removeSkillMastery(EXTRA_HEAL);
				_owner.sendPackets(new S_DelSkill(0, 0, 4, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			break;
		case 20015: // 魔法のヘルム：力
			if (!SkillTable.getInstance().spellCheck(objectId, ENCHANT_WEAPON)) {
				_owner.removeSkillMastery(ENCHANT_WEAPON);
				_owner.sendPackets(new S_DelSkill(0, 8, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			if (!SkillTable.getInstance().spellCheck(objectId, DETECTION)) {
				_owner.removeSkillMastery(DETECTION);
				_owner
				.sendPackets(new S_DelSkill(0, 16, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0));
			}
			if (!SkillTable.getInstance().spellCheck(objectId,
					PHYSICAL_ENCHANT_STR)) {
				_owner.removeSkillMastery(PHYSICAL_ENCHANT_STR);
				_owner.sendPackets(new S_DelSkill(0, 0, 0, 0, 0, 2, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			break;
		case 20008: // マイナーウィンドヘルム
			if (!SkillTable.getInstance().spellCheck(objectId, HASTE)) {
				_owner.removeSkillMastery(HASTE);
				_owner.sendPackets(new S_DelSkill(0, 0, 0, 0, 0, 4, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			break;
		case 20023: // ウィンドヘルム
			if (!SkillTable.getInstance().spellCheck(objectId, GREATER_HASTE)) {
				_owner.removeSkillMastery(GREATER_HASTE);
				_owner
				.sendPackets(new S_DelSkill(0, 0, 0, 0, 0, 0, 32, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0));
			}
			break;
		}
	}

}
