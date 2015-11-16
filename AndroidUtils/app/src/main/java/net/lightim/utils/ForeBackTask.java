package net.lightim.utils;

import android.os.Handler;

import java.util.concurrent.ExecutorService;
/**
 * entaoyang@163.com
 */
public abstract class ForeBackTask extends RunTask {
	private boolean first = true;
	private Handler backHandler = null;
	private ExecutorService es = null;

	@Override
	final public void run() {
		if (isFinished() || isCanceled()) {
			return;
		}
		try {
			if (first) {
				onFore();
			} else {
				onBack();
			}
		} catch (Throwable t) {
			ex = t;
		}
		if (first) {
			first = false;
			if (es != null) {
				es.submit(this);
			} else if (backHandler != null) {
				backHandler.post(this);
			} else {
				xlog.e("No back handler given");
				finish();
			}
		} else {
			finish();
		}
	}

	@Override
	public void finish() {
		super.finish();
		backHandler = null;
		es = null;
	}

	public void setBackHandler(Handler h) {
		backHandler = h;
	}

	public void setExecutorService(ExecutorService es) {
		this.es = es;
	}

	@Override
	final protected void onRun() throws Exception {
	}

	public abstract void onBack() throws Exception;

	public abstract void onFore() throws Exception;

}
