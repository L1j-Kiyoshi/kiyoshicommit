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

package jp.l1j.server.packets.client;

import java.util.logging.Level;
import java.util.logging.Logger;
import jp.l1j.server.ClientThread;
import jp.l1j.server.datatables.CharacterTable;
import jp.l1j.server.datatables.ExpTable;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.packets.server.S_CharReset;
import jp.l1j.server.packets.server.S_InitialAbilityGrowth;
import jp.l1j.server.packets.server.S_OwnCharStatus;
import jp.l1j.server.utils.CalcInitHpMp;
import jp.l1j.server.utils.CalcStat;

// Referenced classes of package jp.l1j.server.clientpackets:
// ClientBasePacket

public class C_CharReset extends ClientBasePacket {

	private static final String C_CHAR_RESET = "[C] C_CharReset";
	private static Logger _log = Logger.getLogger(C_CharReset.class.getName());

/**
 * //配置完初期點數 按確定 127.0.0.1 Request Work ID : 120 0000: 78 01 0d 0a 0b 0a 12 0d
 * 
 * //提升10及 127.0.0.1 Request Work ID : 120 0000: 78 02 07 00 //提升1及 127.0.0.1
 * Request Work ID : 120 0000: 78 02 00 04
 * 
 * //提升完等級 127.0.0.1 Request Work ID : 120 0000: 78 02 08 00 x...
 * 
 * //萬能藥 127.0.0.1 Request Work ID : 120 0000: 78 03 23 0a 0b 17 12 0d
 */	

	public C_CharReset(byte abyte0[], ClientThread clientthread) {
		super(abyte0);
		L1PcInstance pc = clientthread.getActiveChar();
		int stage = readC();

		if (stage == 0x01) { // 0x01:キャラクター初期化
			int str = readC();
			int intel = readC();
			int wis = readC();
			int dex = readC();
			int con = readC();
			int cha = readC();
			int hp = CalcInitHpMp.calcInitHp(pc);
			int mp = CalcInitHpMp.calcInitMp(pc);
			pc.sendPackets(new S_CharReset(pc, 1, hp, mp, 10, str, intel, wis,
					dex, con, cha));
			initCharStatus(pc, hp, mp, str, intel, wis, dex, con, cha);                        
                        pc.setOriginalStr(pc.getBaseStr());
                        pc.setOriginalInt(pc.getBaseInt());
                	pc.setOriginalWis(pc.getBaseWis());
                	pc.setOriginalDex(pc.getBaseDex());
                	pc.setOriginalCon(pc.getBaseCon());
                        pc.setOriginalCha(pc.getBaseCha());
                
		} else if (stage == 0x02) { // 0x02:ステータス再分配
			int type2 = readC();
			if (type2 == 0x00) { // 0x00:Lv1UP
				setLevelUp(pc, 1); 
			} else if (type2 == 0x07) { // 0x07:Lv10UP
				if (pc.getTempMaxLevel() - pc.getTempLevel() < 10) {
					return;
				}
				setLevelUp(pc,10);
			} else if (type2 == 0x01) {
				pc.addBaseStr((byte) 1);
				setLevelUp(pc, 1);
			} else if (type2 == 0x02) {
				pc.addBaseInt((byte) 1);
				setLevelUp(pc,1);
			} else if (type2 == 0x03) {
				pc.addBaseWis((byte) 1);
				setLevelUp(pc, 1);
			} else if (type2 == 0x04) {
				pc.addBaseDex((byte) 1);
				setLevelUp(pc, 1);
			} else if (type2 == 0x05) {
				pc.addBaseCon((byte) 1);
				setLevelUp(pc, 1);
			} else if (type2 == 0x06) {
				pc.addBaseCha((byte) 1);
				setLevelUp(pc, 1);
			} else if (type2 == 0x08) {
				switch(readC()){
				case 1:
					pc.addBaseStr((byte) 1);
					break;
				case 2:
					pc.addBaseInt((byte) 1);
					break;
				case 3:
					pc.addBaseWis((byte) 1);
					break;
				case 4:
					pc.addBaseDex((byte) 1);
					break;
				case 5:
					pc.addBaseCon((byte) 1);
					break;
				case 6:
					pc.addBaseCha((byte) 1);
					break;
				}
				if (pc.getElixirStats() > 0) {
					pc.sendPackets(new S_CharReset(pc.getElixirStats()));
					return;
				}
				saveNewCharStatus(pc);
			}
		} else if (stage == 0x03) {
			pc.addBaseStr((byte) (readC() - pc.getBaseStr()));
			pc.addBaseInt((byte) (readC() - pc.getBaseInt()));
			pc.addBaseWis((byte) (readC() - pc.getBaseWis()));
			pc.addBaseDex((byte) (readC() - pc.getBaseDex()));
			pc.addBaseCon((byte) (readC() - pc.getBaseCon()));
			pc.addBaseCha((byte) (readC() - pc.getBaseCha()));
			saveNewCharStatus(pc);
		}
	}

