package jp.l1j.server.templates;

public class L1RaidConfig {

	private int _raidId;

	public void setRaidId(int i) {
		_raidId = i;
	}

	public int getRaidId() {
		return _raidId;
	}

	private String _raidName;

	public void setRaidName(String s) {
		_raidName = s;
	}

	public String getRaidName() {
		return _raidName;
	}

	private int _minLevel;

	public void setMinLevel(int i) {
		_minLevel = i;
	}

	public int getMinLevel() {
		return _minLevel;
	}

	private int _maxLevel;

	public void setMaxLevel(int i) {
		_maxLevel = i;
	}

	public int getMaxLevel() {
		return _maxLevel;
	}

	private int _minPlayer;

	public void setMinPlayer(int i) {
		_minPlayer = i;
	}

	public int getMinPlayer() {
		return _minPlayer;
	}

	private int _maxPlayer;

	public void setMaxPlayer(int i) {
		_maxPlayer = i;
	}

	public int getMaxPlayer() {
		return _maxPlayer;
	}

	private int _maxRaidLimit;

	public void setMaxRaidLimit(int i) {
		_maxRaidLimit = i;
	}

	public int getMaxRaidLimit() {
		return _maxRaidLimit;
	}
}
