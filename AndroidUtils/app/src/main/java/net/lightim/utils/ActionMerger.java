package net.lightim.utils;

import java.util.concurrent.atomic.AtomicBoolean;

// 动作合并, 延时执行

/**
 * entaoyang@163.com
 */
public class ActionMerger {
	private AtomicBoolean hasTimer = new AtomicBoolean(false);
	private int millSedonds = 0;
	private Runnable callback;

	public ActionMerger() {

	}

	public void clear() {
		millSedonds = 0;
		callback = null;
		hasTimer.set(false);
	}

	/**
	 * @param millSec
	 *            >0: 延时广播消息; <=0:立即广播消息
	 * @param callback
	 */
	public ActionMerger(int millSec, Runnable callback) {
		this.callback = callback;
		this.millSedonds = millSec;
	}

	public ActionMerger setDelay(int millSec) {
		this.millSedonds = millSec;
		return this;
	}

	public ActionMerger setCallback(Runnable callback) {
		this.callback = callback;
		return this;
	}

	public void trigger() {
		if (millSedonds > 0) {
			if (!hasTimer.getAndSet(true)) {
				TaskUtil.foreDelay(millSedonds, run);// flush
			}
		} else {
			TaskUtil.fore(run);
		}
	}

	private Runnable run = new Runnable() {

		@Override
		public void run() {
			hasTimer.set(false);
			if (callback != null) {
				callback.run();
			}
		}
	};
}
