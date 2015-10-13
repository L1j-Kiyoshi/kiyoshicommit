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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeakCheckedConnection {
	private static Logger _log = Logger
			.getLogger(LeakCheckedConnection.class.getName());
	private Connection _con;
	private Throwable _stackTrace;
	private Map<Statement, Throwable> _openedStatements = new HashMap<Statement, Throwable>();
	private Map<ResultSet, Throwable> _openedResultSets = new HashMap<ResultSet, Throwable>();
	private Object _proxy;

	private LeakCheckedConnection(Connection con) {
		_con = con;
		_proxy = Proxy.newProxyInstance(Connection.class.getClassLoader(),
				new Class[] { Connection.class }, new ConnectionHandler());
		_stackTrace = new Throwable();
	}

	public static Connection create(Connection con) {
		return (Connection) new LeakCheckedConnection(con)._proxy;
	}

	private Object send(Object o, Method m, Object[] args) throws Throwable {
		try {
			return m.invoke(o, args);
		} catch (InvocationTargetException e) {
			if (e.getCause() != null) {
				throw e.getCause();
			}
			throw e;
		}
	}

	private void remove(Object o) {
		if (o instanceof ResultSet) {
			_openedResultSets.remove(o);
		} else if (o instanceof Statement) {
			_openedStatements.remove(o);
		} else {
			throw new IllegalArgumentException("bad class:" + o);
		}
	}

	void closeAll() {
		if (!_openedResultSets.isEmpty()) {
			for (Throwable t : _openedResultSets.values()) {
				_log.log(Level.WARNING, "Leaked ResultSets detected.", t);
			}
		}
		if (!_openedStatements.isEmpty()) {
			for (Throwable t : _openedStatements.values()) {
				_log.log(Level.WARNING, "Leaked Statement detected.", t);
			}
		}
		for (ResultSet rs : _openedResultSets.keySet()) {
			SqlUtil.close(rs);
		}
		for (Statement ps : _openedStatements.keySet()) {
			SqlUtil.close(ps);
		}
	}

	/**
	 * ファイナライザ用無名クラス _guardianに未使用変数の警告が出ますが、削除しないでください。
	 */
	private final Object _guardian = new Object() {
		@Override
		protected void finalize() throws Throwable {
			if (!_con.isClosed()) {
				_log.log(Level.WARNING, "Leaked Connection detected.",
						_stackTrace);
				_con.close();
			}
		}
	};

	private class ConnectionHandler implements
			java.lang.reflect.InvocationHandler {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (method.getName().equals("close")) {
				closeAll();
			}
			Object o = send(_con, method, args);
			if (o instanceof Statement) {
				_openedStatements.put((Statement) o, new Throwable());
				o = new Delegate(o, PreparedStatement.class)._delegateProxy;
			}
			return o;
		}
	}

	private class Delegate implements InvocationHandler {
		private Object _delegateProxy;
		private Object _original;

		Delegate(Object o, Class c) {
			_original = o;
			_delegateProxy = Proxy.newProxyInstance(c.getClassLoader(),
					new Class[] { c }, this);
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (method.getName().equals("close")) {
				remove(_original);
			}
			Object o = send(_original, method, args);
			if (o instanceof ResultSet) {
				_openedResultSets.put((ResultSet) o, new Throwable());
				o = new Delegate(o, ResultSet.class)._delegateProxy;
			}
			return o;
		}
	}
}
