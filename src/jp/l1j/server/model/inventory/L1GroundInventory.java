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
package jp.l1j.server.model.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.l1j.configure.Config;
import jp.l1j.server.model.instance.L1ItemInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.L1World;
import jp.l1j.server.packets.server.S_DropItem;
import jp.l1j.server.packets.server.S_RemoveObject;
import jp.l1j.server.templates.L1InventoryItem;

public class L1GroundInventory extends L1Inventory {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Timer _timer = new Timer();

	private Map<Integer, DeletionTimer> _reservedTimers = new HashMap<Integer, DeletionTimer>();

	private class DeletionTimer extends TimerTask {
		private final L1ItemInstance _item;

		public DeletionTimer(L1ItemInstance item) {
			_item = item;
		}

		@Override
		public void run() {
			try {
				synchronized (L1GroundInventory.this) {
					if (!_items.contains(_item)) {// 拾われたタイミングによってはこの条件を満たし得る
						return; // 既に拾われている
					}
					removeItem(_item);
				}
			} catch (Throwable t) {
				_log.log(Level.SEVERE, t.getLocalizedMessage(), t);
			}
		}
	}

	private void setTimer(L1ItemInstance item) {
		if (!Config.ALT_ITEM_DELETION_TYPE.equalsIgnoreCase("std")) {
			return;
		}
		if (item.getItemId() == 40515) { // 精霊の石
			return;
		}

		_timer.schedule(new DeletionTimer(item),
				Config.ALT_ITEM_DELETION_TIME * 60 * 1000);
	}

	private void cancelTimer(L1ItemInstance item) {
		DeletionTimer timer = _reservedTimers.get(item.getId());
		if (timer == null) {
			return;
		}
		timer.cancel();
	}

	public L1GroundInventory(int objectId, int x, int y, short map) {
		setId(objectId);
		setX(x);
		setY(y);
		setMap(map);
		L1World.getInstance().addVisibleObject(this);
	}

	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		for (L1ItemInstance item : getItems()) {
			if (!perceivedFrom.knownsObject(item)) {
				perceivedFrom.addKnownObject(item);
				perceivedFrom.sendPackets(new S_DropItem(item)); // プレイヤーへDROPITEM情報を通知
			}
		}
	}

	// 認識範囲内にいるプレイヤーへオブジェクト送信
	@Override
	public void insertItem(L1ItemInstance item) {
		super.insertItem(item);
		setTimer(item);

		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(item)) {
			pc.sendPackets(new S_DropItem(item));
			pc.addKnownObject(item);
		}
	}

	// 見える範囲内にいるプレイヤーのオブジェクト更新
	@Override
	public void updateItem(L1ItemInstance item) {
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(item)) {
			pc.sendPackets(new S_DropItem(item));
		}
	}

	// 空インベントリ破棄及び見える範囲内にいるプレイヤーのオブジェクト削除
	@Override
	public void deleteItem(L1ItemInstance item) {
		super.deleteItem(item);
		if (_items.size() == 0) {
			L1World.getInstance().removeVisibleObject(this);
		}
	}

	@Override
	public void onRemoveItem(L1ItemInstance item) {
		cancelTimer(item);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(item)) {
			pc.sendPackets(new S_RemoveObject(item));
			pc.removeKnownObject(item);
		}
	}

	@Override
	public int getOwnerLocation() {
		return L1InventoryItem.LOC_NONE;
	}

	private static Logger _log = Logger
			.getLogger(L1PcInventory.class.getName());
}
