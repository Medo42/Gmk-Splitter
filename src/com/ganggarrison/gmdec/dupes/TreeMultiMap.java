/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.dupes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TreeMultiMap<K extends Comparable<K>, V> implements Iterable<Map.Entry<K, List<V>>> {
	private Map<K, List<V>> multiMap = new TreeMap<K, List<V>>();

	public void add(K key, V value) {
		if (!multiMap.containsKey(key)) {
			multiMap.put(key, new ArrayList<V>());
		}
		multiMap.get(key).add(value);
	}

	public List<V> get(K key) {
		if (!multiMap.containsKey(key)) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(multiMap.get(key));
		}
	}

	@Override
	public Iterator<Entry<K, List<V>>> iterator() {
		return Collections.unmodifiableMap(multiMap).entrySet().iterator();
	}
}
