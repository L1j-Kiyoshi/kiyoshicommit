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
package jp.l1j.server.model;

import jp.l1j.server.utils.IntRange;

public class L1Karma {
	private static final int[] KARMA_POINT = { 10000, 20000, 100000, 500000,
			1500000, 3000000, 5000000, 10000000, 15500000 };

	// 上下限は+-15500000
	private static IntRange KARMA_RANGE = new IntRange(-15500000, 15500000);

	private int _karma = 0;

	public int get() {
		return _karma;
	}

	public void set(int i) {
		_karma = KARMA_RANGE.ensure(i);
	}

	public void add(int i) {
		set(_karma + i);
	}

	public int getLevel() {
		boolean isMinus = false;
		int karmaLevel = 0;

		int karma = get();
		if (karma < 0) {
			isMinus = true;
			karma *= -1;
		}

		for (int point : KARMA_POINT) {
			if (karma >= point) {
				karmaLevel++;
				if (karmaLevel >= 8) {
					break;
				}
			} else {
				break;
			}
		}
		if (isMinus) {
			karmaLevel *= -1;
		}

		return karmaLevel;
	}

	public int getPercent() {
		int karma = get();
		int karmaLevel = getLevel();
		if (karmaLevel == 0) {
			return 0;
		}

		if (karma < 0) {
			karma *= -1;
			karmaLevel *= -1;
		}

		return 100 * (karma - KARMA_POINT[karmaLevel - 1])
				/ (KARMA_POINT[karmaLevel] - KARMA_POINT[karmaLevel - 1]);
	}
}
