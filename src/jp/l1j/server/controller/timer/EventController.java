package jp.l1j.server.controller.timer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.l1j.server.datatables.BossSpawnTable;
import jp.l1j.server.datatables.EventTable;
import jp.l1j.server.datatables.SpawnNpcTable;
import jp.l1j.server.datatables.SpawnTable;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.templates.L1Event;

public class EventController extends Thread {

	private static Logger _log = Logger.getLogger(EventController.class.getName());

	private static Map<Integer, L1Event> _allEvent = new HashMap<Integer, L1Event>();

	private static Map<Integer, Boolean> _isEvent = new HashMap<Integer, Boolean>();

	private static Map<Integer, ArrayList<L1NpcInstance>> _NpcListByEventId = new HashMap<Integer, ArrayList<L1NpcInstance>>();

	private final int CHECK_TIME = 60000; // 1分単位でチェック。変更するとうまく動作しない

	private static EventController _instance = null;

	public static synchronized EventController getInstance() {
		if (_instance == null) {
			_instance = new EventController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			init();
			while (true) {
				Thread.sleep(CHECK_TIME);
				judgmentEvent();
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	// 初回(鯖起動時)のみ呼び出し
	private void init() {
		EventTable.getInstance(); // テーブル読み込み
		for (L1Event event : _allEvent.values()) {
			// 1,このイベントの開催期間内に鯖が起動した場合
			if (calcTimeToLong(event.getStartTime()) <= getNowTimeLong() &&
					getNowTimeLong() <= calcTimeToLong(event.getEndTime())) {
				_isEvent.put(event.getEventId(), true);
				startEvent(event.getEventId());
			} else {
				_isEvent.put(event.getEventId(), false);
			}
		}
	}

	/**
	 * イベントの開始・終了を判定し、開催処理、終了処理を実行する。
	 */
	private void judgmentEvent() {
		for (L1Event event : _allEvent.values()) {
			if (!_isEvent.get(event.getEventId())) { // イベントがまだ未開催
				if (calcTimeToLong(event.getStartTime()) == getNowTimeLong()) {
					_isEvent.put(event.getEventId(), true);
					startEvent(event.getEventId());
				}
			} else { // イベント開催中
				if (calcTimeToLong(event.getEndTime()) == getNowTimeLong()) {
					_isEvent.put(event.getEventId(), false);
					endEvent(event.getEventId());
				}
			}
		}
	}

	public void setEventNpcList(int eventId, L1NpcInstance npc) {
		if (!_NpcListByEventId.get(eventId).contains(npc)) {
			_NpcListByEventId.get(eventId).add(npc);
		}
	}

	public ArrayList<L1NpcInstance> getEventNpcList(int eventId) {
		return _NpcListByEventId.get(eventId);
	}

	public boolean isEvent(int eventId) {
		return _isEvent.get(eventId);
	}

	public Map<Integer, L1Event> getAllEvent() {
		return _allEvent;
	}

	public void addEvent(L1Event event) {
		_allEvent.put(event.getEventId(), event);
	}

	private void startEvent(int eventId) {
		_NpcListByEventId.put(eventId, new ArrayList<L1NpcInstance>());
		SpawnTable.getInstance().fillSpawnTableByEvent(eventId);
		SpawnNpcTable.getInstance().fillNpcSpawnTableByEvent(eventId);
		BossSpawnTable.fillSpawnTableByEvent(eventId);
		String eventName = _allEvent.get(eventId).getName();
		if (eventName != null) {
			String announce = "只今よりイベント《" + eventName + "》を開催いたします。";
			L1World.getInstance().broadcastServerMessage(announce);
		}
	}

	private void endEvent(int eventId) {
		for (L1NpcInstance eventNpc : _NpcListByEventId.get(eventId)) {
			eventNpc.setreSpawn(false);
			eventNpc.deleteMe();
		}
		setNextTime(eventId); // 次回開催日時設定
		String eventName = _allEvent.get(eventId).getName();
		if (eventName != null) {
			String announce = "イベント《" + eventName + "》を終了いたします。";
			L1World.getInstance().broadcastServerMessage(announce);
		}
	}

	private void setNextTime(int eventId) {
		L1Event event = _allEvent.get(eventId);
		if (event.getNextStart() != null) { // 次回開催までの時間が設定されている
			int year = event.getEndTime().get(Calendar.YEAR) - event.getStartTime().get(Calendar.YEAR);
			int month = event.getEndTime().get(Calendar.MONTH) - event.getStartTime().get(Calendar.MONTH);
			int day = event.getEndTime().get(Calendar.DAY_OF_MONTH) - event.getStartTime().get(Calendar.DAY_OF_MONTH);
			int hour = event.getEndTime().get(Calendar.HOUR_OF_DAY) - event.getStartTime().get(Calendar.HOUR_OF_DAY);
			int minute = event.getEndTime().get(Calendar.MINUTE) - event.getStartTime().get(Calendar.MINUTE);
			int second = event.getEndTime().get(Calendar.SECOND) - event.getStartTime().get(Calendar.SECOND);
			event.setStartTime(calcNextTime(event.getEndTime(), event.getNextStart())); // END時刻から何分後か。
			event.getEndTime().setTime(event.getStartTime().getTime()); // 次回のスタート時間に一度設定。
			event.getEndTime().add(Calendar.YEAR, year);
			event.getEndTime().add(Calendar.MONTH, month);
			event.getEndTime().add(Calendar.DAY_OF_MONTH, day);
			event.getEndTime().add(Calendar.HOUR_OF_DAY, hour);
			event.getEndTime().add(Calendar.MINUTE, minute);
			event.getEndTime().add(Calendar.SECOND, second);
			EventTable.getInstance().updateEvent(event); // データベースに保存
			_allEvent.put(eventId, event);
		}
	}

	private Calendar calcNextTime(Calendar nextTime, String s) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(nextTime.getTime());
		int _month = getTimeParse(s, "M");
		int _day = getTimeParse(s, "d");
		int _hour = getTimeParse(s, "h");
		int _minute = getTimeParse(s, "m");
		cal.add(Calendar.MONTH, _month);
		cal.add(Calendar.DAY_OF_MONTH, _day);
		cal.add(Calendar.HOUR_OF_DAY, _hour);
		cal.add(Calendar.MINUTE, _minute);
		return cal;
	}

	private int getTimeParse(String target, String search) {
		if (target == null) {
			return 0;
		}
		int n = 0;
		Matcher matcher = Pattern.compile("\\d+" + search).matcher(target);
		if (matcher.find()) {
			String match = matcher.group();
			n = Integer.parseInt(match.replace(search, ""));
		}
		return n;
	}

	private long calcTimeToLong(Calendar cal) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		cal.set(Calendar.SECOND, 0);
		return Long.parseLong(sdf.format(cal.getTime()));
	}

	private long getNowTimeLong() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.SECOND, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return Long.parseLong(sdf.format(now.getTime()));
	}
}
