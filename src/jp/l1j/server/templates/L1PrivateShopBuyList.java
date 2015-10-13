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
package jp.l1j.server.templates;

// Referenced classes of package jp.l1j.server.templates:
// L1PrivateShopBuyList

public class L1PrivateShopBuyList {
	public L1PrivateShopBuyList() {
	}

	private int _itemObjectId;

	public void setItemObjectId(int i) {
		_itemObjectId = i;
	}

	public int getItemObjectId() {
		return _itemObjectId;
	}

	private int _buyTotalCount; // 買う予定の個数

	public void setBuyTotalCount(int i) {
		_buyTotalCount = i;
	}

	public int getBuyTotalCount() {
		return _buyTotalCount;
	}

	private int _buyPrice;

	public void setBuyPrice(int i) {
		_buyPrice = i;
	}

	public int getBuyPrice() {
		return _buyPrice;
	}

	private int _buyCount; // 買った累計

	public void setBuyCount(int i) {
		_buyCount = i;
	}

	public int getBuyCount() {
		return _buyCount;
	}
}
