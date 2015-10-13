package jp.l1j.server.templates;

public class L1PhysicalAttack {

	private int _skillId; // この物理スキルのスキルID

	public void setSkillId(int i) {
		_skillId = i;
	}

	public int getSkillId() {
		return _skillId;
	}

	private String _name; // この物理スキル名

	public void setName(String s) {
		_name = s;
	}

	public String getName() {
		return _name;
	}

	private int _actionId; // この物理スキルを使用した時のアクションID

	public void setActionId(int i) {
		_actionId = i;
	}

	public int getActionId() {
		return _actionId;
	}

	private String _type; // この物理スキルのタイプ

	public void setType(String s) {
		_type = s;
	}

	public String getType() {
		return _type;
	}

	private int _range; // この物理スキルが使用可能な範囲

	public void setRange(int i) {
		_range = i;
	}

	public int getRange() {
		return _range;
	}

	private int _laverage; // この物理スキルを使用した時のダメージ倍率(1 / 10)

	public void setLaverage(int i) {
		_laverage = i;
	}

	public int getLaverage() {
		return _laverage;
	}

	private int _area; // この物理スキルの範囲

	public void setArea(int i) {
		_area = i;
	}

	public int getArea() {
		return _area;
	}

	/*
	 * 起点となるセルの設定。
	 * 向かっている方向に対して右側はプラス、左側はマイナス、中央は０の数値で示す。
	 */
	private int _baseRL; // 起点となるセル（左右）

	public void setBaseRL(int i) {
		_baseRL = i;
	}

	public int getBaseRL() {
		return _baseRL;
	}

	/*
	 * 起点となるセルの設定。
	 * 向かっている方向に対して前はプラス、後ろはマイナス、中央は０の数値で示す。
	 */
	private int _baseFB; // 起点となるセル（前後）

	public void setBaseFB(int i) {
		_baseFB = i;
	}

	public int getBaseFB() {
		return _baseFB;
	}

	/*
	 * 特殊な範囲を指定する場合の設定。
	 * 起点となるセルから、術者の向いている方向から右側を指定。
	 * マイナスを指定する事も可能。（起点セルからずらした攻撃が可能）
	 */
	private int _areaRight;

	public void setAreaRight(int i) {
		_areaRight = i;
	}

	public int getAreaRight() {
		return _areaRight;
	}

	/*
	 * 特殊な範囲を指定する場合の設定。
	 * 起点となるセルから、術者の向いている方向から左側を指定。
	 * マイナスを指定する事も可能。（起点セルからずらした攻撃が可能）
	 */
	private int _areaLeft;

	public void setAreaLeft(int i) {
		_areaLeft = i;
	}

	public int getAreaLeft() {
		return _areaLeft;
	}

	/*
	 * 特殊な範囲を指定する場合の設定。
	 * 起点となるセルから、術者の向いている方向から前側を指定。
	 * マイナスを指定する事も可能。（起点セルからずらした攻撃が可能）
	 */
	private int _areaFront;

	public void setAreaFront(int i) {
		_areaFront = i;
	}

	public int getAreaFront() {
		return _areaFront;
	}

	/*
	 * 特殊な範囲を指定する場合の設定。
	 * 起点となるセルから、術者の向いている方向から後側を指定。
	 * マイナスを指定する事も可能。（起点セルからずらした攻撃が可能）
	 */
	private int _areaBack;

	public void setAreaBack(int i) {
		_areaBack = i;
	}

	public int getAreaBack() {
		return _areaBack;
	}

	private int _damageValue; // 基礎ダメージ。そのまま追加ダメージとなる。

	public void setDamageValue(int i) {
		_damageValue = i;
	}

	public int getDamageValue() {
		return _damageValue;
	}

	private int _damageDice; // ダメージダイス

	public void setDamageDice(int i) {
		_damageDice = i;
	}

	public int getDamageDice() {
		return _damageDice;
	}

	private int _damageDiceCount; // ダメージダイスを振る回数

	public void setDamageDiceCount(int i) {
		_damageDiceCount = i;
	}

	public int getDamageDiceCount() {
		return _damageDiceCount;
	}

	private int _castgfx; // スキル発動時に表示するエフェクト

	public void setCastGfx(int i) {
		_castgfx = i;
	}

	public int getCastGfx() {
		return _castgfx;
	}

	private int _areaEffect; // スキルが範囲の場合、ターゲット全てに表示するエフェクト

	public void setAreaEffect(int i) {
		_areaEffect = i;
	}

	public int getAreaEffect() {
		return _areaEffect;
	}

	/**
	 * スキルの起点位置がずれるスキルかどうか。
	 * @return
	 * 		起点がずれる場合はtrue
	 */
	public boolean isBaseLocOut() {
		return (_baseFB != 0 || _baseRL != 0);
	}

	/**
	 * スキルが不規則な範囲攻撃（長方形型等）かどうか。
	 * @return
	 * 		不規則な範囲攻撃系スキルならtrue
	 */
	public boolean isIrregularArea() {
		return (_areaFront != 0 || _areaBack != 0 || _areaLeft != 0 || _areaRight != 0);
	}
}
