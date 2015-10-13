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
package jp.l1j.server.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * org.w3c.dom.NodeListにIterableを付加するためのアダプタ。
 */
// 標準ライブラリに同じものが用意されているようなら置換してください。
public class IterableNodeList implements Iterable<Node> {
	private final NodeList _list;

	private class MyIterator implements Iterator<Node> {
		private int _idx = 0;

		@Override
		public boolean hasNext() {
			return _idx < _list.getLength();
		}

		@Override
		public Node next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return _list.item(_idx++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public IterableNodeList(NodeList list) {
		_list = list;
	}

	@Override
	public Iterator<Node> iterator() {
		return new MyIterator();
	}

}
