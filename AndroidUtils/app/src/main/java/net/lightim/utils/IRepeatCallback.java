package net.lightim.utils;
/**
 * entaoyang@163.com
 */
public interface IRepeatCallback {
	/**
	 * @param index 第几次被调用
	 * @param value
	 * @return true:继续回调; false:终止后面的回调
	 */
	boolean onRepeat(int index, long value);

	void onRepeatEnd();
}
