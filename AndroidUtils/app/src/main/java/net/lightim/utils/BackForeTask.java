package net.lightim.utils;
/**
 * entaoyang@163.com
 */
public abstract class BackForeTask extends RunTask {
	private boolean first = true;

	@Override
	final public void run() {
		if (isFinished() || isCanceled()) {
			return;
		}
		try {
			if (first) {
				onBack();
			} else {
				onFore();
			}
		} catch (Throwable t) {
			ex = t;
		}
		if (first) {
			first = false;
			TaskUtil.getMainHandler().post(this);
		} else {
			finish();
		}
	}

	@Override
	final protected void onRun() throws Exception {
	}

	public abstract void onBack() throws Exception;

	public abstract void onFore() throws Exception;

}
