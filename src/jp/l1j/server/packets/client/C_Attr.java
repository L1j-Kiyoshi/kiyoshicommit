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

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.l1j.configure.Config;
import jp.l1j.server.ClientThread;
import jp.l1j.server.controller.timer.WarTimeController;
import jp.l1j.server.datatables.CharacterTable;
import jp.l1j.server.datatables.ClanTable;
import jp.l1j.server.datatables.HouseTable;
import jp.l1j.server.datatables.NpcTable;
import jp.l1j.server.datatables.PetTable;
import jp.l1j.server.model.L1CastleLocation;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.L1ChatParty;
import jp.l1j.server.model.L1Clan;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.L1Party;
import jp.l1j.server.model.L1Quest;
import jp.l1j.server.model.L1Teleport;
import jp.l1j.server.model.L1War;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.instance.L1PetInstance;
import jp.l1j.server.model.item.L1ItemId;
import jp.l1j.server.model.map.L1Map;
import jp.l1j.server.packets.server.S_ChangeName;
import jp.l1j.server.packets.server.S_CharTitle;
import jp.l1j.server.packets.server.S_CharVisualUpdate;
import jp.l1j.server.packets.server.S_MessageYN;
import jp.l1j.server.packets.server.S_OwnCharStatus2;
import jp.l1j.server.packets.server.S_PacketBox;
import jp.l1j.server.packets.server.S_Resurrection;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.packets.server.S_Trade;
import jp.l1j.server.templates.L1House;
import jp.l1j.server.templates.L1Npc;
import jp.l1j.server.templates.L1Pet;

// Referenced classes of package jp.l1j.server.clientpackets:
// ClientBasePacket

public class C_Attr extends ClientBasePacket {

	private static Logger _log = Logger.getLogger(C_Attr.class.getName());
	private static final String C_ATTR = "[C] C_Attr";

