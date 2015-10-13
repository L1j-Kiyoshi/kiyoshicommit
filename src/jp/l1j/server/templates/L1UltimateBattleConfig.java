package jp.l1j.server.templates;

import jp.l1j.server.model.L1Location;

public class L1UltimateBattleConfig {

	private int _ultimateBattleId;

	public void setUltimateBattleId(int i) {
		_ultimateBattleId = i;
	}

	public int getUltimateBattleId() {
		return _ultimateBattleId;
	}

	private int _managerId;

	public int getManagerId() {
		return _managerId;
	}

	public void setManagerId(int i) {
		_managerId = i;
	}

	private int _managerX;

	public int getManagerX() {
		return _managerX;
	}

	public void setManagerX(int i) {
		_managerX = i;
	}

	private int _managerY;

	public int getManagerY() {
		return _managerY;
	}

	public void setManagerY(int i) {
		_managerY = i;
	}

	private int _managerMapId;

	public int getManagerMapId() {
		return _managerMapId;
	}

	public void setManagerMapId(int i) {
		_managerMapId = i;
	}

	private int _managerHeading;

	public int getManagerHeading() {
		return _managerHeading;
	}

	public void setManagerHeading(int i) {
		_managerHeading = i;
	}

	private int _subManagerId;

	public int getSubManagerId() {
		return _subManagerId;
	}

	public void setSubManagerId(int i) {
		_subManagerId = i;
	}

	private int _subManagerX;

	public int getSubManagerX() {
		return _subManagerX;
	}

	public void setSubManagerX(int i) {
		_subManagerX = i;
	}

	private int _subManagerY;

	public int getSubManagerY() {
		return _subManagerY;
	}

	public void setSubManagerY(int i) {
		_subManagerY = i;
	}

	private int _subManagerMapId;

	public int getSubManagerMapId() {
		return _subManagerMapId;
	}

	public void setSubManagerMapId(int i) {
		_subManagerMapId = i;
	}

	private int _subManagerHeading;

	public int getSubManagerHeading() {
		return _subManagerHeading;
	}

	public void setSubManagerHeading(int i) {
		_subManagerHeading = i;
	}

	private int _locX1;

	public int getLocX1() {
		return _locX1;
	}

	public void setLocX1(int locX1) {
		_locX1 = locX1;
	}

	private int _locY1;

	public int getLocY1() {
		return _locY1;
	}

	public void setLocY1(int locY1) {
		_locY1 = locY1;
	}

	private int _locX2;

	public int getLocX2() {
		return _locX2;
	}

	public void setLocX2(int locX2) {
		_locX2 = locX2;
	}

	private int _locY2;

	public int getLocY2() {
		return _locY2;
	}

	public void setLocY2(int locY2) {
		_locY2 = locY2;
	}

	public void setMapId(short mapId) {
		_mapId = mapId;
	}

	public short getMapId() {
		return _mapId;
	}

	private int _locX;
	private int _locY;
	private short _mapId;
	private L1Location _location; // 中心点

	// setされたlocx1〜locy2から中心点を求める。
	public void resetLoc() {
		_locX = (_locX2 + _locX1) / 2;
		_locY = (_locY2 + _locY1) / 2;
		_location = new L1Location(_locX, _locY, _mapId);
	}

	public L1Location getLocation() {
		return _location;
	}

	private int _minLevel;

	public int getMinLevel() {
		return _minLevel;
	}

	public void setMinLevel(int level) {
		_minLevel = level;
	}

	private int _maxLevel;

	public int getMaxLevel() {
		return _maxLevel;
	}

	public void setMaxLevel(int level) {
		_maxLevel = level;
	}

	private int _maxPlayer;

	public int getMaxPlayer() {
		return _maxPlayer;
	}

	public void setMaxPlayer(int count) {
		_maxPlayer = count;
	}

	private boolean _enterRoyal;

	public void setEnterRoyal(boolean enterRoyal) {
		_enterRoyal = enterRoyal;
	}

	public boolean isEnterRoyal() {
		return _enterRoyal;
	}

	private boolean _enterKnight;

	public void setEnterKnight(boolean enterKnight) {
		_enterKnight = enterKnight;
	}

	public boolean isEnterKnight() {
		return _enterKnight;
	}

	private boolean _enterMage;

	public void setEnterMage(boolean enterMage) {
		_enterMage = enterMage;
	}

	public boolean isEnterMage() {
		return _enterMage;
	}

	private boolean _enterElf;

	public void setEnterElf(boolean enterElf) {
		_enterElf = enterElf;
	}

	public boolean isEnterElf() {
		return _enterElf;
	}

	private boolean _enterDarkelf;

	public void setEnterDarkelf(boolean enterDarkelf) {
		_enterDarkelf = enterDarkelf;
	}

	public boolean isEnterDarkelf() {
		return _enterDarkelf;
	}

	private boolean _enterDragonKnight;

	public void setEnterDragonKnight(boolean enterDragonKnight) {
		_enterDragonKnight = enterDragonKnight;
	}

	public boolean isEnterDragonKnight() {
		return _enterDragonKnight;
	}

	private boolean _enterIllusionist;

	public void setEnterIllusionist(boolean enterIllusionist) {
		_enterIllusionist = enterIllusionist;
	}

	public boolean isEnterIllusionist() {
		return _enterIllusionist;
	}

	private boolean _enterMale;

	public void setEnterMale(boolean enterMale) {
		_enterMale = enterMale;
	}

	public boolean isEnterMale() {
		return _enterMale;
	}

	private boolean _enterFemale;

	public void setEnterFemale(boolean enterFemale) {
		_enterFemale = enterFemale;
	}

	public boolean isEnterFemale() {
		return _enterFemale;
	}

	private boolean _usePot;

	public boolean canUsePot() {
		return _usePot;
	}

	public void setUsePot(boolean usePot) {
		_usePot = usePot;
	}
	private int _hpr;

	public int getHpr() {
		return _hpr;
	}

	public void setHpr(int hpr) {
		_hpr = hpr;
	}

	private int _mpr;

	public int getMpr() {
		return _mpr;
	}

	public void setMpr(int mpr) {
		_mpr = mpr;
	}
}
