/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.files;

import java.io.File;
import java.io.IOException;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.file.ResourceList;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Resource.Kind;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.FileTools;

public abstract class ResourceFormat<T> {
	public abstract void write(File path, T resource, GmFile gmf) throws IOException;

	public abstract void addResToGmFile(T resource, GmFile gmf, ResNode parent);

	public abstract T read(File path, String resourceName, DeferredReferenceCreatorNotifier drcn)
			throws IOException;

	public <U extends Resource<U, ?>> void addResToTree(U resource, GmFile gmf, ResNode parent) {
		Kind kind = resource.getKind();
		ResourceList<U> list = (ResourceList<U>) gmf.getList(kind);
		U oldRes = list.getUnsafe(resource.getId());
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

	protected String defaultFilestring(Resource<?, ?> resource) throws IOException {
		return defaultFilestring(resource.getName());
	}

	protected String defaultFilestring(String name) throws IOException {
		if (FileTools.isGoodFilename(name)) {
			return name;
		}
		throw new IOException("Ressource name \"" + name + "\" can't be used as a filename, aborting.");
	}

	protected File getXmlFile(File path, Resource<?, ?> resource) throws IOException {
		return new File(path, defaultFilestring(resource) + ".xml");
	}

	protected File getXmlFile(File path, String resourceName) throws IOException {
		return new File(path, defaultFilestring(resourceName) + ".xml");
	}
}