	private static final int HEADING_TABLE_X[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
	private static final int HEADING_TABLE_Y[] = { -1, -1, 0, 1, 1, 1, 0, -1 };

	public C_Attr(byte abyte0[], ClientThread clientthread) throws Exception {
		super(abyte0);
		int i = readH();
		int attrcode;

		if(i == 479) {
			attrcode = i;
		} else {
			int count = readD();
			attrcode = readH();
		}

		String name;
		int c;

		L1Clan clan;
		L1PcInstance clanMember[];
		String clan_name;
		String clan_member_name[];
		boolean loginLeader;

		L1PcInstance pc = clientthread.getActiveChar();

		switch (attrcode) {
		case 97: // %0が血盟に加入したがっています。承諾しますか？（Y/N）
			c = readH();
			L1PcInstance joinPc = (L1PcInstance) L1World.getInstance().findObject(pc.getTempID());
			pc.setTempID(0);
			if (joinPc != null) {
				if (c == 0) { // No
					joinPc.sendPackets(new S_ServerMessage(96, pc.getName()));
					// \f1%0はあなたの要請を拒絶しました。
				} else if (c == 1) { // Yes
					int clan_id = pc.getClanId();
					String clanName = pc.getClanName();
					clan = L1World.getInstance().getClan(clanName);
					if (clan != null) {
						int maxMember = 0;
						int charisma = pc.getCha();
						boolean lv45quest = false;
						if (pc.getQuest().isEnd(L1Quest.QUEST_LEVEL45)) {
							lv45quest = true;
						}
						if (pc.getLevel() >= 50) { // Lv50以上
							if (lv45quest == true) { // Lv45クエストクリア済み
								maxMember = charisma * 9;
							} else {
								maxMember = charisma * 3;
							}
						} else { // Lv50未満
							if (lv45quest == true) { // Lv45クエストクリア済み
								maxMember = charisma * 6;
							} else {
								maxMember = charisma * 2;
							}
						}
						if (Config.MAX_CLAN_MEMBER > 0 && Config.MAX_CLAN_MEMBER < maxMember) {
							// Clan人数の上限の設定あり
							maxMember = Config.MAX_CLAN_MEMBER;
						}

						if (joinPc.getClanId() == 0) { // クラン未加入
							String clanMembersName[] = clan.getAllMembers();
							if (maxMember <= clanMembersName.length) { // 空きがない
								// %0はあなたを血盟員として受け入れることができません。
								joinPc.sendPackets(new S_ServerMessage(188, pc.getName()));
								return;
							}
							for (L1PcInstance clanMembers : clan
									.getOnlineClanMember()) {
								// \f1%0が血盟の一員として受け入れられました。
								clanMembers.sendPackets(new S_ServerMessage(94, joinPc.getName()));
							}
							joinPc.setClanid(clan_id);
							joinPc.setClanname(clanName);
							joinPc.setClanRank(L1Clan.CLAN_RANK_REGULAR);
							joinPc.setTitle("");
							joinPc.setRejoinClanTime(null);
							joinPc.sendPackets(new S_CharTitle(joinPc.getId(), ""));
							joinPc.broadcastPacket(new S_CharTitle(joinPc.getId(), ""));
							joinPc.save(); // DBにキャラクター情報を書き込む
							clan.addMemberName(joinPc.getName());
							joinPc.sendPackets(new S_ServerMessage(95,clanName));
							// \f1%0血盟に加入しました。
						} else { // クラン加入済み（クラン連合）
							if (Config.CLAN_ALLIANCE) {
								changeClan(clientthread, pc, joinPc, maxMember);
							} else {
								joinPc.sendPackets(new S_ServerMessage(89)); // \f1あなたはすでに血盟に加入しています。
							}
						}
					}
				}
			}
			break;

		case 453: // 本当に解散してもよろしいですか？（Y/N）
			c = readH();
			if (c == 0) { // No
				return;
			}
			clan_name = pc.getClanName();
			clan = L1World.getInstance().getClan(clan_name);
			clan_member_name = clan.getAllMembers();
			for (i = 0; i < clan_member_name.length; i++) { // クラン員のクラン情報をクリア
				L1PcInstance online_pc = L1World.getInstance().getPlayer(clan_member_name[i]);
				if (online_pc != null) { // オンライン中のクラン員
					online_pc.setClanid(0);
					online_pc.setClanname("");
					online_pc.setClanRank(0);
					online_pc.setTitle("");
					online_pc.sendPackets(new S_CharTitle(online_pc.getId(), ""));
					online_pc.broadcastPacket(new S_CharTitle(online_pc.getId(), ""));
					online_pc.save(); // DBにキャラクター情報を書き込む
					online_pc.sendPackets(new S_ServerMessage(269, pc.getName(), clan_name));
					// %1血盟の血盟主%0が血盟を解散させました。
				} else { // オフライン中のクラン員
					try {
						L1PcInstance offline_pc = CharacterTable.getInstance().restoreCharacter(
										clan_member_name[i]);
						offline_pc.setClanid(0);
						offline_pc.setClanname("");
						offline_pc.setClanRank(0);
						offline_pc.setTitle("");
						offline_pc.save(); // DBにキャラクター情報を書き込む
					} catch (Exception e) {
						_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				}
			}
			String emblem_file = String.valueOf(pc.getClanId());
			File file = new File("emblem/" + emblem_file);
			file.delete();
			ClanTable.getInstance().deleteClan(clan_name);
			break;

		case 1906: // 血盟戦中に任意で脱退した場合、3日間血盟加入ができなくなります。(君主の同意要請はY、任意脱退はN)
			c = readH();
			if (c == 0) { // No
				leaveClan(pc, false);
			} else { // Yes
				loginLeader = false;
				clan_name = pc.getClanName();
				clan = L1World.getInstance().getClan(clan_name);
				clanMember = clan.getOnlineClanMember();
				for (i = 0; i < clanMember.length; i++) {
					if (clanMember[i].getClanRank() == L1Clan.CLAN_RANK_LEADER
							|| clanMember[i].getClanRank() == L1Clan.CLAN_RANK_SUBLEADER) {
						// %0%sが血盟からの脱退を申請しました。承諾しますか？
						clanMember[i].setTempID(pc.getId()); // 相手のオブジェクトIDを保存しておく
						clanMember[i].sendPackets(new S_MessageYN(1908, pc.getName()));
						loginLeader = true;
					}
				}
				if (loginLeader) {
					// 君主/副君主に血盟脱退の承認を要請中です。しばらくお待ちください。
					pc.sendPackets(new S_ServerMessage(302));
				} else {
					// 君主/副君主はオフラインです。任意で脱退しますか？
					pc.sendPackets(new S_MessageYN(1914));
				}
			}
			break;

		case 1908: // %0%sが血盟からの脱退を申請しました。承諾しますか？
			L1PcInstance leavePc = (L1PcInstance) L1World.getInstance().findObject(pc.getTempID());
			c = readH();
			if (c == 0) { // No
				clan_name = pc.getClanName();
				clan = L1World.getInstance().getClan(clan_name);
				clanMember = clan.getOnlineClanMember();
				for (i = 0; i < clanMember.length; i++) {
					// %0君主/副君主が%1の血盟脱退を拒否しました。
					clanMember[i].sendPackets(new S_ServerMessage(1917, pc.getName(), leavePc.getName()));
				}
				// 君主が血盟脱退を拒否しました。任意で脱退しますか？
				leavePc.sendPackets(new S_MessageYN(1912));
			} else { // Yes
				clan_name = pc.getClanName();
				clan = L1World.getInstance().getClan(clan_name);
				clanMember = clan.getOnlineClanMember();
				for (i = 0; i < clanMember.length; i++) {
					// %0君主/副君主が%1の血盟脱退を承認しました。
					clanMember[i].sendPackets(new S_ServerMessage(178, pc.getName(), leavePc.getName()));
				}
				leaveClan(leavePc, true);
			}
			break;

		case 1912: // 君主が血盟脱退を拒否しました。任意で脱退しますか？
		case 1914: // 君主/副君主はオフラインです。任意で脱退しますか？
			c = readH();
			if (c == 0) { // No
				return;
			}
			leaveClan(pc, false);
			break;

		case 217: // %0血盟の%1があなたの血盟との戦争を望んでいます。戦争に応じますか？（Y/N）
		case 221: // %0血盟が降伏を望んでいます。受け入れますか？（Y/N）
		case 222: // %0血盟が戦争の終結を望んでいます。終結しますか？（Y/N）
			c = readH();
			L1PcInstance enemyLeader = (L1PcInstance) L1World.getInstance().findObject(pc.getTempID());
			if (enemyLeader == null) {
				return;
			}
			pc.setTempID(0);
			String clanName = pc.getClanName();
			String enemyClanName = enemyLeader.getClanName();
			if (c == 0) { // No
				if (attrcode == 217) {
					enemyLeader.sendPackets(new S_ServerMessage(236, clanName)); // %0血盟があなたの血盟との戦争を拒絶しました。
				} else if (attrcode == 221 || attrcode == 222) {
					enemyLeader.sendPackets(new S_ServerMessage(237, clanName)); // %0血盟があなたの提案を拒絶しました。
				}
			} else if (c == 1) { // Yes
				if (attrcode == 217) {
					L1War war = new L1War();
					war.handleCommands(2, enemyClanName, clanName); // 模擬戦開始
				} else if (attrcode == 221 || attrcode == 222) {
					// 全戦争リストを取得
					for (L1War war : L1World.getInstance().getWarList()) {
						if (war.CheckClanInWar(clanName)) { // 自クランが行っている戦争を発見
							if (attrcode == 221) {
								war.SurrenderWar(enemyClanName, clanName); // 降伏
							} else if (attrcode == 222) {
								war.CeaseWar(enemyClanName, clanName); // 終結
							}
							break;
						}
					}
				}
			}
			break;

		case 252: // %0%sがあなたとアイテムの取引を望んでいます。取引しますか？（Y/N）
			c = readH();
			L1PcInstance trading_partner = (L1PcInstance) L1World.getInstance()
					.findObject(pc.getTradeID());
			if (trading_partner != null) {
				if (c == 0) // No
				{
					trading_partner.sendPackets(new S_ServerMessage(253, pc
							.getName())); // %0%dはあなたとの取引に応じませんでした。
					pc.setTradeID(0);
					trading_partner.setTradeID(0);
				} else if (c == 1) // Yes
				{
					pc.sendPackets(new S_Trade(trading_partner.getName()));
					trading_partner.sendPackets(new S_Trade(pc.getName()));
				}
			}
			break;

		case 321: // また復活したいですか？（Y/N）
			c = readH();
			L1PcInstance resusepc1 = (L1PcInstance) L1World.getInstance()
					.findObject(pc.getTempID());
			pc.setTempID(0);
			if (resusepc1 != null) { // 復活スクロール
				if (c == 0) { // No
					;
				} else if (c == 1) { // Yes
					pc.sendPackets(new S_SkillSound(pc.getId(), '\346'));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), '\346'));
					// pc.resurrect(pc.getLevel());
					// pc.setCurrentHp(pc.getLevel());
					pc.resurrect(pc.getMaxHp() / 2);
					pc.setCurrentHp(pc.getMaxHp() / 2);
					pc.startHpRegeneration();
					pc.startMpRegeneration();
					pc.startHpRegenerationByDoll();
					pc.startMpRegenerationByDoll();
					pc.stopPcDeleteTimer();
					pc.sendPackets(new S_Resurrection(pc, resusepc1, 0));
					pc.broadcastPacket(new S_Resurrection(pc, resusepc1, 0));
					pc.sendPackets(new S_CharVisualUpdate(pc));
					pc.broadcastPacket(new S_CharVisualUpdate(pc));
				}
			}
			break;

		case 322: // また復活したいですか？（Y/N）
			c = readH();
			L1PcInstance resusepc2 = (L1PcInstance) L1World.getInstance()
					.findObject(pc.getTempID());
			pc.setTempID(0);
			if (resusepc2 != null) { // 祝福された 復活スクロール、リザレクション、グレーター リザレクション
				if (c == 0) { // No
					;
				} else if (c == 1) { // Yes
					pc.sendPackets(new S_SkillSound(pc.getId(), '\346'));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), '\346'));
					pc.resurrect(pc.getMaxHp());
					pc.setCurrentHp(pc.getMaxHp());
					pc.startHpRegeneration();
					pc.startMpRegeneration();
					pc.startMpRegenerationByDoll();
					pc.stopPcDeleteTimer();
					pc.sendPackets(new S_Resurrection(pc, resusepc2, 0));
					pc.broadcastPacket(new S_Resurrection(pc, resusepc2, 0));
					pc.sendPackets(new S_CharVisualUpdate(pc));
					pc.broadcastPacket(new S_CharVisualUpdate(pc));
					// EXPロストしている、G-RESを掛けられた、EXPロストした死亡
					// 全てを満たす場合のみEXP復旧
					if (pc.getExpRes() == 1 && pc.isGres() && pc.isGresValid()) {
						pc.resExp();
						pc.setExpRes(0);
						pc.setGres(false);
					}
				}
			}
			break;

		case 325: // 動物の名前を決めてください：
			c = readH(); // ?
			name = readS(-1); // TODO 名前が1byte分欠けてしまう不具合の対応
			L1PetInstance pet = (L1PetInstance) L1World.getInstance()
					.findObject(pc.getTempID());
			pc.setTempID(0);
			renamePet(pet, name);
			break;

		case 512: // 家の名前は？
			c = readH(); // ?
			name = readS();
			int houseId = pc.getTempID();
			pc.setTempID(0);
			if (name.length() <= 16) {
				L1House house = HouseTable.getInstance().getHouseTable(houseId);
				house.setHouseName(name);
				HouseTable.getInstance().updateHouse(house); // DBに書き込み
			} else {
				pc.sendPackets(new S_ServerMessage(513)); // 家の名前が長すぎます。
			}
			break;

		case 630: // %0%sがあなたと決闘を望んでいます。応じますか？（Y/N）
			c = readH();
			L1PcInstance fightPc = (L1PcInstance) L1World.getInstance()
					.findObject(pc.getFightId());
			if (c == 0) {
				pc.setFightId(0);
				fightPc.setFightId(0);
				fightPc.sendPackets(new S_ServerMessage(631, pc.getName())); // %0%dがあなたとの決闘を断りました。
			} else if (c == 1) {
				fightPc.sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL,
						fightPc.getFightId(), fightPc.getId()));
				pc.sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL, pc
						.getFightId(), pc.getId()));
			}
			break;

		case 653: // 離婚をするとリングは消えてしまいます。離婚を望みますか？（Y/N）
			c = readH();
			L1PcInstance target653 = (L1PcInstance) L1World.getInstance()
					.findObject(pc.getPartnerId());
			if (c == 0) { // No
				return;
			} else if (c == 1) { // Yes
				if (target653 != null) {
					target653.setPartnerId(0);
					target653.save();
					target653.sendPackets(new S_ServerMessage(662)); // \f1あなたは結婚していません。
				} else {
					CharacterTable.getInstance().updatePartnerId(pc
							.getPartnerId());
				}
			}
			pc.setPartnerId(0);
			pc.save(); // DBにキャラクター情報を書き込む
			pc.sendPackets(new S_ServerMessage(662)); // \f1あなたは結婚していません。
			break;

		case 654: // %0%sあなたと結婚したがっています。%0と結婚しますか？（Y/N）
			c = readH();
			L1PcInstance partner = (L1PcInstance) L1World.getInstance()
					.findObject(pc.getTempID());
			pc.setTempID(0);
			if (partner != null) {
				if (c == 0) { // No
					partner.sendPackets(new S_ServerMessage( // %0%sはあなたとの結婚を拒絶しました。
							656, pc.getName()));
				} else if (c == 1) { // Yes
					pc.setPartnerId(partner.getId());
					pc.save();
					pc.sendPackets(new S_ServerMessage( // 皆の祝福の中で、二人の結婚が行われました。
							790));
					pc.sendPackets(new S_ServerMessage( // おめでとうございます！%0と結婚しました。
							655, partner.getName()));

					partner.setPartnerId(pc.getId());
					partner.save();
					partner.sendPackets(new S_ServerMessage( // 皆の祝福の中で、二人の結婚が行われました。
							790));
					partner.sendPackets(new S_ServerMessage( // おめでとうございます！%0と結婚しました。
							655, pc.getName()));
				}
			}
			break;

		// コールクラン
		case 729: // 君主が呼んでいます。召喚に応じますか？（Y/N）
			c = readH();
			if (c == 0) { // No
				;
			} else if (c == 1) { // Yes
				callClan(pc);
			}
			break;

		case 738: // 経験値を回復するには%0のアデナが必要です。経験値を回復しますか？
			c = readH();
			if (c == 0) { // No
				;
			} else if (c == 1 && pc.getExpRes() == 1) { // Yes
				int level = pc.getLevel();
				int cost = level * level * 100;
				if (pc.getInventory().consumeItem(L1ItemId.ADENA, cost)) {
					pc.resExp();
					pc.setExpRes(0);
				} else {
					pc.sendPackets(new S_ServerMessage(189)); // \f1アデナが不足しています。
				}
			}
			break;

		case 951: // チャットパーティー招待を許可しますか？（Y/N）
			c = readH();
			L1PcInstance chatPc = (L1PcInstance) L1World.getInstance()
					.findObject(pc.getPartyID());
			if (chatPc != null) {
				if (c == 0) { // No
					chatPc.sendPackets(new S_ServerMessage(423, pc.getName())); // %0が招待を拒否しました。
					pc.setPartyID(0);
				} else if (c == 1) { // Yes
					if (chatPc.isInChatParty()) {
						if (chatPc.getChatParty().isVacancy() || chatPc
								.isGm()) {
							chatPc.getChatParty().addMember(pc);
						} else {
							chatPc.sendPackets(new S_ServerMessage(417)); // これ以上パーティーメンバーを受け入れることはできません。
						}
					} else {
						L1ChatParty chatParty = new L1ChatParty();
						chatParty.addMember(chatPc);
						chatParty.addMember(pc);
						chatPc.sendPackets(new S_ServerMessage(424, pc
								.getName())); // %0がパーティーに入りました。
					}
				}
			}
			break;

		case 953: // パーティー招待を許可しますか？（Y/N）
			c = readH();
			L1PcInstance target = (L1PcInstance) L1World.getInstance()
					.findObject(pc.getPartyID());
			if (target != null) {
				if (c == 0) // No
				{
					target.sendPackets(new S_ServerMessage(423, pc.getName())); // %0が招待を拒否しました。
					pc.setPartyID(0);
				} else if (c == 1) // Yes
				{
					if (target.isInParty()) {
						// 招待主がパーティー中
						if (target.getParty().isVacancy() || target.isGm()) {
							// パーティーに空きがある
							target.getParty().addMember(pc);
						} else {
							// パーティーに空きがない
							target.sendPackets(new S_ServerMessage(417)); // これ以上パーティーメンバーを受け入れることはできません。
						}
					} else {
						// 招待主がパーティー中でない
						L1Party party = new L1Party();
						party.addMember(target);
						party.addMember(pc);
						target.sendPackets(new S_ServerMessage(424, pc
								.getName())); // %0がパーティーに入りました。
					}
				}
			}
			break;

		case 954: // 自動分配のパーティ招待を許可しますか？(Y/N)
			c = readH();
			L1PcInstance target2 = (L1PcInstance) L1World.getInstance().findObject(pc.getPartyID());
			if (target2 != null) {
				if (c == 0) { // No
					target2.sendPackets(new S_ServerMessage(423, pc.getName())); // %0が招待を拒否しました。
					pc.setPartyID(0);
				} else if (c == 1) { // Yes
					if (target2.isInParty()) {
						// 招待主がパーティー中
						if (target2.getParty().isVacancy() || target2.isGm()) {
							// パーティーに空きがある
							target2.getParty().addMember(pc);
						} else {
							// パーティーに空きがない
							target2.sendPackets(new S_ServerMessage(417)); // これ以上パーティーメンバーを受け入れることはできません。
						}
					} else {
						// 招待主がパーティー中でない
						L1Party party = new L1Party();
						party.addMember(target2);
						party.addMember(pc);
						target2.sendPackets(new S_ServerMessage(424, pc
								.getName())); // %0がパーティーに入りました。
					}
				}
			}
			break;

		case 479: // どの能力値を向上させますか？（str、dex、int、con、wis、cha）
			if (readC() == 1) {
				String s = readS();
				if (!(pc.getLevel() - 50 > pc.getBonusStats())) {
					return;
				}
				if (s.toLowerCase().equals("str".toLowerCase())) {
					// if(l1pcinstance.getStr() < 255)
					if (pc.getBaseStr() < 35) {
						pc.addBaseStr((byte) 1); // 素のSTR値に+1
						pc.setBonusStats(pc.getBonusStats() + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.save(); // DBにキャラクター情報を書き込む
					} else {
						pc.sendPackets(new S_ServerMessage(481));
					}
				} else if (s.toLowerCase().equals("dex".toLowerCase())) {
					// if(l1pcinstance.getDex() < 255)
					if (pc.getBaseDex() < 35) {
						pc.addBaseDex((byte) 1); // 素のDEX値に+1
						pc.resetBaseAc();
						pc.setBonusStats(pc.getBonusStats() + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.save(); // DBにキャラクター情報を書き込む
					} else {
						pc.sendPackets(new S_ServerMessage(481)); // 一つの能力値の最大値は25です。他の能力値を選択してください
					}
				} else if (s.toLowerCase().equals("con".toLowerCase())) {
					// if(l1pcinstance.getCon() < 255)
					if (pc.getBaseCon() < 35) {
						pc.addBaseCon((byte) 1); // 素のCON値に+1
						pc.setBonusStats(pc.getBonusStats() + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.save(); // DBにキャラクター情報を書き込む
					} else {
						pc.sendPackets(new S_ServerMessage(481)); // 一つの能力値の最大値は25です。他の能力値を選択してください
					}
				} else if (s.toLowerCase().equals("int".toLowerCase())) {
					// if(l1pcinstance.getInt() < 255)
					if (pc.getBaseInt() < 35) {
						pc.addBaseInt((byte) 1); // 素のINT値に+1
						pc.setBonusStats(pc.getBonusStats() + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.save(); // DBにキャラクター情報を書き込む
					} else {
						pc.sendPackets(new S_ServerMessage(481)); // 一つの能力値の最大値は25です。他の能力値を選択してください
					}
				} else if (s.toLowerCase().equals("wis".toLowerCase())) {
					// if(l1pcinstance.getWis() < 255)
					if (pc.getBaseWis() < 35) {
						pc.addBaseWis((byte) 1); // 素のWIS値に+1
						pc.resetBaseMr();
						pc.setBonusStats(pc.getBonusStats() + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.save(); // DBにキャラクター情報を書き込む
					} else {
						pc.sendPackets(new S_ServerMessage(481)); // 一つの能力値の最大値は25です。他の能力値を選択してください
					}
				} else if (s.toLowerCase().equals("cha".toLowerCase())) {
					// if(l1pcinstance.getCha() < 255)
					if (pc.getBaseCha() < 35) {
						pc.addBaseCha((byte) 1); // 素のCHA値に+1
						pc.setBonusStats(pc.getBonusStats() + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.save(); // DBにキャラクター情報を書き込む
					} else {
						pc.sendPackets(new S_ServerMessage(481)); // 一つの能力値の最大値は25です。他の能力値を選択してください
					}
				}
			}
			break;
			// TODO ペットレース用　予約者名簿start
		case 1256:
			jp.l1j.server.model.L1PolyRace.getInstance().requsetAttr(pc, readC());
			break;
			// TODO ペットレース用　予約者名簿 end
		case 1268://デスマッチ
			if(pc.getLevel()>51){
				//jp.l1j.server.model.L1DeathMatchExpert.getInstance().requsetAttr(pc, readC());
			}else{
				jp.l1j.server.model.L1DeathMatch.getInstance().requsetAttr(pc, readC());
			}
			break;
		default:
			break;
		}
	}

	private void leaveClan(L1PcInstance leavePc, boolean isApproved) {
		String clan_name = leavePc.getClanName();
		L1Clan clan = L1World.getInstance().getClan(clan_name);
		L1PcInstance clanMember[] = clan.getOnlineClanMember();
		for (int i = 0; i < clanMember.length; i++) {
			clanMember[i].sendPackets(new S_ServerMessage(178, leavePc.getName(), clan_name));
			// \f1%0が%1血盟を脱退しました。
		}
		if (clan.getWarehouseUsingChar() == leavePc.getId()) { // 脱退するキャラがクラン倉庫使用中
			clan.setWarehouseUsingChar(0); // クラン倉庫のロックを解除
		}
		boolean inWar = false;
		List<L1War> warList = L1World.getInstance().getWarList(); // 全戦争リストを取得
		for (L1War war : warList) {
			if (war.CheckClanInWar(leavePc.getClanName())) { // 自クランが既に戦争中
				inWar = true;
				break;
			}
		}
		try {
			if (inWar) {
				long time = 0; // 再加入時間(ミリ秒)
				if (isApproved) { // 君主または副君主による承認済の脱退
					time = 60 * 60 * 2 * 1000; // 2時間
				} else { // 任意脱退
					time = 60 * 60 * 24 * 3 * 1000; // 3日間
				}
				leavePc.setRejoinClanTime(new Timestamp(System.currentTimeMillis() + time));
			}
			leavePc.setClanid(0);
			leavePc.setClanname("");
			leavePc.setClanRank(0);
			leavePc.setTitle("");
			leavePc.sendPackets(new S_CharTitle(leavePc.getId(), ""));
			leavePc.broadcastPacket(new S_CharTitle(leavePc.getId(), ""));
			leavePc.save(); // DBにキャラクター情報を書き込む
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		clan.delMemberName(leavePc.getName());
	}

	private void changeClan(ClientThread clientthread, L1PcInstance pc,
					L1PcInstance joinPc, int maxMember) {
		try {
			int clanId = pc.getClanId();
			String clanName = pc.getClanName();
			L1Clan clan = L1World.getInstance().getClan(clanName);
			String clanMemberName[] = clan.getAllMembers();
			int clanNum = clanMemberName.length;

			int oldClanId = joinPc.getClanId();
			String oldClanName = joinPc.getClanName();
			L1Clan oldClan = L1World.getInstance().getClan(oldClanName);
			String oldClanMemberName[] = oldClan.getAllMembers();
			int oldClanNum = oldClanMemberName.length;
			if (clan != null && oldClan != null && joinPc.isCrown() && // 自分が君主
					joinPc.getId() == oldClan.getLeaderId()) {
				if (maxMember < clanNum + oldClanNum) { // 空きがない
					joinPc.sendPackets( // %0はあなたを血盟員として受け入れることができません。
							new S_ServerMessage(188, pc.getName()));
					return;
				}
				L1PcInstance clanMember[] = clan.getOnlineClanMember();
				for (int cnt = 0; cnt < clanMember.length; cnt++) {
					clanMember[cnt].sendPackets(new S_ServerMessage(94, joinPc
							.getName())); // \f1%0が血盟の一員として受け入れられました。
				}

				for (int i = 0; i < oldClanMemberName.length; i++) {
					L1PcInstance oldClanMember = L1World.getInstance().getPlayer(oldClanMemberName[i]);
					if (oldClanMember == null) {
						oldClanMember = CharacterTable.getInstance().restoreCharacter(oldClanMemberName[i]);
					}
					oldClanMember.setClanid(clanId);
					oldClanMember.setClanname(clanName);
					// 血盟連合に加入した君主は、副君主
					// 君主が連れてきた血盟員は、一般血盟員
					if (oldClanMember.getId() == joinPc.getId()) {
						oldClanMember.setClanRank(L1Clan.CLAN_RANK_SUBLEADER);
					} else {
						oldClanMember.setClanRank(L1Clan.CLAN_RANK_REGULAR);
					}
					oldClanMember.setTitle("");
					oldClanMember.setRejoinClanTime(null);
					oldClanMember.save();
					clan.addMemberName(oldClanMember.getName());
					if (oldClanMember.getOnlineStatus() == 1) {
						oldClanMember.sendPackets(new S_CharTitle(joinPc.getId(), ""));
						oldClanMember.broadcastPacket(new S_CharTitle(joinPc.getId(), ""));
						oldClanMember.sendPackets(new S_ServerMessage(95, clanName));
						// \f1%0血盟に加入しました。
					}
				}
				// 旧クラン削除
				String emblem_file = String.valueOf(oldClanId);
				File file = new File("emblem/" + emblem_file);
				file.delete();
				ClanTable.getInstance().deleteClan(oldClanName);
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private static void renamePet(L1PetInstance pet, String name) {
		if (pet == null || name == null) {
			throw new NullPointerException();
		}

		int petItemObjId = pet.getItemObjId();
		L1Pet petTemplate = PetTable.getInstance().getTemplate(petItemObjId);
		if (petTemplate == null) {
			throw new NullPointerException();
		}

		L1PcInstance pc = (L1PcInstance) pet.getMaster();
		if (PetTable.isNameExists(name)) {
			pc.sendPackets(new S_ServerMessage(327)); // 同じ名前がすでに存在しています。
			return;
		}
		if (!Config.RENAME_PET_NAME) {
			L1Npc l1npc = NpcTable.getInstance().getTemplate(pet.getNpcId());
			if (!(pet.getName().equalsIgnoreCase(l1npc.getName())) ) {
				pc.sendPackets(new S_ServerMessage(326)); // 一度決めた名前は変更できません。
				return;
			}
		}
 		pet.setName(name);
		petTemplate.setName(name);
		PetTable.getInstance().storePet(petTemplate); // DBに書き込み
		L1ItemInstance item = pc.getInventory().getItem(pet.getItemObjId());
		pc.getInventory().updateItem(item);
		pc.sendPackets(new S_ChangeName(pet.getId(), name));
		pc.broadcastPacket(new S_ChangeName(pet.getId(), name));
	}

	private void callClan(L1PcInstance pc) {
		L1PcInstance callClanPc = (L1PcInstance) L1World.getInstance()
				.findObject(pc.getTempID());
		pc.setTempID(0);
		if (callClanPc == null) {
			return;
		}
		if (!pc.getMap().isEscapable() && !pc.isGm()) {
			// 周辺のエネルギーがテレポートを妨害しています。そのため、ここでテレポートは使用できません。
			pc.sendPackets(new S_ServerMessage(647));
			L1Teleport.teleport(pc, pc.getLocation(), pc.getHeading(), false);
			return;
		}
		if (pc.getId() != callClanPc.getCallClanId()) {
			return;
		}

		boolean isInWarArea = false;
		int castleId = L1CastleLocation.getCastleIdByArea(callClanPc);
		if (castleId != 0) {
			isInWarArea = true;
			if (WarTimeController.getInstance().isNowWar(castleId)) {
				isInWarArea = false; // 戦争時間中は旗内でも使用可能
			}
		}
		short mapId = callClanPc.getMapId();
		if (mapId != 0 && mapId != 4 && mapId != 304 || isInWarArea) {
			// \f1あなたのパートナーは今あなたが行けない所でプレイ中です。
			pc.sendPackets(new S_ServerMessage(547));
			return;
		}

		L1Map map = callClanPc.getMap();
		int locX = callClanPc.getX();
		int locY = callClanPc.getY();
		int heading = callClanPc.getCallClanHeading();
		locX += HEADING_TABLE_X[heading];
		locY += HEADING_TABLE_Y[heading];
		heading = (heading + 4) % 4;

		boolean isExsistCharacter = false;
		for (L1Object object : L1World.getInstance()
				.getVisibleObjects(callClanPc, 1)) {
			if (object instanceof L1Character) {
				L1Character cha = (L1Character) object;
				if (cha.getX() == locX && cha.getY() == locY
						&& cha.getMapId() == mapId) {
					isExsistCharacter = true;
					break;
				}
			}
		}

		if (locX == 0 && locY == 0 || !map.isPassable(locX, locY)
				|| isExsistCharacter) {
			// 障害物があってそこまで移動することができません。
			pc.sendPackets(new S_ServerMessage(627));
			return;
		}
		L1Teleport.teleport(pc, locX, locY, mapId, heading, true, L1Teleport
				.CALL_CLAN);
	}

	@Override
	public String getType() {
		return C_ATTR;
	}
}