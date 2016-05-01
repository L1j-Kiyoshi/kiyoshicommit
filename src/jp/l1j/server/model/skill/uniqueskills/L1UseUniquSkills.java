package jp.l1j.server.model.skill.uniqueskills;

import jp.l1j.server.GeneralThreadPool;
import jp.l1j.server.model.L1Character;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1Kenesi;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1KenesiS;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1Musesim;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1Musesome;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1Nutssim;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1Nutssome;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1Rirafu;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1Ruota;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1Saylalaf;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1Thisetol;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1Tifsim;
import jp.l1j.server.model.skill.uniqueskills.antharas.L1Tifsome;

public class L1UseUniquSkills {

	private static L1UseUniquSkills _instance = null;

	public static synchronized L1UseUniquSkills getInstance() {
		if (_instance == null) {
			_instance = new L1UseUniquSkills();
		}
		return _instance;
	}

	public void useSkills(int skillId, L1Character user, L1Character target) {

		if (skillId == 4500) { // オーブモーク！ ケネシ
			L1Kenesi kenesi = new L1Kenesi(user, target);
			GeneralThreadPool.getInstance().execute(kenesi);
		} else if (skillId == 4501) { // オーブモーク！  リラフ
			L1Rirafu rirafu = new L1Rirafu(user, target);
			GeneralThreadPool.getInstance().execute(rirafu);
		} else if (skillId == 4502) { // オーブモーク！ ルオタ
			L1Ruota ruota = new L1Ruota(user, target);
			GeneralThreadPool.getInstance().execute(ruota);
		} else if (skillId == 4503) { // オーブモーク！リオタ
		} else if (skillId == 4504) { // オーブモーク！  ティセトル
			L1Thisetol thisetol = new L1Thisetol(user);
			GeneralThreadPool.getInstance().execute(thisetol);
		} else if (skillId == 4505) { // オーブモーク！  ケネシ(強化)
			L1KenesiS kenesiS = new L1KenesiS(user, target);
			GeneralThreadPool.getInstance().execute(kenesiS);
		} else if (skillId == 4506) { // オーブモーク！  ミューズサム
			L1Musesome musesome = new L1Musesome(user, target);
			GeneralThreadPool.getInstance().execute(musesome);
		} else if (skillId == 4507) { // オーブモーク！  ミューズシム
			L1Musesim musesim = new L1Musesim(user, target);
			GeneralThreadPool.getInstance().execute(musesim);
		} else if (skillId == 4508) { // オーブモーク！ ティギル
		} else if (skillId == 4509) { // オーブモーク！ ティギロ
		} else if (skillId == 4510) { // オーブモーク！ ケンティギル
		} else if (skillId == 4511) { // オーブモーク！ ケンティギロ
		} else if (skillId == 4512) { // オーブモーク！ナッツサム
			L1Nutssome nutssim = new L1Nutssome(user, target);
			GeneralThreadPool.getInstance().execute(nutssim);
		} else if (skillId == 4513) { // オーブモーク！ナッツシム
			L1Nutssim nutssim = new L1Nutssim(user, target);
			GeneralThreadPool.getInstance().execute(nutssim);
		} else if (skillId == 4514) { // オーブモーク！ケンロウ
		} else if (skillId == 4515) { // オーブモーク！ケンルウ
		} else if (skillId == 4516) { // オーブモーク！セイララフ
			L1Saylalaf saylalaf = new L1Saylalaf(user, target);
			GeneralThreadPool.getInstance().execute(saylalaf);
		} else if (skillId == 4517) { // オーブモーク！セイリラフ
		} else if (skillId == 4518) { // オーブモーク！ティフサム
			L1Tifsome tifsome = new L1Tifsome(user, target);
			GeneralThreadPool.getInstance().execute(tifsome);
		} else if (skillId == 4519) { // オーブモーク！ティフシム
			L1Tifsim tifsim = new L1Tifsim(user, target);
			GeneralThreadPool.getInstance().execute(tifsim);
		}
	}
}
