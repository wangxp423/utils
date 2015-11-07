package net.lightim.utils.msg;

import java.util.List;

/**
 * 进程内有效
 *
 * @author entaoyang@163.com
 */
public class MsgCenter {
	private static MsgObservable stub = new MsgObservable();

	/**
	 * 如果没有指定消息ID, 则会添加一个全局的监听器, 能监听所有消息.
	 *
	 * @param listener 非空
	 * @param msgs     要监听的消息ID列表, 如果没有提供msg列表,则所有的消息都会被回调-----比如用于监控所有消息
	 */
	public static void addListener(MsgListener listener, String... msgs) {
		stub.addListener(listener, msgs);
	}

	public static void addListener(MsgListener listener, Class<?>... clses) {
		for (Class<?> cls : clses) {
			stub.addListener(listener, cls.getName());
		}
	}

	public static void remove(String msg, MsgListener listener) {
		stub.remove(msg, listener);
	}

	public static void remove(Class<?> cls, MsgListener listener) {
		stub.remove(cls.getName(), listener);
	}

	public static void remove(MsgListener listener) {
		stub.remove(listener);
	}

	public static void clear() {
		stub.clear();
	}

	public static List<Object> fire(Msg msg) {
		if (msg != null && msg.msg != null && msg.msg.length() > 0) {
			stub.fire(msg);
			return msg.returnValues;
		}
		return null;
	}

	public static List<Object> fire(String msg) {
		return fire(new Msg(msg));
	}

	public static List<Object> fire(Class<?> cls) {
		return fire(cls.getName());
	}

	public static void fireFore(final Msg msg) {
		msg.fireFore();
	}

	public static void fireBack(final Msg msg) {
		msg.fireBack();
	}
}
