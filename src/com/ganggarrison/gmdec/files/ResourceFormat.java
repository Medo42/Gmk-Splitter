package com.ganggarrison.gmdec.files;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.file.ResourceList;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Resource.Kind;

public abstract class ResourceFormat<T extends Resource<T, ?>> extends FileTreeFormat<T> {
	// TODO Improve ID conflict behaviour
	@Override
	public final void addResToGmFile(T resource, GmFile gmf, ResNode parent) {
		Kind kind = resource.getKind();
		ResourceList<T> list = (ResourceList<T>) gmf.getList(kind);
		T oldRes = list.getUnsafe(resource.getId());
		if (oldRes != null) {
			System.err.println("" + kind + " " + resource.getName() + " has the same ID as " + kind + " "
					+ oldRes.getName() + ", changing id.");
			resource.setId(-1);
		}
		oldRes = list.get(resource.getName());
		if (oldRes != null) {
			throw new IllegalArgumentException("" + kind + " " + resource.getName() + " has duplicate name!");
		}
		list.add(resource);
		if (resource.getId() >= list.lastId) {
			list.lastId = resource.getId();
		}
		ResNode child = new ResNode(resource.getName(),
				ResNode.STATUS_SECONDARY, kind, resource.reference);
		parent.add(child);
	}
}
