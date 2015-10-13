package jp.l1j.server.templates;

public class L1SpawnUltimateBattle {

	private int _monsterId;

	public void setMonsterId(int i) {
		_monsterId = i;
	}

	public int getMonsterId() {
		return _monsterId;
	}

	private int _count;

	public void setCount(int i) {
		_count = i;
	}

	public int getCount() {
		return _count;
	}

	private int _spawnDelay; // このモンスターが出てから次が出るまでの時間(秒)

	public void setSpawnDelay(int i) {
		_spawnDelay = i;
	}

	public int getSpawnDelay() {
		return _spawnDelay;
	}

	private int _sealCount; // 証の数

	public void setSealCount(int i) {
		_sealCount = i;
	}

	public int getSealCount() {
		return _sealCount;
	}

	private boolean _isDrop = false; // ドロップの有無

	public void setDrop(boolean b) {
		_isDrop = b;
	}

	public boolean isDrop() {
		return _isDrop;
	}
}
