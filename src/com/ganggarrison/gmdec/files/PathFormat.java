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

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.Path;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.ResourceTreeEntry;
import com.ganggarrison.gmdec.xml.PathXmlFormat;

public class PathFormat extends ResourceFormat<Path> {
	@Override
	public Path read(File filePath, ResourceTreeEntry entry, DeferredReferenceCreatorNotifier drcn) throws IOException {
		Path path = new PathXmlFormat().read(getXmlFile(filePath, entry), drcn);
		path.setName(entry.name);
		return path;
	}

	@Override
	public void write(File filePath, Path path, GmFile gmf) throws IOException {
		new PathXmlFormat().write(path, getXmlFile(filePath, path));
	}
}
