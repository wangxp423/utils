package net.lightim.utils;
/**
 * entaoyang@163.com
 */
public class IdGen {
	private static int id = 0;

	public synchronized static int gen() {
		return ++id;
	}
}
