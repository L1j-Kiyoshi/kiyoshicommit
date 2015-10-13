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

import java.util.List;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.L1Object;
import jp.l1j.server.model.npc.L1NpcHtml;
import org.w3c.dom.Element;

public class L1NpcListedAction extends L1NpcXmlAction {
	private List<L1NpcAction> _actions;

	public L1NpcListedAction(Element element) {
		super(element);
		_actions = L1NpcXmlParser.listActions(element);
	}

	@Override
	public L1NpcHtml execute(String actionName, L1PcInstance pc, L1Object obj,
			byte[] args) {
		L1NpcHtml result = null;
		for (L1NpcAction action : _actions) {
			if (!action.acceptsRequest(actionName, pc, obj)) {
				continue;
			}
			L1NpcHtml r = action.execute(actionName, pc, obj, args);
			if (r != null) {
				result = r;
			}
		}
		return result;
	}

	@Override
	public L1NpcHtml executeWithAmount(String actionName, L1PcInstance pc,
			L1Object obj, int amount) {
		L1NpcHtml result = null;
		for (L1NpcAction action : _actions) {
			if (!action.acceptsRequest(actionName, pc, obj)) {
				continue;
			}
			L1NpcHtml r = action.executeWithAmount(actionName, pc, obj, amount);
			if (r != null) {
				result = r;
			}
		}
		return result;
	}
}
