package net.lightim.utils;

public class IdGen {
	private static int id = 0;

	public synchronized static int gen() {
		return ++id;
	}
}
