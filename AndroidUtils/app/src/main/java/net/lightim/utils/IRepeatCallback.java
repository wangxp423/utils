package net.lightim.utils;

public interface IRepeatCallback {
	/**
	 * @param index 第几次被调用
	 * @param value
	 * @return true:继续回调; false:终止后面的回调
	 */
	boolean onRepeat(int index, long value);

	void onRepeatEnd();
}
