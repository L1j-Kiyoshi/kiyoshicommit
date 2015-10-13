package jp.l1j.server.templates;

import java.util.Calendar;

public class L1Event {

	private int _eventId;

	public void setEventId(int i) {
		_eventId = i;
	}

	public int getEventId() {
		return _eventId;
	}

	private String _eventName;

	public void setName(String s) {
		_eventName = s;
	}

	public String getName() {
		return _eventName;
	}

	private Calendar _startTime;

	public Calendar getStartTime() {
		return _startTime;
	}

	public void setStartTime(Calendar i) {
		_startTime = i;
	}

	private Calendar _endTime;

	public Calendar getEndTime() {
		return _endTime;
	}

	public void setEndTime(Calendar i) {
		_endTime = i;
	}

	private String _nextStart;

	public String getNextStart() {
		return _nextStart;
	}

	public void setNextStart(String s) {
		_nextStart = s;
	}
}