	private void saveNewCharStatus(L1PcInstance pc) {
		pc.setInCharReset(false);
		if(pc.getOriginalAc() > 0) {
			pc.addAc(pc.getOriginalAc());
		}
		if(pc.getOriginalMr() > 0) {
			pc.addMr(0 - pc.getOriginalMr());
		}
		pc.refresh();
		pc.setCurrentHp(pc.getMaxHp());
		pc.setCurrentMp(pc.getMaxMp());
		if (pc.getTempMaxLevel() != pc.getLevel()) {
			pc.setLevel(pc.getTempMaxLevel());
			pc.setExp(ExpTable.getExpByLevel(pc.getTempMaxLevel()));
		}
		if (pc.getLevel() > 50) {
			pc.setBonusStats(pc.getLevel() - 50);
		} else {
			pc.setBonusStats(0);
		}
		pc.sendPackets(new S_OwnCharStatus(pc));
		S_InitialAbilityGrowth AbilityGrowth = new S_InitialAbilityGrowth(pc);
		pc.sendPackets(AbilityGrowth);
		
		L1ItemInstance item = pc.getInventory().findItemId(49142); // 希望のロウソク
		if (item != null) {
			try {
				pc.getInventory().removeItem(item, 1);
				pc.save(); // DBにキャラクター情報を書き込む
			} catch (Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		L1Teleport.teleport(pc, 32628, 32772, (short) 4, 4, false);
	}

	private void initCharStatus(L1PcInstance pc, int hp, int mp, int str,
			int intel, int wis, int dex, int con, int cha) {
		pc.addBaseMaxHp((short)(hp - pc.getBaseMaxHp()));
		pc.addBaseMaxMp((short)(mp - pc.getBaseMaxMp()));
		pc.addBaseStr((byte)(str - pc.getBaseStr()));
		pc.addBaseInt((byte)(intel - pc.getBaseInt()));
		pc.addBaseWis((byte)(wis - pc.getBaseWis()));
		pc.addBaseDex((byte)(dex - pc.getBaseDex()));
		pc.addBaseCon((byte)(con - pc.getBaseCon()));
		pc.addBaseCha((byte)(cha - pc.getBaseCha()));
		pc.setOriginalStr(pc.getBaseStr());
		pc.setOriginalInt(pc.getBaseInt());
		pc.setOriginalWis(pc.getBaseWis());
		pc.setOriginalDex(pc.getBaseDex());
		pc.setOriginalCon(pc.getBaseCon());
		pc.setOriginalCha(pc.getBaseCha());
		pc.addMr(0 - pc.getMr());
    	pc.addDmgup(0 - pc.getDmgup());
    	pc.addHitup(0 - pc.getHitup());
	}

	private void setLevelUp(L1PcInstance pc ,int addLv) {
		pc.setTempLevel(pc.getTempLevel()+ addLv);
		for (int i = 0; i < addLv; i++) {
			short randomHp = CalcStat.calcStatHp(pc.getType(),
					pc.getBaseMaxHp(), pc.getBaseCon(), pc.getOriginalHpup());
			short randomMp = CalcStat.calcStatMp(pc.getType(),
					pc.getBaseMaxMp(), pc.getBaseWis(), pc.getOriginalMpup());
			pc.addBaseMaxHp(randomHp);
			pc.addBaseMaxMp(randomMp);
		}
		int newAc = CalcStat.calcAc(pc.getTempLevel(), pc.getBaseDex());
		pc.sendPackets(new S_CharReset(pc,pc.getTempLevel(),
				pc.getBaseMaxHp(), pc.getBaseMaxMp(), newAc,
				pc.getBaseStr(), pc.getBaseInt(), pc.getBaseWis(),
				pc.getBaseDex(), pc.getBaseCon(), pc.getBaseCha()));
	}

	private int getInitialHp (L1PcInstance pc) {
		int hp = 1;
		if (pc.isCrown()) {
			hp = 14;
		} else if (pc.isKnight()) {
			hp = 16;
		} else if (pc.isElf()) {
			hp = 15;
		} else if (pc.isWizard()) {
			hp = 12;
		} else if (pc.isDarkelf()) {
			hp = 12;
		} else if (pc.isDragonKnight()) {
			hp = 15;
		} else if (pc.isIllusionist()) {
			hp = 15;
		}
		return hp;
	}

	private int getInitialMp (L1PcInstance pc) {
		int mp = 1;
		if (pc.isCrown()) {
			switch (pc.getWis()) {
			case 11:
				mp = 2;
				break;
			case 12:
			case 13:
			case 14:
			case 15:
				mp = 3;
				break;
			case 16:
			case 17:
			case 18:
				mp = 4;
				break;
			default:
				mp = 2;
				break;
			}
		} else if (pc.isKnight()) {
			switch (pc.getWis()) {
			case 9:
			case 10:
			case 11:
				mp = 1;
				break;
			case 12:
			case 13:
				mp = 2;
				break;
			default:
				mp = 1;
				break;
			}
		} else if (pc.isElf()) {
			switch (pc.getWis()) {
			case 12:
			case 13:
			case 14:
			case 15:
				mp = 4;
				break;
			case 16:
			case 17:
			case 18:
				mp = 6;
				break;
			default:
				mp = 4;
				break;
			}
		} else if (pc.isWizard()) {
			switch (pc.getWis()) {
			case 12:
			case 13:
			case 14:
			case 15:
				mp = 6;
				break;
			case 16:
			case 17:
			case 18:
				mp = 8;
				break;
			default:
				mp = 6;
				break;
			}
		} else if (pc.isDarkelf()) {
			switch (pc.getWis()) {
			case 10:
			case 11:
				mp = 3;
				break;
			case 12:
			case 13:
			case 14:
			case 15:
				mp = 4;
				break;
			case 16:
			case 17:
			case 18:
				mp = 6;
				break;
			default:
				mp = 3;
				break;
			}
		} else if (pc.isDragonKnight()) {
			switch (pc.getWis()) {
			case 12:
			case 13:
			case 14:
			case 15:
				mp = 4;
				break;
			case 16:
			case 17:
			case 18:
				mp = 6;
				break;
			default:
				mp = 4;
				break;
			}
		} else if (pc.isIllusionist()) {
			switch (pc.getWis()) {
			case 12:
			case 13:
			case 14:
			case 15:
				mp = 4;
				break;
			case 16:
			case 17:
			case 18:
				mp = 6;
				break;
			default:
				mp = 4;
				break;
			}
		}
		return mp;
	}

	@Override
	public String getType() {
		return C_CHAR_RESET;
	}

}
