/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.dupes;

import java.util.List;

public interface ItemAccessor<Item> {
	List<Item> getItems();

	Integer getId(Item item);

	void setId(Item item, int id);

	int getFirstValidId();

	void setMaxId(int id);

	String getItemName();

	boolean informAboutNewIds();
}