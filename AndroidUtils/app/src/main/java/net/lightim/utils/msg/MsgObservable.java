package net.lightim.utils.msg;

import net.lightim.utils.MultiHashMap;

import java.util.ArrayList;
import java.util.List;

// 从java.util.Observable拷贝过来的, 修改了一下

/**
 * <p>
 * 观察者模式, 对属性的观察
 * </p>
 *
 * @author entaoyang@163.com
 */
public class MsgObservable {
	private MultiHashMap<String, MsgListener> listeners = new MultiHashMap<String, MsgListener>(12);

	/**
	 * 如果没有指定消息ID, 则会添加一个全局的监听器, 能监听所有消息.
	 *
	 * @param listener 非空
	 * @param msgs     要监听的消息ID列表
	 */
	synchronized public void addListener(MsgListener listener, String... msgs) {
		if (listener == null) {
			throw new IllegalArgumentException("listener can not be null!");
		}

		for (String msg : msgs) {
			listeners.put(msg, listener);
		}
	}

	public synchronized void remove(String msg, MsgListener listener) {
		listeners.remove(msg, listener);
	}

	public synchronized void remove(MsgListener listener) {
		listeners.removeValue(listener);
	}

	public synchronized void clear() {
		listeners.clear();
	}

	/**
	 * 激发一个广播,通知所有监听者,
	 */
	public void fire(Msg msg) {
		ArrayList<MsgListener> tofire = new ArrayList<MsgListener>(8);
		synchronized (this) {
			List<MsgListener> ls = listeners.get(msg.msg);
			if (ls != null && !ls.isEmpty()) {
				tofire.addAll(ls);
			}
		}
		for (MsgListener observer : tofire) {
			try {
				observer.onMsg(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
