/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.dupes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This dupe removal strategy will preserve the order of all items with valid
 * IDs. It tries to prevent changing IDs unless neccessary. Items without valid
 * IDs will be inserted at the upper end of the range.
 */
public class OrderPreservingDupeRemoval {
	public static <Item> void perform(ItemAccessor<Item> accessor) {
		int changed = 0;

		List<Item> items = accessor.getItems();
		List<Item> invalidIdItems = new ArrayList<Item>();
		TreeMultiMap<Integer, Item> itemsById = new TreeMultiMap<Integer, Item>();

		for (Item item : items) {
			Integer id = accessor.getId(item);
			if (id == null) {
				invalidIdItems.add(item);
			} else {
				itemsById.add(id, item);
			}
		}

		int nextFreeItemId = accessor.getFirstValidId();
		for (Map.Entry<Integer, List<Item>> entry : itemsById) {
			for (Item item : entry.getValue()) {
				int id = entry.getKey();
				if (id >= nextFreeItemId) {
					nextFreeItemId = id + 1;
				} else {
					accessor.setId(item, nextFreeItemId++);
					changed++;
				}
			}
		}

		for (Item item : invalidIdItems) {
			accessor.setId(item, nextFreeItemId++);
		}

		accessor.setMaxId(nextFreeItemId - 1);

		if (changed > 0) {
			System.err.println("INFO: " + changed + " duplicate " + accessor.getItemName() + " IDs have been changed.");
		}
		if (invalidIdItems.size() > 0 && accessor.informAboutNewIds()) {
			System.err.println("INFO: " + invalidIdItems.size() + " new " + accessor.getItemName()
					+ " IDs have been assigned.");
		}
	}
}
