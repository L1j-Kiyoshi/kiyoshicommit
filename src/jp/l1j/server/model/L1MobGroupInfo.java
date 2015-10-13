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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jp.l1j.server.model.L1Spawn;
import jp.l1j.server.model.instance.L1NpcInstance;

// Referenced classes of package jp.l1j.server.model:
// L1MobGroupInfo

public class L1MobGroupInfo {
	private static Logger _log = Logger.getLogger(L1MobGroupInfo.class
			.getName());

	private final List<L1NpcInstance> _membersList =
			new ArrayList<L1NpcInstance>();

	private L1NpcInstance _leader;

	public L1MobGroupInfo() {
	}

	public void setLeader(L1NpcInstance npc) {
		_leader = npc;
	}

	public L1NpcInstance getLeader() {
		return _leader;
	}

	public boolean isLeader(L1NpcInstance npc) {
		return npc.getId() == _leader.getId();
	}

	private L1Spawn _spawn;

	public void setSpawn(L1Spawn spawn) {
		_spawn = spawn;
	}

	public L1Spawn getSpawn() {
		return _spawn;
	}

	public void addMember(L1NpcInstance npc) {
		if (npc == null) {
			throw new NullPointerException();
		}

		// 最初のメンバーであればリーダーにする
		if (_membersList.isEmpty()) {
			setLeader(npc);
			// リーダーの再ポップ情報を保存する
			if (npc.isReSpawn()) {
				setSpawn(npc.getSpawn());
			}
		}

		if (!_membersList.contains(npc)) {
			_membersList.add(npc);
		}
		npc.setMobGroupInfo(this);
		npc.setMobGroupId(_leader.getId());
	}

	public synchronized int removeMember(L1NpcInstance npc) {
		if (npc == null) {
			throw new NullPointerException();
		}

		if (_membersList.contains(npc)) {
			_membersList.remove(npc);
		}
		npc.setMobGroupInfo(null);

		// リーダーで他のメンバーがいる場合は、新リーダーにする
		if (isLeader(npc)) {
			if (isRemoveGroup() && _membersList.size() != 0) { // リーダーが死亡したらグループ解除する場合
				for (L1NpcInstance minion : _membersList) {
					minion.setMobGroupInfo(null);
					minion.setSpawn(null);
					minion.setreSpawn(false);
				}
				return 0;
			}
			if (_membersList.size() != 0) {
				setLeader(_membersList.get(0));
			}
		}

		// 残りのメンバー数を返す
		return _membersList.size();
	}
	
	public List<L1NpcInstance> getMembers() {
		return _membersList;
	}

	public int getNumOfMembers() {
		return _membersList.size();
	}

	private boolean _isRemoveGroup;

	public boolean isRemoveGroup() {
		return _isRemoveGroup;
	}

	public void setRemoveGroup(boolean flag) {
		_isRemoveGroup = flag;
	}

}
