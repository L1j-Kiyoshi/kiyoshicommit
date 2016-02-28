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
package jp.l1j.server.model.item.executor;

import static jp.l1j.locale.I18N.*;
import static jp.l1j.server.model.item.L1ItemId.*;
import jp.l1j.configure.Config;
import jp.l1j.server.datatables.EnchantLogTable;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.inventory.L1PcInventory;
import jp.l1j.server.model.item.L1ItemId;
import jp.l1j.server.packets.server.S_OwnCharStatus;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.packets.server.S_SpMr;
import jp.l1j.server.packets.server.S_SystemMessage;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1Armor;

public class L1EnchantScroll {

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	/** enchant_scrolls の値が-1のアイテムを強化できる強化スクロールのItemIDの配列 */
	private static final int[] NORMAL_SCROLL_OF_ENCHANT_WEAPON
									= { SCROLL_OF_ENCHANT_WEAPON, B_SCROLL_OF_ENCHANT_WEAPON, C_SCROLL_OF_ENCHANT_WEAPON,
										A_SCROLL_OF_ENCHANT_WEAPON, ALCHEMYSCROLL_OF_ANCIENTS, B_SCROLL_OF_KARBAS,
										SCROLL_OF_KARBAS };

	/** enchant_scrolls の値が-1のアイテムを強化できる強化スクロールのItemIDの配列 */
	private static final int[] NORMAL_SCROLL_OF_ENCHANT_ARMOR
									= { SCROLL_OF_ENCHANT_ARMOR, B_SCROLL_OF_ENCHANT_ARMOR, C_SCROLL_OF_ENCHANT_ARMOR,
										MAGICSCROLL_OF_ANCIENTS, SCROLL_OF_GIAN, B_SCROLL_OF_GIAN };

	private static L1EnchantScroll _instance;

	public static L1EnchantScroll getInstance() {
		if (_instance == null) {
			_instance = new L1EnchantScroll();
		}

		return _instance;
	}

	public boolean use(L1PcInstance pc, L1ItemInstance item, L1ItemInstance target) {
		boolean result = false;

		int itemId = item.getItemId();
		int use_type = item.getItem().getUseType();
		if (target.getItem().getType2()== 1) { // 武器
			if (itemId >= 41429 && itemId <= 41432) { // 属性武器強化スクロール各種
				result = enchantAttrWeapon(pc, item, target);
			} else if (use_type == 26) { // 武器強化スクロール
				result = enchantWeapon(pc, item, target);
			}
		} else if (target.getItem().getType2()== 2) { // 防具
			if (1 <= target.getItem().getType() && target.getItem().getType() <= 8) {
				if (itemId == 40075) { // 防具破壊スクロール
					result = destroyArmor(pc, item, target);
				} else if (itemId == 42502) { // 血戦の防具強化スクロール
					result = enchantBloodBathArmor(pc, item, target);
				} else if (use_type == 27) { // 防具強化スクロール
					result = enchantArmor(pc, item, target);
				}
			} else if (10 <= target.getItem().getType() && target.getItem().getType() <= 13) { // 装飾品
				if (itemId == 50671) { // ルームティスの強化スクロール
					result = enchantRoomtis(pc, item, target);

				} else if (10 <= target.getItem().getType() && target.getItem().getType() <= 13) {
					if (itemId == 60005) { // オリムの装飾品魔法スクロール
						result = enchantAccessoryOfOrim(pc, item, target);

				} else if (10 <= target.getItem().getType() && target.getItem().getType() <= 13) {
					if (itemId == 160005) { // 祝福されたオリムの装飾品魔法スクロール
							result = enchantAccessoryOfBlessOrim(pc, item, target);
				} else {
					result = enchantAccessory(pc, item, target);
						}
					}
				}
			}
		}
		return result;
	}

	private boolean destroyArmor(L1PcInstance pc, L1ItemInstance item, L1ItemInstance target) {
		if (target.getItem().getType2() == 2) {
			int msg = 0;
			switch (target.getItem().getType()) {
			case 1: // helm
				msg = 171; // \f1ヘルムが塵になり、風に飛んでいきます。
				break;
			case 2: // armor
				msg = 169; // \f1アーマーが壊れ、下に落ちました。
				break;
			case 3: // T
				msg = 170; // \f1シャツが細かい糸になり、破けて落ちました。
				break;
			case 4: // cloak
				msg = 168; // \f1マントが破れ、塵になりました。
				break;
			case 5: // glove
				msg = 172; // \f1グローブが消えました。
				break;
			case 6: // boots
				msg = 173; // \f1靴がバラバラになりました。
				break;
			case 7: // shield
				msg = 174; // \f1シールドが壊れました。
				break;
			default:
				msg = 167; // \f1肌がムズムズします。
				break;
			}
			pc.sendPackets(new S_ServerMessage(msg));
			pc.getInventory().removeItem(target, 1);
			pc.getInventory().saveItem(target);
		} else {
			pc.sendPackets(new S_ServerMessage(154)); // \f1スクロールが散らばります。
			pc.getInventory().saveItem(target);
		}

		return true;
	}

