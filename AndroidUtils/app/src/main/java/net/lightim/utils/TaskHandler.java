package net.lightim.utils;

import android.os.Handler;
import android.os.HandlerThread;
/**
 * entaoyang@163.com
 */
public class TaskHandler {
	private Handler back;
	private HandlerThread thread;

	public TaskHandler() {
		this("");
	}

	public TaskHandler(String name) {
		thread = new HandlerThread("QueueTask:" + name);
		thread.setDaemon(true);
		thread.start();
		back = new Handler(thread.getLooper());
	}

	private Handler mainHandler() {
		return TaskUtil.getMainHandler();
	}

	public void quit() {
		back.getLooper().quit();
	}

	public Handler getHandler() {
		return back;
	}

	public void back(Runnable r) {
		back.post(r);
	}

	/**
	 * thread handler中运行, 排队,保证顺序
	 */
	public RunTask back(RunTask t) {
		back.post(t);
		return t;
	}

	public void backDelay(int millSec, Runnable task) {
		back.postDelayed(task, millSec);
	}

	public RunTask backDelay(int millSec, RunTask task) {
		back.postDelayed(task, millSec);
		return task;
	}

	public void fore(Runnable r) {
		mainHandler().post(r);
	}

	public RunTask fore(RunTask r) {
		mainHandler().post(r);
		return r;
	}

	public void foreDelay(int millSec, Runnable r) {
		mainHandler().postDelayed(r, millSec);
	}

	public RunTask foreDelay(int millSec, RunTask r) {
		mainHandler().postDelayed(r, millSec);
		return r;
	}

	public RunTask backFore(final BackForeTask t) {
		return back(t);
	}

	public RunTask foreBack(final ForeBackTask t) {
		t.setBackHandler(back);
		return fore(t);
	}
}
