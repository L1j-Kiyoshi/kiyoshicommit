package jp.l1j.server.templates;

public class L1RaidDrop {

	private int _raidId;

	public void setRaidId(int i) {
		_raidId = i;
	}

	public int getRaidId() {
		return _raidId;
	}

	private int _patternId;

	public void setPatternId(int i) {
		_patternId = i;
	}

	public int getPatternId() {
		return _patternId;
	}

	private int _setId;

	public void setSetId(int i) {
		_setId = i;
	}

	public int getSetId() {
		return _setId;
	}

	private int _itemId;

	public void setItemId(int i) {
		_itemId = i;
	}

	public int getItemId() {
		return _itemId;
	}

	private String _itemName;

	public void setItemName(String s) {
		_itemName = s;
	}

	public String getItemName() {
		return _itemName;
	}

	private int _minCount;

	public void setMinCount(int i) {
		_minCount = i;
	}

	public int getMinCount() {
		return _minCount;
	}

	private int _maxCount;

	public void setMaxCount(int i) {
		_maxCount = i;
	}

	public int getMaxCount() {
		return _maxCount;
	}

	private int _dropChance;

	public void setDropChance(int i) {
		_dropChance = i;
	}

	public int getDropChance() {
		return _dropChance;
	}

}
