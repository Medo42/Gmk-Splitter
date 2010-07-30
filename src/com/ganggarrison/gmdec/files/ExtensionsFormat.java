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
import org.lateralgm.resources.Resource;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.xml.ExtensionsXmlFormat;

public class ExtensionsFormat extends ResourceFormat<List<String>> {
	private static String filename = "Extension Packages.xml";

	@Override
	public List<String> read(File path, String resourceName, DeferredReferenceCreatorNotifier drcn)
			throws IOException {
		return new ExtensionsXmlFormat().read(new File(path, filename), drcn);
	}

	@Override
	public void addResToGmFile(List<String> extensions, GmFile gmf, ResNode parent) {
		gmf.packages.clear();
		gmf.packages.addAll(extensions);
		parent.addChild("Extension Packages", ResNode.STATUS_SECONDARY, Resource.Kind.EXTENSIONS);
	}

	@Override
	public void write(File path, List<String> extensions, GmFile gmf) throws IOException {
		new ExtensionsXmlFormat().write(extensions, new File(path, filename));
	}
}
