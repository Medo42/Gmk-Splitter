package com.ganggarrison.gmdec.dupes;

import java.util.List;

public interface ItemAccessor<Item> {
	List<Item> getItems();

	Integer getId(Item item);

	void setId(Item item, int id);

	int getFirstValidId();

	void setMaxId(int id);

	String getItemName();
}