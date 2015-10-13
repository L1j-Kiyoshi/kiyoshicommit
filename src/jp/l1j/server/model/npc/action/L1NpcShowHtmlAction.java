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
package jp.l1j.server.model.npc.action;

import java.util.ArrayList;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.npc.L1NpcHtml;
import jp.l1j.server.utils.IterableElementList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class L1NpcShowHtmlAction extends L1NpcXmlAction {
	private final String _htmlId;
	private final String[] _args;

	public L1NpcShowHtmlAction(Element element) {
		super(element);

		_htmlId = element.getAttribute("HtmlId");
		NodeList list = element.getChildNodes();
		ArrayList<String> dataList = new ArrayList<String>();
		for (Element elem : new IterableElementList(list)) {
			if (elem.getNodeName().equalsIgnoreCase("Data")) {
				dataList.add(elem.getAttribute("Value"));
			}
		}
		_args = dataList.toArray(new String[dataList.size()]);
	}

	@Override
	public L1NpcHtml execute(String actionName, L1PcInstance pc, L1Object obj,
			byte[] args) {
		return new L1NpcHtml(_htmlId, _args);
	}

}
