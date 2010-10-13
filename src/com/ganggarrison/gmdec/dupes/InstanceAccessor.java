package com.ganggarrison.gmdec.dupes;

import java.util.ArrayList;
import java.util.List;

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Instance.PInstance;

public class InstanceAccessor implements ItemAccessor<Instance> {
	private final GmFile gmFile;

	public InstanceAccessor(GmFile gmFile) {
		this.gmFile = gmFile;
	}

	@Override
	public List<Instance> getItems() {
		ArrayList<Instance> items = new ArrayList<Instance>();
		for (Room room : gmFile.rooms) {
			for (Instance instance : room.instances) {
				items.add(instance);
			}
		}
		return items;
	}

	@Override
	public Integer getId(Instance item) {
		Integer id = item.properties.get(PInstance.ID);
		if (id == null || id < getFirstValidId()) {
			return null;
		} else {
			return id;
		}
	}

	@Override
	public void setId(Instance item, int id) {
		item.properties.put(PInstance.ID, id);
	}

	@Override
	public int getFirstValidId() {
		return 100001;
	}

	@Override
	public void setMaxId(int id) {
		gmFile.lastInstanceId = id;
	}

	@Override
	public String getItemName() {
		return "Instance";
	}
}