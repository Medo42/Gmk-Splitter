package com.ganggarrison.gmdec.dupes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TreeMultiMap<K extends Comparable<K>, V> implements Iterable<Map.Entry<K, List<V>>> {
	private Map<K, List<V>> bag = new TreeMap<K, List<V>>();

	public void add(K key, V value) {
		if (!bag.containsKey(key)) {
			bag.put(key, new ArrayList<V>());
		}
		bag.get(key).add(value);
	}

	public List<V> get(K key) {
		if (!bag.containsKey(key)) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(bag.get(key));
		}
	}

	@Override
	public Iterator<Entry<K, List<V>>> iterator() {
		return Collections.unmodifiableMap(bag).entrySet().iterator();
	}
}
