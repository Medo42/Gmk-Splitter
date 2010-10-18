package com.ganggarrison.gmdec.dupes;

import java.util.Collections;
import java.util.List;

import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Resource.Kind;

import com.ganggarrison.gmdec.GmkSplitter;
import com.ganggarrison.gmdec.GmkSplitter.IdPreservation;

public class ResourceAccessor<T extends Resource<T, ?>> implements ItemAccessor<T> {
	private List<T> resources;

	public ResourceAccessor(List<T> resources) {
		this.resources = resources;
	}

	@Override
	public List<T> getItems() {
		return Collections.unmodifiableList(resources);
	}

	@Override
	public Integer getId(T item) {
		int id = item.getId();
		return id >= 0 ? id : null;
	}

	@Override
	public void setId(T item, int id) {
		item.setId(id);
	}

	@Override
	public int getFirstValidId() {
		return 0;
	}

	@Override
	public void setMaxId(int id) {
		// The resource lists calculate that automatically.
	}

	@Override
	public String getItemName() {
		if (resources.isEmpty()) {
			return "Resource";
		} else {
			return resources.get(0).getKind().toString();
		}
	}

	@Override
	public boolean informAboutNewIds() {
		if (resources.isEmpty()) {
			return false;
		} else {
			if(resources.get(0).getKind() == Kind.OBJECT) {
				return GmkSplitter.preserveIds == IdPreservation.ALL || GmkSplitter.preserveIds == IdPreservation.OBJECTS;
			} else {
				return GmkSplitter.preserveIds == IdPreservation.ALL;
			}
		}
	}
}
