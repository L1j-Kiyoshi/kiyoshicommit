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

package jp.l1j.server.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionUtil {
	private static Logger _log = Logger.getLogger(ReflectionUtil.class
			.getName());

	private static void logException(Exception e) {
		_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
	}

	public static Class<?> classForName(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			logException(e);
		}
		return null;
	}

	public static Constructor<?> getConstructor(Class<?> cls, Class<?>... args) {
		try {
			return cls.getDeclaredConstructor(args);
		} catch (SecurityException e) {
			logException(e);
		} catch (NoSuchMethodException e) {
			logException(e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Constructor<?> con, Object... args) {
		try {
			con.setAccessible(true);
			return (T) con.newInstance(args);
		} catch (IllegalArgumentException e) {
			logException(e);
		} catch (InstantiationException e) {
			logException(e);
		} catch (IllegalAccessException e) {
			logException(e);
		} catch (InvocationTargetException e) {
			logException(e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<?> cls) {
		Constructor<?> con = getConstructor(cls, new Class<?>[0]);
		return (T) newInstance(con, new Object[0]);
	}

	public static <T, T2> T newInstance(String className, Class<T2> arg,
			T2 argValue) {
		Class<?> cls = ReflectionUtil.classForName(className);
		Constructor<?> con = ReflectionUtil.getConstructor(cls, arg);
		return ReflectionUtil.<T> newInstance(con, argValue);
	}

	public static <T> T newInstance(String className) {
		Class<?> cls = ReflectionUtil.classForName(className);
		return ReflectionUtil.<T> newInstance(cls);
	}
}
