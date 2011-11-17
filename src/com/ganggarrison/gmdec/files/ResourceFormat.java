package com.ganggarrison.gmdec.files;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.file.ResourceList;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Resource;

import com.ganggarrison.gmdec.ResourceTreeEntry;
import com.ganggarrison.gmdec.ResourceTreeEntry.Type;
import com.ganggarrison.gmdec.dupes.OrderPreservingDupeRemoval;
import com.ganggarrison.gmdec.dupes.ResourceAccessor;

public abstract class ResourceFormat<T extends InstantiableResource<T, ?>> extends FileTreeFormat<T> {
	@Override
	public void addResToTree(T resource, ResNode parent) {
		ResNode child = new ResNode(resource.getName(),
				ResNode.STATUS_SECONDARY, resource.getClass(), resource.reference);
		parent.add(child);
	}

	@Override
	public void addAllResourcesToGmFile(List<T> resources, GmFile gmf) {
		if (resources.isEmpty()) {
			return;
		}
		@SuppressWarnings("unchecked")
		Class<T> kind = (Class<T>) resources.get(0).getClass();

		checkDuplicateNames(resources, kind);

		// Take care of dupes and unassigned IDs
		ResourceAccessor<T> accessor = new ResourceAccessor<T>(resources);
		OrderPreservingDupeRemoval.perform(accessor);

		ResourceList<T> list = (ResourceList<T>) gmf.resMap.getList(kind);
		for (T resource : resources) {
			list.add(resource);
			if (resource.getId() >= list.lastId) {
				list.lastId = resource.getId();
			}
		}
	}

	@Override
	public ResourceTreeEntry createResourceTreeEntry(T resource) {
		return new ResourceTreeEntry(resource.getName(), baseFilename(resource), Type.RESOURCE);
	};

	public static void checkDuplicateNames(Collection<? extends Resource<?, ?>> resources, Class<?> kind) {
		HashSet<String> names = new HashSet<String>();
		HashSet<String> lcNames = new HashSet<String>();
		for (Resource<?, ?> resource : resources) {
			String name = resource.getName();
			String lcName = name.toLowerCase();
			if (names.contains(name)) {
				throw new IllegalArgumentException(kind.getSimpleName() + " " + name
						+ " has duplicate name!");
			}
			if (lcNames.contains(lcName)) {
				System.err.println("Warning: The name of " + kind.getSimpleName() + " " + name
						+ " only differs in case from a different " + kind.getSimpleName() + ".");
			}
			names.add(name);
			lcNames.add(lcName);
		}
	}
}
