/**
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

package jp.l1j.server.model.gametime;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import jp.l1j.server.utils.IntRange;

public class L1GameTime {
	// 2003年7月3日 12:00(UTC)が1月1日00:00
	private static final long BASE_TIME_IN_MILLIS_REAL = 1057233600000L;

	private final int _time;

	private final Calendar _calendar;

	private Calendar makeCalendar(int time) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(0);
		cal.add(Calendar.SECOND, time);
		return cal;
	}

	private L1GameTime(int time) {
		_time = time;
		_calendar = makeCalendar(time);
	}

	public static L1GameTime valueOf(long timeMillis) {
		long t1 = timeMillis - BASE_TIME_IN_MILLIS_REAL;
		if (t1 < 0) {
			throw new IllegalArgumentException();
		}
		int t2 = (int) ((t1 * 6) / 1000L);
		int t3 = t2 % 3; // 時間が3の倍数になるように調整
		return new L1GameTime(t2 - t3);
	}

	public static L1GameTime fromSystemCurrentTime() {
		return L1GameTime.valueOf(System.currentTimeMillis());
	}

	public static L1GameTime valueOfGameTime(Time time) {
		long t = time.getTime() + TimeZone.getDefault().getRawOffset();
		return new L1GameTime((int) (t / 1000L));
	}

	public Time toTime() {
		int t = _time % (24 * 3600); // 日付情報分を切り捨て
		return new Time(t * 1000L - TimeZone.getDefault().getRawOffset());
	}

	public int get(int field) {
		return _calendar.get(field);
	}

	public int getSeconds() {
		return _time;
	}

	public Calendar getCalendar() {
		return (Calendar) _calendar.clone();
	}

	public boolean isNight() {
		int hour = _calendar.get(Calendar.HOUR_OF_DAY);
		return !IntRange.includes(hour, 6, 17); // 6:00-17:59(昼)で無ければtrue
	}

	@Override
	public String toString() {
		SimpleDateFormat f = new SimpleDateFormat(
				"yyyy.MM.dd G 'at' HH:mm:ss z");
		f.setTimeZone(_calendar.getTimeZone());
		return f.format(_calendar.getTime()) + "(" + getSeconds() + ")";
	}
}
