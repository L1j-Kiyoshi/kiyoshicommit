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
package jp.l1j.server.templates;

public class L1Weapon extends L1Item {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public L1Weapon() {
	}

	private int _range = 0; // ● 射程範囲

	@Override
	public int getRange() {
		return _range;
	}

	public void setRange(int i) {
		_range = i;
	}

	private int _hitModifier = 0; // ● 命中率補正

	@Override
	public int getHitModifier() {
		return _hitModifier;
	}

	public void setHitModifier(int i) {
		_hitModifier = i;
	}

	private int _dmgModifier = 0; // ● ダメージ補正

	@Override
	public int getDmgModifier() {
		return _dmgModifier;
	}

	public void setDmgModifier(int i) {
		_dmgModifier = i;
	}

	private int _pvpHitModifier = 0; // ● PvP命中率補正

	@Override
	public int getPvPHitModifier() {
		return _pvpHitModifier;
	}

	public void setPvPHitModifier(int i) {
		_pvpHitModifier = i;
	}

	private int _pvpDmgModifier = 0; // ● PvPダメージ補正

	@Override
	public int getPvPDmgModifier() {
		return _pvpDmgModifier;
	}

	public void setPvPDmgModifier(int i) {
		_pvpDmgModifier = i;
	}

	private int _sayhaDmg = 0; // ● サイハビームのダメージ

	@Override
	public int getSayhaDmg() {
		return _sayhaDmg;
	}

	public void setSayhaDmg(int i) {
		_sayhaDmg = i;
	}

	private int _sayhaGfx = 0; // ● サイハビームのグラフィック

	@Override
	public int getSayhaGfx() {
		return _sayhaGfx;
	}

	public void setSayhaGfx(int i) {
		_sayhaGfx = i;
	}

	private int _weaponSpecial; // ● DB、クロウの発動・弱点露出率等

	@Override
	public int getWeaponSpecial() {
		return _weaponSpecial;
	}

	public void setWeaponSpecial(int i) {
		_weaponSpecial = i;
	}

	private int _magicDmgModifier = 0; // ● 攻撃魔法のダメージ補正

	@Override
	public int getMagicDmgModifier() {
		return _magicDmgModifier;
	}

	public void setMagicDmgModifier(int i) {
		_magicDmgModifier = i;
	}

	private boolean _hpDrain; // ● HP吸収

	@Override
	public boolean isHpDrain() {
		return _hpDrain;
	}

	public void setHpDrain(boolean i) {
		_hpDrain = i;
	}

	private int _hpDrainChance = 0; // ● HP吸収確率

	@Override
	public int getHpDrainChance() {
		return _hpDrainChance;
	}

	public void setHpDrainChance(int i) {
		_hpDrainChance = i;
	}

	private boolean _mpDrain; // ● MP吸収

	@Override
	public boolean isMpDrain() {
		return _mpDrain;
	}

	public void setMpDrain(boolean i) {
		_mpDrain = i;
	}

	private boolean _canbeDmg; // ● 損傷の有無

	@Override
	public boolean getCanbeDmg() {
		return _canbeDmg;
	}

	public void setCanbeDmg(boolean i) {
		_canbeDmg = i;
	}

	private boolean _isTwohanded; // true:両手、false:片手

	@Override
	public boolean isTwohanded() {
		return _isTwohanded;
	}

	public void setIsTwohanded(boolean isTwohanded) {
		_isTwohanded = isTwohanded;
	}

}
