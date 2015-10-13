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
package jp.l1j.server.model;

public class L1ObjectAmount<T> {
	private final T _obj;
	private final int _amount;

	public L1ObjectAmount(T obj, int amount) {
		_obj = obj;
		_amount = amount;
	}

	public T getObject() {
		return _obj;
	}

	public int getAmount() {
		return _amount;
	}
}
