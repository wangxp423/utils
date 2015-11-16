package net.lightim.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * 一种结果是执行完成, 另一个是被取消. 这两个都是结束状态
 *
 * @author entaoyang@163.com
 */
public abstract class RunTask implements Runnable {
	protected final static Hashtable<Integer, RunTask> map = new Hashtable<Integer, RunTask>(64);
	protected int id = IdGen.gen();
	protected boolean canceled = false;
	protected boolean finished = false;
	protected Throwable ex = null;
	protected HashSet<String> groups = null;

	public RunTask() {
		map.put(id, this);
	}

	public static RunTask find(int id) {
		return map.get(id);
	}

	public static void cancel(String group) {
		ArrayList<RunTask> ls = new ArrayList<RunTask>();
		synchronized (map) {
			for (RunTask t : map.values()) {
				if (t.groups != null && t.groups.contains(group)) {
					ls.add(t);
				}
			}
		}
		for (RunTask t : ls) {
			t.cancel();
		}
	}

	public static void cancel(int id) {
		RunTask rx = map.remove(id);
		if (rx != null) {
			rx.cancel();
		}
	}

	public void finish() {
		finished = true;
		map.remove(id);
		onFinish();
	}

	public void cancel() {
		canceled = true;
		map.remove(id);
	}

	public RunTask addGroup(String group) {
		if (groups == null) {
			groups = new HashSet<String>();
		}
		groups.add(group);
		return this;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public Throwable getError() {
		return ex;
	}

	public boolean error() {
		return ex != null;
	}

	public boolean isFinished() {
		return finished;
	}

	public int id() {
		return id;
	}

	@Override
	public void run() {
		if (isFinished() || isCanceled()) {
			return;
		}
		try {
			onRun();
		} catch (Throwable t) {
			xlog.e(t);
			ex = t;
		}
		finish();
	}

	protected void onFinish() {

	}

	protected abstract void onRun() throws Exception;

}
