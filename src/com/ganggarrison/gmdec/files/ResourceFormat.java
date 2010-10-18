package com.ganggarrison.gmdec.files;

import java.util.HashSet;
import java.util.List;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.file.ResourceList;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Resource.Kind;

import com.ganggarrison.gmdec.dupes.OrderPreservingDupeRemoval;
import com.ganggarrison.gmdec.dupes.ResourceAccessor;

public abstract class ResourceFormat<T extends Resource<T, ?>> extends FileTreeFormat<T> {
	@Override
	public void addResToTree(T resource, ResNode parent) {
		ResNode child = new ResNode(resource.getName(),
				ResNode.STATUS_SECONDARY, resource.getKind(), resource.reference);
		parent.add(child);
	}

	@Override
	public void addAllResourcesToGmFile(List<T> resources, GmFile gmf) {
		if (resources.isEmpty()) {
			return;
		}
		Kind kind = resources.get(0).getKind();

		checkDuplicateNames(resources, kind);

		// Take care of dupes and unassigned IDs
		ResourceAccessor<T> accessor = new ResourceAccessor<T>(resources);
		OrderPreservingDupeRemoval.perform(accessor);

		ResourceList<T> list = (ResourceList<T>) gmf.getList(kind);
		for (T resource : resources) {
			list.add(resource);
			if (resource.getId() >= list.lastId) {
				list.lastId = resource.getId();
			}
		}
	}

	private void checkDuplicateNames(List<T> resources, Kind kind) {
		HashSet<String> names = new HashSet<String>();
		HashSet<String> lcNames = new HashSet<String>();
		for(T resource : resources) {
			String name = resource.getName();
			String lcName = name.toLowerCase();
			if (names.contains(name)) {
				throw new IllegalArgumentException(kind + " " + name
						+ " has duplicate name!");
			}
			if (lcNames.contains(lcName)) {
				System.err.println("Warning: The name of " + kind + " " + name
						+ " only differs in case from a different " + kind + ".");
			}
			names.add(name);
			lcNames.add(lcName);
		}
	}
}