	private boolean enchantWeapon(L1PcInstance pc, L1ItemInstance item, L1ItemInstance target) {
		int itemId = item.getItemId();

		if (target == null || target.getItem().getType2() != 1) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int safe_enchant = target.getItem().getSafeEnchant();
		if (safe_enchant < 0) { // 強化不可
			pc.sendPackets(new S_ServerMessage(1453)); // \f1これ以上は強化できません。
			return false;
		}

		if (target.isSealed()) { // 封印された装備強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		boolean isScrollId = false;
		for (int dedicatedScrollId : target.getItem().getEnchantScrolls()) {
			if (itemId == dedicatedScrollId) {
				isScrollId = true;
				break;
			}
			if (dedicatedScrollId == -1) {
				for (int enchantScrollId : NORMAL_SCROLL_OF_ENCHANT_WEAPON) {
					if (itemId == enchantScrollId) {
						isScrollId = true;
						break;
					}
				}
			}
		}

		if (!isScrollId) { // 特定のアイテムでしか強化不可能
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int enchant_level = target.getEnchantLevel();

		if (itemId == C_SCROLL_OF_ENCHANT_WEAPON) { // c-dai
			if (enchant_level <= -6) {
				// -7以上はできない。
				failureEnchant(pc, target);
				pc.getInventory().saveItem(target);
			} else {
				successEnchant(pc, target, -1);
				pc.getInventory().saveItem(target);
			}
		} else if (enchant_level < safe_enchant && safe_enchant > 0) {
			successEnchant(pc, target, randomLevel(target, itemId));
			pc.getInventory().saveItem(target);
		} else {
			int rnd = _random.nextInt(100) + 1;
			int enchant_chance_wepon;
			if (enchant_level >= 9) {
				enchant_chance_wepon = 100 / 6;
			} else {
				enchant_chance_wepon = 100 / 3;
			}

			// 武具固有のOEレート
			enchant_chance_wepon = (int) (enchant_chance_wepon * item.getItem().getOverEnchantRate());

			// サーバーの固有OEレート(+5%なら5%UP)
			enchant_chance_wepon += Config.ENCHANT_CHANCE_WEAPON;

			// +9以上の場合
			if (enchant_level >= 9) {
				if (rnd < enchant_chance_wepon ||
						(rnd < (enchant_chance_wepon * 2) && itemId == A_SCROLL_OF_ENCHANT_WEAPON)) {
					int randomEnchantLevel = randomLevel(target, itemId);
					successEnchant(pc, target, randomEnchantLevel);
					pc.getInventory().saveItem(target);
				} else if (rnd < (enchant_chance_wepon * 2)) {
					// \f1%0が%2と強烈に%1光りましたが、幸い無事にすみました。
					pc.sendPackets(new S_ServerMessage(160, target.getLogName(), "$245", "$248"));
				} else {
					if (target.isProtected()) { // 保護中
						protectEnchant(pc, item, target);
						pc.getInventory().saveItem(target);
					} else { // 失敗
						failureEnchant(pc, target);
						pc.getInventory().deleteItem(target);
					}
				}
			} else if (rnd < enchant_chance_wepon) { // 1/3で成功。
				int randomEnchantLevel = randomLevel(target, itemId);
				successEnchant(pc, target, randomEnchantLevel);
				pc.getInventory().saveItem(target);
			} else {
				if (target.isProtected()) { // 保護中
					protectEnchant(pc, item, target);
					pc.getInventory().saveItem(target);
				} else { // 失敗
					failureEnchant(pc, target);
					pc.getInventory().deleteItem(target);
				}
			}
		}
		return true;
	}

	private boolean enchantBloodBathArmor(L1PcInstance pc, L1ItemInstance item, L1ItemInstance target) {

		if (target == null
				|| target.getItem().getType2() != 2
				|| target.getItem().getType() >= 10) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		if (target.isSealed()) { // 封印された装備強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		if (target.getItemId() < 23000 || target.getItemId() >= 23090) { // 血戦防具以外
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int chance = ((target.getItemId() % 100) % 10) + 2;

		if (chance >= 11) {
			pc.sendPackets(new S_ServerMessage(1453)); // \f1これ以上は強化できません。
			return false;
		}

		if (_random.nextInt(chance) == 0) { // 成功
			pc.getInventory().removeItem(target);
			L1ItemInstance createItem = pc.getInventory().storeItem(target.getItemId() + 1, 1);
			createItem.setIdentified(true);
			pc.sendPackets(new S_ServerMessage(403, createItem.getName())); // \f1%0を手に入れました。
			return true;
		} else if ((_random.nextInt(1000) + 1) <= Config.ENCHANT_CHANCE_ARMOR) {
			pc.getInventory().removeItem(target);
			pc.getInventory().storeItem(target.getItemId() + 1, 1);
			return true;
		} else {
			pc.getInventory().removeItem(item, 1);
			pc.sendPackets(new S_ServerMessage(3658)); // \f1アイテム合成に失敗しました。
			return false;
		}
	}

	private boolean enchantArmor(L1PcInstance pc, L1ItemInstance item, L1ItemInstance target) {
		int itemId = item.getItemId();

		if (target == null
				|| target.getItem().getType2() != 2
				|| target.getItem().getType() >= 10) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int safe_enchant = ((L1Armor) target.getItem()).getSafeEnchant();
		if (safe_enchant < 0) { // 強化不可
			pc.sendPackets(new S_ServerMessage(1453)); // \f1これ以上は強化できません。
			return false;
		}

		if (target.isSealed()) { // 封印された装備強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		boolean isScrollId = false;
		for (int dedicatedScrollId : target.getItem().getEnchantScrolls()) {
			if (itemId == dedicatedScrollId) {
				isScrollId = true;
				break;
			}
			if (dedicatedScrollId == -1) {
				for (int scrollId : NORMAL_SCROLL_OF_ENCHANT_ARMOR) {
					if (itemId == scrollId) {
						isScrollId = true;
						break;
					}
				}
			}
		}

		if (!isScrollId) { // 特定のアイテムでしか強化不可能
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int enchant_level = target.getEnchantLevel();
		if (itemId == L1ItemId.C_SCROLL_OF_ENCHANT_ARMOR) { // c-zel
			if (enchant_level <= -6) {
				// -7以上はできない。
				failureEnchant(pc, target);
				pc.getInventory().deleteItem(target);
			} else {
				successEnchant(pc, target, -1);
				pc.getInventory().saveItem(target);
			}
		} else if (enchant_level < safe_enchant) {
			successEnchant(pc, target, randomLevel(target, itemId));
			pc.getInventory().saveItem(target);
		} else {
			int rnd = _random.nextInt(100) + 1;
			int enchant_chance_armor;
			int enchant_level_tmp;
			if (safe_enchant == 0) { // 骨、ブラックミスリル用補正
				enchant_level_tmp = enchant_level + 2;
			} else {
				enchant_level_tmp = enchant_level;
			}

			enchant_chance_armor = enchant_level / enchant_level_tmp;

			// 防具固有のOEレート
			enchant_chance_armor = (int) (enchant_chance_armor * target.getItem().getOverEnchantRate());

			// サーバー全体のOEレート率（足し算）
			enchant_chance_armor += Config.ENCHANT_CHANCE_ARMOR;

			if (rnd < enchant_chance_armor) {
				int randomEnchantLevel = randomLevel(target, itemId);
				successEnchant(pc, target, randomEnchantLevel);
				pc.getInventory().saveItem(target);
			} else if (enchant_level >= 9
					&& rnd < (enchant_chance_armor * 2)) {
				String item_name_id = target.getName();
				String pm = "";
				String msg = "";
				if (enchant_level > 0) {
					pm = "+";
				}
				msg = (new StringBuilder()).append(pm + enchant_level)
						.append(" ").append(item_name_id).toString();
				// \f1%0が%2と強烈に%1光りましたが、幸い無事にすみました。
				pc.sendPackets(new S_ServerMessage(160, msg, "$252",
						"$248"));
			} else {
				if (target.isProtected()) { // 保護中
					protectEnchant(pc, item, target);
					pc.getInventory().saveItem(target);
				} else { // 失敗
					failureEnchant(pc, target);
					pc.getInventory().deleteItem(target);
				}
			}
		}
		return true;
	}

	private boolean enchantAttrWeapon(L1PcInstance pc, L1ItemInstance item, L1ItemInstance target) {
		int itemId = item.getItemId();

		if (target == null
				|| target.getItem().getType2() != 1) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int safeEnchant = target.getItem().getSafeEnchant();
		if (safeEnchant < 0) { // 強化不可
			pc.sendPackets(new S_ServerMessage(1453)); // \f1これ以上は強化できません。
			return false;
		}

		if (target.isSealed()) { // 封印された装備強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		// 0:無属性 1:地 2:火 4:水 8:風
		int oldAttrEnchantKind = target.getAttrEnchantKind();
		int oldAttrEnchantLevel = target.getAttrEnchantLevel();
		int enchantLevel = target.getEnchantLevel();

		boolean isSameAttr = false; // スクロールと強化済みの属性が同一か
		if (itemId == 41429 && oldAttrEnchantKind == 8
				|| itemId == 41430 && oldAttrEnchantKind == 1
				|| itemId == 41431 && oldAttrEnchantKind == 4
				|| itemId == 41432 && oldAttrEnchantKind == 2) { // 同じ属性
			isSameAttr = true;
		}
		if (isSameAttr && oldAttrEnchantLevel == 3 && enchantLevel <= 9) {
			pc.sendPackets(new S_ServerMessage(1453)); // これ以上は強化できません。
			return false;
		} else if (isSameAttr && oldAttrEnchantLevel == 4 && enchantLevel <= 10) {
			pc.sendPackets(new S_ServerMessage(1453)); // これ以上は強化できません。
			return false;
		} else if (isSameAttr && oldAttrEnchantLevel >= 5) {
			pc.sendPackets(new S_ServerMessage(1453)); // これ以上は強化できません。
			return false;
		}

		int rnd = _random.nextInt(100) + 1;
		if (Config.ATTR_ENCHANT_CHANCE >= rnd) {
			pc.sendPackets(new S_ServerMessage(1410, target
					.getLogName(), "$245", "$247")); // f1%0に強力な魔法の力が染み入ります。
			int newAttrEnchantKind = 0;
			int newAttrEnchantLevel = 0;
			if (isSameAttr) { // 同じ属性なら+1
				newAttrEnchantLevel = oldAttrEnchantLevel + 1;
			} else { // 異なる属性なら1
				newAttrEnchantLevel = 1;
			}
			if (itemId == 41429) { // 風の武器強化スクロール
				newAttrEnchantKind = 8;
			} else if (itemId == 41430) { // 地の武器強化スクロール
				newAttrEnchantKind = 1;
			} else if (itemId == 41431) { // 水の武器強化スクロール
				newAttrEnchantKind = 4;
			} else if (itemId == 41432) { // 火の武器強化スクロール
				newAttrEnchantKind = 2;
			}
			target.setAttrEnchantKind(newAttrEnchantKind);
			pc.getInventory().updateItem(target,
					L1PcInventory.COL_ATTR_ENCHANT_KIND);
			pc.getInventory().saveItem(target);
			target.setAttrEnchantLevel(newAttrEnchantLevel);
			pc.getInventory().updateItem(target,
					L1PcInventory.COL_ATTR_ENCHANT_LEVEL);
			pc.getInventory().saveItem(target);
		} else {
			pc.sendPackets(new S_ServerMessage(1411, target.getLogName(), "$245", "$247")); // f1%0に魔法が入り込めません。
		}
		return true;
	}

	private boolean enchantAccessory(L1PcInstance pc, L1ItemInstance item, L1ItemInstance target) {
		int itemId = item.getItemId();
		if (target == null
				|| target.getItem().getType2() != 2
				|| target.getItem().getType() < 10
				|| target.getItem().getType() > 13) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int safe_enchant = target.getItem().getSafeEnchant();
		if (safe_enchant < 0) { // 強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		if (target.isSealed()) { // 封印された装備強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int enchant_level = target.getEnchantLevel();
		if (enchant_level >= safe_enchant || enchant_level >= 10) { // 強化上限 はsafe_enchant値で設定
			pc.sendPackets(new S_ServerMessage(1453)); // \f1これ以上は強化できません。
			return false;
		}

		boolean isScrollId = false;
		for (int dedicatedScrollId : target.getItem().getEnchantScrolls()) {
			if (itemId == dedicatedScrollId) {
				isScrollId = true;
				break;
			}
			if (dedicatedScrollId == -1) { // -1の場合は装飾品強化で強化可能
				if (itemId == 49148 || itemId == 60005 || itemId == 160005) {
					isScrollId = true;
					break;
				}
			}
		}

		if (!isScrollId) { // 特定のアイテムでしか強化不可能
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int rnd = _random.nextInt(100) + 1;
		int enchant_chance_accessory;
		if (enchant_level >= 5) {
			enchant_chance_accessory = 100 / 6;
		} else {
			enchant_chance_accessory = 100 / 3;
		}

		// このアクセサリーの固有倍率
		enchant_chance_accessory = (int) (enchant_chance_accessory * target.getItem().getOverEnchantRate());

		// サーバーで決められた固定成功率（+%)
		enchant_chance_accessory += Config.ENCHANT_CHANCE_ACCESSORY;

		if (rnd < enchant_chance_accessory) { // 成功
			successEnchant(pc, target, 1);
			pc.getInventory().saveItem(target);
		} else if (target.isProtected()) { // 保護中
			protectEnchant(pc, item, target);
			pc.getInventory().saveItem(target);
		} else { // 失敗
			failureEnchant(pc, target);
			pc.getInventory().deleteItem(target);
		}
		return true;
	}

	private boolean enchantAccessoryOfOrim(L1PcInstance pc, L1ItemInstance item, L1ItemInstance target) {
		int itemId = item.getItemId();
		if (target == null
				|| target.getItem().getType2() != 2
				|| target.getItem().getType() < 10
				|| target.getItem().getType() > 13) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int safe_enchant = target.getItem().getSafeEnchant();
		if (safe_enchant < 0) { // 強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		if (target.isSealed()) { // 封印された装備強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int enchant_level = target.getEnchantLevel();
		if (enchant_level >= safe_enchant || enchant_level >= 10) { // 強化上限 はsafe_enchant値で設定
			pc.sendPackets(new S_ServerMessage(1453)); // \f1これ以上は強化できません。
			return false;
		}

		boolean isScrollId = false;
		for (int dedicatedScrollId : target.getItem().getEnchantScrolls()) {
			if (itemId == dedicatedScrollId) {
				isScrollId = true;
				break;
			}
			if (dedicatedScrollId == -1) { // -1の場合は装飾品強化で強化可能
				if (itemId == 49148 || itemId == 60005 || itemId == 160005) {
					isScrollId = true;
					break;
				}
			}
		}

		if (!isScrollId) { // 特定のアイテムでしか強化不可能
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int rnd = _random.nextInt(100) + 1;
		int enchant_chance_accessory;
		if (enchant_level == 0) {
			enchant_chance_accessory = 70;
		} else if (enchant_level == 1){
			enchant_chance_accessory = 60;
		} else if (enchant_level == 2){
			enchant_chance_accessory = 60;
		} else if (enchant_level == 3){
			enchant_chance_accessory = 50;
		} else if (enchant_level == 4){
			enchant_chance_accessory = 25;
		} else if (enchant_level == 5){
			enchant_chance_accessory = 5;
		} else if (enchant_level == 6){
			enchant_chance_accessory = 4;
		} else {
			enchant_chance_accessory = 3;
		}

		// このアクセサリーの固有倍率
		enchant_chance_accessory = (int) (enchant_chance_accessory * target.getItem().getOverEnchantRate());

		// サーバーで決められた固定成功率（+%)
		enchant_chance_accessory += Config.ENCHANT_CHANCE_ACCESSORY;

		if (rnd < enchant_chance_accessory) { // 成功
			successEnchant(pc, target, 1);
			pc.getInventory().saveItem(target);
		} else if (target.isProtected()) { // 保護中
			protectEnchant(pc, item, target);
			pc.getInventory().saveItem(target);

		} else if (rnd < (enchant_chance_accessory * 1.3)) { //維持
			// \f1%0が%2と強烈に%1光りましたが、幸い無事にすみました。
			pc.sendPackets(new S_ServerMessage(160, target.getLogName(), "$245", "$248"));
		} else if (rnd > enchant_chance_accessory && (target.getEnchantLevel() == 0)) {
			// \f1%0が%2と強烈に%1光りましたが、幸い無事にすみました。
			pc.sendPackets(new S_ServerMessage(160, target.getLogName(), "$245", "$248"));
		} else { // 失敗
			successEnchant(pc, target, -1);
		}
		return true;
	}


	private boolean enchantAccessoryOfBlessOrim(L1PcInstance pc, L1ItemInstance item, L1ItemInstance target) {
		int itemId = item.getItemId();
		if (target == null
				|| target.getItem().getType2() != 2
				|| target.getItem().getType() < 10
				|| target.getItem().getType() > 13) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int safe_enchant = target.getItem().getSafeEnchant();
		if (safe_enchant < 0) { // 強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		if (target.isSealed()) { // 封印された装備強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int enchant_level = target.getEnchantLevel();
		if (enchant_level >= safe_enchant || enchant_level >= 10) { // 強化上限 はsafe_enchant値で設定
			pc.sendPackets(new S_ServerMessage(1453)); // \f1これ以上は強化できません。
			return false;
		}

		boolean isScrollId = false;
		for (int dedicatedScrollId : target.getItem().getEnchantScrolls()) {
			if (itemId == dedicatedScrollId) {
				isScrollId = true;
				break;
			}
			if (dedicatedScrollId == -1) { // -1の場合は装飾品強化で強化可能
				if (itemId == 49148 || itemId == 60005 || itemId == 160005) {
					isScrollId = true;
					break;
				}
			}
		}

		if (!isScrollId) { // 特定のアイテムでしか強化不可能
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}
		int rnd = _random.nextInt(100) + 1;
		int enchant_chance_accessory;
		if (enchant_level == 0) {
			enchant_chance_accessory = 20;
		} else if (enchant_level == 1){
			enchant_chance_accessory = 20;
		} else if (enchant_level == 2){
			enchant_chance_accessory = 20;
		} else if (enchant_level == 3){
			enchant_chance_accessory = 10;
		} else if (enchant_level == 4){
			enchant_chance_accessory = 5;
		} else if (enchant_level == 5){
			enchant_chance_accessory = 3;
		} else if (enchant_level == 6){
			enchant_chance_accessory = 3;
		} else {
			enchant_chance_accessory = 2;
		}

		// このアクセサリーの固有倍率
		enchant_chance_accessory = (int) (enchant_chance_accessory * target.getItem().getOverEnchantRate());

		// サーバーで決められた固定成功率（+%)
		enchant_chance_accessory += Config.ENCHANT_CHANCE_ACCESSORY;

		if (rnd < enchant_chance_accessory) { // 成功
			successEnchant(pc, target, 1);
			pc.getInventory().saveItem(target);
		} else if (target.isProtected()) { // 保護中
			protectEnchant(pc, item, target);
			pc.getInventory().saveItem(target);
		} else { // 失敗
			//pc.getInventory().removeItem(item, 1);
			pc.sendPackets(new S_ServerMessage(4056, target.getLogName())); // \f1何も起きませんでした。
		}
		return true;
	}

	private boolean enchantRoomtis(L1PcInstance pc, L1ItemInstance item, L1ItemInstance target) {
		int itemId = item.getItemId();
		if (target == null
				|| target.getItem().getType2() != 2
				|| target.getItem().getType() < 10
				|| target.getItem().getType() > 13) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int safe_enchant = target.getItem().getSafeEnchant();
		if (safe_enchant < 0) { // 強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		if (target.isSealed()) { // 封印された装備強化不可
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int enchant_level = target.getEnchantLevel();
		if (enchant_level >= safe_enchant || enchant_level >= 10) { // 強化上限 はsafe_enchant値で設定
			pc.sendPackets(new S_ServerMessage(1453)); // \f1これ以上は強化できません。
			return false;
		}

		boolean isScrollId = false;
		for (int dedicatedScrollId : target.getItem().getEnchantScrolls()) {
			if (itemId == dedicatedScrollId) {
				isScrollId = true;
				break;
			}
			if (dedicatedScrollId == -1) { // -1の場合は装飾品強化で強化可能
				if (itemId == 49148 || itemId == 60005 || itemId == 160005) {
					isScrollId = true;
					break;
				}
			}
		}

		if (!isScrollId) { // 特定のアイテムでしか強化不可能
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return false;
		}

		int rnd = _random.nextInt(1000) + 1;
		int[] enchant_chance = { 900, 800, 650, 500, 350, 250, 175, 100 };

		if (rnd < enchant_chance[enchant_level]) { // 成功
			successEnchant(pc, target, 1);
			pc.getInventory().saveItem(target);
		} else if (target.isProtected()) { // 保護中
			protectEnchant(pc, item, target);
			pc.getInventory().saveItem(target);
		} else { // 失敗
			failureEnchant(pc, target);
			pc.getInventory().deleteItem(target);
		}
		return true;
	}

	private void successEnchant(L1PcInstance pc, L1ItemInstance item, int i) {
		String s = "";
		String sa = "";
		String sb = "";
		String s1 = item.getName();
		String pm = "";
		if (item.getEnchantLevel() > 0) {
			pm = "+";
		}
		if (item.getItem().getType2() == 1) {
			if (!item.isIdentified() || item.getEnchantLevel() == 0) {
				switch (i) {
				case -1:
					s = s1;
					sa = "$246";
					sb = "$247";
					break;

				case 1: // '\001'
					s = s1;
					sa = "$245";
					sb = "$247";
					break;

				case 2: // '\002'
					s = s1;
					sa = "$245";
					sb = "$248";
					break;

				case 3: // '\003'
					s = s1;
					sa = "$245";
					sb = "$248";
					break;
				}
			} else {
				switch (i) {
				case -1:
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1).toString();
					// \f1%0が%2%1光ります。
					sa = "$246";
					sb = "$247";
					break;

				case 1: // '\001'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1).toString();
					// \f1%0が%2%1光ります。
					sa = "$245";
					sb = "$247";
					break;

				case 2: // '\002'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1).toString();
					// \f1%0が%2%1光ります。
					sa = "$245";
					sb = "$248";
					break;

				case 3: // '\003'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1).toString();
					// \f1%0が%2%1光ります。
					sa = "$245";
					sb = "$248";
					break;
				}
			}
		} else if (item.getItem().getType2() == 2) {
			if (!item.isIdentified() || item.getEnchantLevel() == 0) {
				switch (i) {
				case -1:
					s = s1;
					sa = "$246";
					sb = "$247";
					break;

				case 1: // '\001'
					s = s1;
					sa = "$252";
					sb = "$247 ";
					break;

				case 2: // '\002'
					s = s1;
					sa = "$252";
					sb = "$248 ";
					break;

				case 3: // '\003'
					s = s1;
					sa = "$252";
					sb = "$248 ";
					break;
				}
			} else {
				switch (i) {
				case -1:
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1).toString();
					// \f1%0が%2%1光ります。
					sa = "$246";
					sb = "$247";
					break;

				case 1: // '\001'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1).toString();
					// \f1%0が%2%1光ります。
					sa = "$252";
					sb = "$247 ";
					break;

				case 2: // '\002'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1).toString();
					// \f1%0が%2%1光ります。
					sa = "$252";
					sb = "$248 ";
					break;

				case 3: // '\003'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1).toString();
					// \f1%0が%2%1光ります。
					sa = "$252";
					sb = "$248 ";
					break;
				}
			}
		}
		pc.sendPackets(new S_ServerMessage(161, s, sa, sb));

		int safe_enchant = item.getItem().getSafeEnchant();
		int oldEnchantLvl = item.getEnchantLevel();
		int newEnchantLvl = oldEnchantLvl + i;
		int grade = item.getItem().getGrade();

		item.setEnchantLevel(newEnchantLvl);
		item.setProtected(false);
		pc.getInventory().updateItem(item, L1PcInventory.COL_ENCHANTLVL);

		if (newEnchantLvl > safe_enchant) {
			pc.getInventory().saveItem(item);
		}

		if (item.getItem().getType2() == 1
				&& Config.LOGGING_WEAPON_ENCHANT != 0) {
			if (safe_enchant == 0
					|| newEnchantLvl >= Config.LOGGING_WEAPON_ENCHANT) {
				loggingEnchant(pc, item, oldEnchantLvl, newEnchantLvl);
			}
		}

		if (item.getItem().getType2() == 2 && item.getItem().getType() < 10
				&& Config.LOGGING_ARMOR_ENCHANT != 0) {
			if (safe_enchant == 0
					|| newEnchantLvl >= Config.LOGGING_ARMOR_ENCHANT) {
				loggingEnchant(pc, item, oldEnchantLvl, newEnchantLvl);
			}
		}

		if (item.getItem().getType2() == 2 && item.getItem().getType() >= 10
				&& item.getItem().getType() <= 13
				&& Config.LOGGING_ACCESSORY_ENCHANT != 0) {
			if (safe_enchant == 0
					|| newEnchantLvl >= Config.LOGGING_ACCESSORY_ENCHANT) {
				loggingEnchant(pc, item, oldEnchantLvl, newEnchantLvl);
			}
		}

		if (item.getItem().getType2() == 1
				&& Config.ANNOUNCE_WEAPON_ENCHANT != 0) {
			if (safe_enchant == 0
					|| newEnchantLvl >= Config.ANNOUNCE_WEAPON_ENCHANT) {
				announceEnchant(pc, item);
			}
		}

		if (item.getItem().getType2() == 2 && item.getItem().getType() < 10
				&& Config.ANNOUNCE_ARMOR_ENCHANT != 0) {
			if (safe_enchant == 0
					|| newEnchantLvl >= Config.ANNOUNCE_ARMOR_ENCHANT) {
				announceEnchant(pc, item);
			}
		}

		if (item.getItem().getType2() == 2 && item.getItem().getType() >= 10
				&& item.getItem().getType() <= 13
				&& Config.ANNOUNCE_ACCESSORY_ENCHANT != 0) {
			if (safe_enchant == 0
					|| newEnchantLvl >= Config.ANNOUNCE_ACCESSORY_ENCHANT) {
				announceEnchant(pc, item);
			}
		}

		if (item.isEquipped()) { // 装備中
			if (item.getItem().getType2() == 2
					&& item.getItem().getType() < 10) { // 防具
				pc.addAc(-i);
			}

			L1EnchantBonus bonusItem = L1EnchantBonus.get(item.getItem().getItemId());
			if (bonusItem != null) {
				pc.addAc(bonusItem.getAc(i));
				pc.addStr(bonusItem.getStr(i));
				pc.addDex(bonusItem.getDex(i));
				pc.addCon(bonusItem.getCon(i));
				pc.addInt(bonusItem.getInt(i));
				pc.addWis(bonusItem.getWis(i));
				pc.addCha(bonusItem.getCha(i));
				pc.addMaxHp(bonusItem.getHp(i));
				pc.addHpr(bonusItem.getHpr(i));
				pc.addMaxMp(bonusItem.getMp(i));
				pc.addMpr(bonusItem.getMpr(i));
				pc.addMr(bonusItem.getMr(i));
				pc.addSp(bonusItem.getSp(i));
				pc.addHitModifierByArmor(bonusItem.getHitModifier(i));
				pc.addDmgModifierByArmor(bonusItem.getDmgModifier(i));
				pc.addBowHitModifierByArmor(bonusItem.getBowHitModifier(i));
				pc.addBowDmgModifierByArmor(bonusItem.getBowDmgModifier(i));
				pc.addWeightReduction(bonusItem.getWeightReduction(i));
				pc.addDamageReductionByArmor(bonusItem.getDamageReduction(i));
				pc.addEarth(bonusItem.getDefenseEarth(i));
				pc.addWater(bonusItem.getDefenseWater(i));
				pc.addFire(bonusItem.getDefenseFire(i));
				pc.addWind(bonusItem.getDefenseWind(i));
				pc.addResistStun(bonusItem.getResistStun(i));
				pc.addResistStone(bonusItem.getResistStone(i));
				pc.addResistSleep(bonusItem.getResistSleep(i));
				pc.addResistFreeze(bonusItem.getResistFreeze(i));
				pc.addResistBlind(bonusItem.getResistBlind(i));
				pc.addExpBonusPct(bonusItem.getExpBonus(i));
				pc.addPotionRecoveryRatePct(bonusItem.getPotionRecoveryRate(i));
				pc.sendPackets(new S_OwnCharStatus(pc));
				pc.sendPackets(new S_SpMr(pc));
			}

			if (item.getItem().getType2() == 2
					&& item.getItem().getType() >= 10
					&& item.getItem().getType() <= 13) { // アクセサリー
				if (item.getId() == 21377) { // 旧上級装備
					pc.addFire(1);
					pc.addWater(1);
					pc.addEarth(1);
					pc.addWind(1);
					pc.sendPackets(new S_OwnCharStatus(pc));
					if ((Config.ACCESSORY_ENCHANT_BONUS.equalsIgnoreCase("std")
							&& oldEnchantLvl < 6 && newEnchantLvl >= 6)
						|| (Config.ACCESSORY_ENCHANT_BONUS.equalsIgnoreCase("ext")
							&& newEnchantLvl >= 6)) {
						pc.addHpr(1);
						pc.addMpr(1);
					}
				} else if (item.getId() == 20420 || item.getId() == 20426) { // 旧中級装備
					pc.addMaxHp(2);
					pc.sendPackets(new S_OwnCharStatus(pc));
					if ((Config.ACCESSORY_ENCHANT_BONUS.equalsIgnoreCase("std")
							&& oldEnchantLvl < 6 && newEnchantLvl >= 6)
						|| (Config.ACCESSORY_ENCHANT_BONUS.equalsIgnoreCase("ext")
							&& newEnchantLvl >= 6)) {
						pc.addMr(1);
						pc.sendPackets(new S_SpMr(pc));
					}
				} else if (item.getId() == 21351) { // 旧下級装備
					pc.addMaxMp(1);
					pc.sendPackets(new S_OwnCharStatus(pc));
					if ((Config.ACCESSORY_ENCHANT_BONUS.equalsIgnoreCase("std")
							&& oldEnchantLvl < 6 && newEnchantLvl >= 6)
						|| (Config.ACCESSORY_ENCHANT_BONUS.equalsIgnoreCase("ext")
							&& newEnchantLvl >= 6)) {
						pc.addSp(1);
						pc.sendPackets(new S_SpMr(pc));
					}
				} else if (grade == 3) { // 特級
					if (oldEnchantLvl == 0 && newEnchantLvl == 1) {
						pc.addMaxHp(15);
					}
					if (newEnchantLvl >= 2) {
						pc.addMaxHp(i * 5);
						pc.addAc(-i);
					}
					pc.sendPackets(new S_OwnCharStatus(pc));
				}
			}
		}
	}

	private void failureEnchant(L1PcInstance pc, L1ItemInstance target) {
		String s = "";
		String sa = "";
		int itemType = target.getItem().getType2();
		String nameId = target.getName();
		String pm = "";
		if (itemType == 1) { // 武器
			if (!target.isIdentified() || target.getEnchantLevel() == 0) {
				s = nameId; // \f1%0が強烈に%1光ったあと、蒸発してなくなります。
				sa = "$245";
				pc.getInventory().removeItem(target, target.getCount());
				pc.getInventory().saveItem(target);
			} else {
				if (target.getEnchantLevel() > 0) {
					pm = "+";
				}
				s = (new StringBuilder()).append(pm + target.getEnchantLevel())
						.append(" ").append(nameId).toString(); // \f1%0が強烈に%1
				// 光ったあと
				// 、蒸発してなくなります。
				sa = "$245";
				pc.getInventory().removeItem(target, target.getCount());
				pc.getInventory().saveItem(target);
			}
		} else if (itemType == 2) { // 防具
			if (!target.isIdentified() || target.getEnchantLevel() == 0) {
				s = nameId; // \f1%0が強烈に%1光ったあと、蒸発してなくなります。
				sa = " $252";
				pc.getInventory().removeItem(target, target.getCount());
				pc.getInventory().saveItem(target);
			} else {
				if (target.getEnchantLevel() > 0) {
					pm = "+";
				}
				s = (new StringBuilder()).append(pm + target.getEnchantLevel())
						.append(" ").append(nameId).toString(); // \f1%0が強烈に%1
				// 光ったあと
				// 、蒸発してなくなります。
				sa = " $252";
				pc.getInventory().removeItem(target, target.getCount());
				pc.getInventory().saveItem(target);
			}
		}
		pc.sendPackets(new S_ServerMessage(164, s, sa));
		pc.getInventory().removeItem(target, target.getCount());
		pc.getInventory().saveItem(target);
	}

	private void protectEnchant(L1PcInstance pc, L1ItemInstance item, L1ItemInstance target) {
		L1EnchantProtectScroll scroll = L1EnchantProtectScroll.get(item.getItemId());

		if (scroll != null) {
			target.setEnchantLevel(target.getEnchantLevel()
					- scroll.getDownLevel(target.getItemId()));
		}

		pc.sendPackets(new S_ServerMessage(1310));
		// 強烈な光りを放ちましたが、装備が蒸発しませんでした。
		target.setProtected(false);
		pc.getInventory().updateItem(target, L1PcInventory.COL_ENCHANTLVL);
	}

	private int enchantChance(L1ItemInstance l1iteminstance) {
		byte byte0 = 0;
		int i = l1iteminstance.getEnchantLevel();
		if (l1iteminstance.getItem().getType2() == 1) {
			switch (i) {
			case 0: // '\0'
				byte0 = 50;
				break;

			case 1: // '\001'
				byte0 = 33;
				break;

			case 2: // '\002'
				byte0 = 25;
				break;

			case 3: // '\003'
				byte0 = 25;
				break;

			case 4: // '\004'
				byte0 = 25;
				break;

			case 5: // '\005'
				byte0 = 20;
				break;

			case 6: // '\006'
				byte0 = 33;
				break;

			case 7: // '\007'
				byte0 = 33;
				break;

			case 8: // '\b'
				byte0 = 33;
				break;

			case 9: // '\t'
				byte0 = 25;
				break;

			case 10: // '\n'
				byte0 = 20;
				break;
			}
		} else if (l1iteminstance.getItem().getType2() == 2) {
			switch (i) {
			case 0: // '\0'
				byte0 = 50;
				break;

			case 1: // '\001'
				byte0 = 33;
				break;

			case 2: // '\002'
				byte0 = 25;
				break;

			case 3: // '\003'
				byte0 = 25;
				break;

			case 4: // '\004'
				byte0 = 25;
				break;

			case 5: // '\005'
				byte0 = 20;
				break;

			case 6: // '\006'
				byte0 = 17;
				break;

			case 7: // '\007'
				byte0 = 14;
				break;

			case 8: // '\b'
				byte0 = 12;
				break;

			case 9: // '\t'
				byte0 = 11;
				break;
			}
		}
		return byte0;
	}

	private int randomLevel(L1ItemInstance item, int itemId) {
		int safe_enchant = item.getItem().getSafeEnchant();

		if (itemId == L1ItemId.B_SCROLL_OF_ENCHANT_ARMOR
				|| itemId == 140129) {
			if (safe_enchant == 0) {
				return 1;
			} else if (item.getEnchantLevel() <= -6) {
				int j = _random.nextInt(100) + 1;
				if (j < 20) {
					return 1;
				} else if (j >= 21 && j <= 40) {
					return 2;
				} else if (j >= 41 && j <= 60) {
					return 3;
				} else if (j >= 61 && j <= 80) {
					return 4;
				} else if (j >= 81 && j <= 100) {
					return 5;
				}
			} else if (item.getEnchantLevel() >= -5
					&& item.getEnchantLevel() <= -3) {
				int j = _random.nextInt(100) + 1;
				if (j < 25) {
					return 1;
				} else if (j >= 26 && j <= 50) {
					return 2;
				} else if (j >= 51 && j <= 75) {
					return 3;
				} else if (j >= 76 && j <= 100) {
					return 4;
				}
			} else if (item.getEnchantLevel() <= 2) {
				int j = _random.nextInt(100) + 1;
				if (j < 32) {
					return 1;
				} else if (j >= 33 && j <= 76) {
					return 2;
				} else if (j >= 77 && j <= 100) {
					return 3;
				}
			} else if (item.getEnchantLevel() >= 3
					&& item.getEnchantLevel() <= 5) {
				int j = _random.nextInt(100) + 1;
				if (j < 50) {
					return 2;
				} else {
					return 1;
				}
			}
			{
				return 1;
			}
		}
		else if (itemId == L1ItemId.B_SCROLL_OF_ENCHANT_WEAPON
				||  itemId == 140130) {
			if (item.getEnchantLevel() <= 2) {
				int j = _random.nextInt(100) + 1;
				if (j < 32) {
					return 1;
				} else if (j >= 33 && j <= 76) {
					return 2;
				} else if (j >= 77 && j <= 100) {
					return 3;
				}
			} else if (item.getEnchantLevel() >= 3
					&& item.getEnchantLevel() <= 5) {
				int j = _random.nextInt(100) + 1;
				if (j < 50) {
					return 2;
				} else {
					return 1;
				}
			}
			{
				return 1;
			}
		}
		return 1;
	}

	private void loggingEnchant(L1PcInstance pc, L1ItemInstance item,
			int oldEnchantLvl, int newEnchantLvl) {
		EnchantLogTable logenchant = new EnchantLogTable();
		logenchant.storeLogEnchant(pc.getId(), item.getId(),
				oldEnchantLvl, newEnchantLvl);
	}

	private void announceEnchant(L1PcInstance pc, L1ItemInstance item) {
		for (L1PcInstance listner : L1World.getInstance().getAllPlayers()) {
			if (!listner.getExcludingList().contains(pc.getName())) {
				if (listner.isShowTradeChat() || listner.isShowWorldChat()) {
					listner.sendPackets(new S_SystemMessage(String.format(I18N_OVER_ENCHANT_SUCCESSFUL,
							pc.getName(), item.getLogName())));
					// %sが%sの成功に成功しました。
				}
			}
		}
	}

}
