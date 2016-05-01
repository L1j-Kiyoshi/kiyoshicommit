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
package jp.l1j.server.model.item.executor;

import static jp.l1j.locale.I18N.*;
import static jp.l1j.server.model.skill.L1SkillId.*;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import jp.l1j.server.datatables.ItemTable;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.inventory.L1PcInventory;
import jp.l1j.server.model.skill.L1BuffUtil;
import jp.l1j.server.packets.server.S_Dexup;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.packets.server.S_SkillHaste;
import jp.l1j.server.packets.server.S_SkillIconShield;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.packets.server.S_SpMr;
import jp.l1j.server.packets.server.S_Strup;
import jp.l1j.server.utils.PerformanceTimer;

@XmlAccessorType(XmlAccessType.FIELD)
public class L1FloraPotion {

	private static Logger _log = Logger.getLogger(L1FloraPotion.class.getName());

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ItemEffectList")
	private static class ItemEffectList implements Iterable<L1FloraPotion> {
		@XmlElement(name = "Item")
		private List<L1FloraPotion> _list;

		public Iterator<L1FloraPotion> iterator() {
			return _list.iterator();
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Effect {
		@XmlAttribute(name = "Time")
		private int _time;

		public int getTime() {
			return _time;
		}
		@XmlAttribute(name = "GfxId")
		private int _gfxId;

		public int getGfxId() {
			return _gfxId;
		}

		@XmlAttribute(name = "Str")
		private int _str = 0;

		public int getStr() {
			return _str;
		}

		@XmlAttribute(name = "Dex")
		private int _dex = 0;

		public int getDex() {
			return _dex;
		}

		@XmlAttribute(name = "Ac")
		private int _ac = 0;

		public int getAc() {
			return _ac;
		}

		@XmlAttribute(name = "Sp")
		private int _sp = 0;

		public int getSp() {
			return _sp;
		}

		@XmlAttribute(name = "Haste")
		private boolean _isHaste = false;

		public boolean isHaste() {
			return _isHaste;
		}

		@XmlAttribute(name = "Hit")
		private int _hit = 0;

		public int getHit() {
			return _hit;
		}

		@XmlAttribute(name = "Dmg")
		private int _dmg = 0;

		public int getDmg() {
			return _dmg;
		}


		@XmlAttribute(name = "BowHit")
		private int _bowHit = 0;

		public int getBowHit() {
			return _bowHit;
		}

		@XmlAttribute(name = "BowDmg")
		private int _bowDmg = 0;

		public int getBowDmg() {
			return _bowDmg;
		}

		@XmlAttribute(name = "MapId")
		private int _mapid = 0;

		public int getMapId() {
			return _mapid;
		}
	}

	private static final String _path = "./data/xml/Item/FloraPotion.xml";

	private static HashMap<Integer, L1FloraPotion> _dataMap = new HashMap<Integer, L1FloraPotion>();

	public static L1FloraPotion get(int id) {
		return _dataMap.get(id);
	}

	@XmlAttribute(name = "ItemId")
	private int _itemId;

	private int getItemId() {
		return _itemId;
	}

	@XmlAttribute(name = "Remove")
	private int _remove;

	private int getRemove() {
		return _remove;
	}

	@XmlElement(name = "Effect")
	private CopyOnWriteArrayList<Effect> _effects;

	private List<Effect> getEffects() {
		return _effects;
	}

	public Effect getEffect(L1PcInstance pc) {
		for (Effect each : getEffects()) {
			return each;
		}
		return null;
	}

	public Effect getEffect(L1PcInstance pc, boolean isMapJudgement) {
		for (Effect each : getEffects()) {
			if (isMapJudgement) {
				if (each.getMapId() != 0 && pc.getMapId() != each.getMapId()) {
					continue;
				}
			}
			return each;
		}
		return null;
	}

	private static void loadXml(HashMap<Integer, L1FloraPotion> dataMap) {
		PerformanceTimer timer = new PerformanceTimer();
		System.out.print("loading flora potions...");
		try {
			JAXBContext context = JAXBContext.newInstance(L1FloraPotion.ItemEffectList.class);

			Unmarshaller um = context.createUnmarshaller();

			File file = new File(_path);
			ItemEffectList list = (ItemEffectList) um.unmarshal(file);

			for (L1FloraPotion each : list) {
				if (ItemTable.getInstance().getTemplate(each.getItemId()) == null) {
					System.out.println(String.format(I18N_DOES_NOT_EXIST_ITEM_LIST, each.getItemId()));
					// %s はアイテムリストに存在しません。
				} else {
					dataMap.put(each.getItemId(), each);
				}
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, _path + "load failed.", e);
			System.exit(0);
		}
		System.out.println("OK! " + timer.elapsedTimeMillis() + "ms");
	}

	public static void load() {
		loadXml(_dataMap);
	}

	public static void reload() {
		HashMap<Integer, L1FloraPotion> dataMap = new HashMap<Integer, L1FloraPotion>();
		loadXml(dataMap);
		_dataMap = dataMap;
	}

	public boolean use(L1PcInstance pc, L1ItemInstance item) {
		if (pc.hasSkillEffect(71) == true) { // ディケイ ポーションの状態
			pc.sendPackets(new S_ServerMessage(698)); // 魔力によって何も飲むことができません。
			return false;
		}

		// アブソルート バリアの解除
		L1BuffUtil.cancelBarrier(pc);

		int maxChargeCount = item.getItem().getMaxChargeCount();
		int chargeCount = item.getChargeCount();
		if (maxChargeCount > 0 && chargeCount <= 0) {
			// \f1何も起きませんでした。
			pc.sendPackets(new S_ServerMessage(79));
			return false;
		}

		Effect effect = getEffect(pc, true);
		if (effect == null) {
			// \f1何も起きませんでした。
			pc.sendPackets(new S_ServerMessage(79));
			return false;
		}

		if (effect.getStr() > 0) { // STR上昇効果 (激励,フローラ)
			if (pc.hasSkillEffect(PHYSICAL_ENCHANT_STR))
				pc.removeSkillEffect(PHYSICAL_ENCHANT_STR);
			if (pc.hasSkillEffect(DRESS_MIGHTY))
				pc.removeSkillEffect(DRESS_MIGHTY);
			if (!pc.hasSkillEffect(STATUS_FLORA_POTION_STR))
				pc.addStr(effect.getStr());
			pc.sendPackets(new S_Strup(pc, effect.getStr(), effect.getTime()));
			pc.setSkillEffect(STATUS_FLORA_POTION_STR, effect.getTime() * 1000);
		}
		if (effect.getDex() > 0) { // DEX上昇効果 (才能,フローラ)
			if (pc.hasSkillEffect(PHYSICAL_ENCHANT_DEX))
				pc.removeSkillEffect(PHYSICAL_ENCHANT_DEX);
			if (pc.hasSkillEffect(DRESS_DEXTERITY))
				pc.removeSkillEffect(DRESS_DEXTERITY);
			if (!pc.hasSkillEffect(STATUS_FLORA_POTION_DEX))
				pc.addDex(effect.getDex());
			pc.sendPackets(new S_Dexup(pc, effect.getDex(), effect.getTime()));
			pc.setSkillEffect(STATUS_FLORA_POTION_DEX, effect.getTime() * 1000);
		}
		if (effect.getAc() != 0) { // AC効果
			if (pc.hasSkillEffect(SHIELD))
				pc.removeSkillEffect(SHIELD);
			if (pc.hasSkillEffect(EARTH_BLESS))
				pc.removeSkillEffect(EARTH_BLESS);
			if (pc.hasSkillEffect(IRON_SKIN))
				pc.removeSkillEffect(IRON_SKIN);
			if (pc.hasSkillEffect(EARTH_SKIN))
				pc.removeSkillEffect(EARTH_SKIN);
			if (!pc.hasSkillEffect(STATUS_FLORA_POTION_AC))
				pc.addAc(effect.getAc());
			pc.sendPackets(new S_SkillIconShield(5, effect.getTime()));
			pc.setSkillEffect(STATUS_FLORA_POTION_AC, effect.getTime() * 1000);
		}
		if (effect.isHaste()) { // ヘイスト効果
			if (pc.hasSkillEffect(STATUS_HASTE))
				pc.removeSkillEffect(STATUS_HASTE);
			if (pc.hasSkillEffect(HASTE))
				pc.removeSkillEffect(HASTE);
			if (pc.hasSkillEffect(GREATER_HASTE))
				pc.removeSkillEffect(GREATER_HASTE);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 1, effect.getTime()));
			pc.setSkillEffect(STATUS_HASTE, effect.getTime() * 1000);
		}
		if (getItemId() == 50555) { // ドラゴンの石
			if (pc.hasSkillEffect(STONE_OF_DRAGON)) {
				pc.removeSkillEffect(STONE_OF_DRAGON);
			}
			pc.addHitup(effect.getHit());
			pc.addDmgup(effect.getDmg());
			pc.addBowHitup(effect.getBowHit());
			pc.addBowDmgup(effect.getBowDmg());
			pc.addSp(effect.getSp());
			pc.sendPackets(new S_SpMr(pc));
			pc.setSkillEffect(STONE_OF_DRAGON, effect.getTime() * 1000);
			//pc.sendPackets(new S_SkillBrave(pc.getId(), 1, effect.getTime()));
			//pc.broadcastPacket(new S_SkillBrave(pc.getId(), 1, 0));
		}

		pc.sendPackets(new S_SkillSound(pc.getId(), effect.getGfxId()));
		pc.broadcastPacket(new S_SkillSound(pc.getId(), effect.getGfxId()));

		if (getRemove() > 0) {
			if (chargeCount > 0) {
				item.setChargeCount(chargeCount - getRemove());
				pc.getInventory().updateItem(item,
						L1PcInventory.COL_CHARGE_COUNT);
			} else {
				pc.getInventory().removeItem(item, getRemove());
			}
		}

		return true;
	}

}
