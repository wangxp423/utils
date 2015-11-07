package net.lightim.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author entaoyang@163.com
 */
public class MultiHashMap<K, V> {
	private HashMap<K, LinkedList<V>> model;

	public MultiHashMap() {
		this(8);
	}

	public MultiHashMap(int capacity) {
		model = new HashMap<K, LinkedList<V>>(capacity < 8 ? 8 : capacity);
	}

	public void clear() {
		model.clear();
	}

	public boolean containsKey(K key) {
		return model.containsKey(key);
	}

	/**
	 * 需要遍历所有的value, 性能较差
	 *
	 * @param value
	 * @return
	 */
	public boolean containsValue(V value) {
		for (LinkedList<V> ls : model.values()) {
			if (ls != null && ls.contains(value)) {
				return true;
			}
		}
		return false;
	}

	public Set<Entry<K, LinkedList<V>>> entrySet() {
		return model.entrySet();
	}

	/**
	 * 根据Key查找对应的Value的集合, 应该对返回的结果集只进行读操作
	 *
	 * @param key
	 * @return
	 */
	public List<V> get(K key) {
		return model.get(key);
	}

	public boolean isEmpty() {
		return model.isEmpty();
	}

	public Set<K> keySet() {
		return model.keySet();
	}

	public void put(K key, V value) {
		LinkedList<V> ls = model.get(key);
		if (null == ls) {
			ls = new LinkedList<V>();
			model.put(key, ls);
		}
		if (!ls.contains(value)) {
			ls.add(value);
		}
	}

	public LinkedList<V> remove(K key) {
		return model.remove(key);
	}

	public void remove(K key, V val) {
		LinkedList<V> ls = model.get(key);
		if (null != ls) {
			ls.remove(val);
		}
	}

	/**
	 * 低效率, 需要遍历
	 *
	 * @param val
	 */
	public void removeValue(V val) {
		for (LinkedList<V> ls : model.values()) {
			if (ls != null) {
				ls.remove(val);
			}
		}
	}

	public int size() {
		return model.size();
	}

	public Collection<LinkedList<V>> values() {
		return model.values();
	}

}
