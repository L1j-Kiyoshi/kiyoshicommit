package jp.l1j.server.model.npc.action;

public class L1CreateNpcHandler {

	private static L1CreateNpcHandler _instance = null;

	public static synchronized L1CreateNpcHandler getInstance() {
		if (_instance == null) {
			_instance = new L1CreateNpcHandler();
		}
		return _instance;
	}

	public String getReturnTalk(int npcId) throws Exception {
		String htmlid = "";
		if (npcId == 70642) { // 鍛冶屋ヘクター
			htmlid = "l1jhector01";
		} else if (npcId == 70690) { // バミュト
			htmlid = "l1jbamute01";
		} else if (npcId == 70028) { // ポーション商人 ランダル
			htmlid = "l1jrandal01";
		} else if (npcId == 70641) { // 裁断師 ヘルベルト（ハーバート）
			htmlid = "l1jherbert01";
		} else if (npcId == 99020) { // アイテム製作師^ゾウのラヴァゴーレム
			htmlid = "l1jrushi01";
		} else if (npcId == 91057) { // アイテム製作師^強靭なハイオス
			htmlid = "l1jhighos01";
		} else if (npcId == 91058) { // アイテム製作師^細やかなシューヌ
			htmlid = "l1jshune01";
		} else if (npcId == 91059) { // アイテム製作師^根気のあるドオホ
			htmlid = "l1jduoho01";
		} else if (npcId == 91060) { // アイテム製作師^派手なバエミ
			htmlid = "l1jbaemi01";
		} else if (npcId == 91061) { // アイテム製作師^シュエルメ
			htmlid = "l1jsherme01";
		} else if (npcId == 70762) { // アイテム製作師^カリフ
			htmlid = "l1jkarif01";
		} else if (npcId == 70904) { // 鍛冶屋^クプ
			htmlid = "l1jkoup01";
		} else if (npcId == 70662) { // アイテム製作師^イベルビン
			htmlid = "l1jivelviin1";
		}
		return htmlid;
	}
}
