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
import java.util.List;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.resources.Extensions;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.ResourceTreeEntry;
import com.ganggarrison.gmdec.ResourceTreeEntry.Type;
import com.ganggarrison.gmdec.xml.ExtensionsXmlFormat;

public class ExtensionsFormat extends FileTreeFormat<List<String>> {
	private static String baseFilename = "Extension Packages";
	private static String filename = baseFilename + ".xml";

	@Override
	public List<String> read(File path, ResourceTreeEntry entry, DeferredReferenceCreatorNotifier drcn)
			throws IOException {
		return new ExtensionsXmlFormat().read(new File(path, filename), drcn);
	}

	@Override
	public void addResToTree(List<String> resource, ResNode parent) {
		parent.addChild("Extension Packages", ResNode.STATUS_SECONDARY, Extensions.class);
	}

	@Override
	public ResourceTreeEntry createResourceTreeEntry(List<String> resource) {
		return new ResourceTreeEntry(baseFilename, baseFilename, Type.RESOURCE);
	}

	@Override
	public void addAllResourcesToGmFile(List<List<String>> resources, GmFile gmf) {
		if (resources.size() != 1) {
			throw new IllegalArgumentException("There is only one extension package list.");
		}
		gmf.packages.clear();
		gmf.packages.addAll(resources.get(0));
	}

	@Override
	public void write(File path, List<String> extensions, GmFile gmf) throws IOException {
		new ExtensionsXmlFormat().write(extensions, new File(path, filename));
	}
}
