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
import org.lateralgm.resources.Font;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.ResourceTreeEntry;
import com.ganggarrison.gmdec.xml.FontXmlFormat;

public class FontFormat extends ResourceFormat<Font> {
	@Override
	public Font read(File path, ResourceTreeEntry entry, DeferredReferenceCreatorNotifier drcn) throws IOException {
		Font font = new FontXmlFormat().read(getXmlFile(path, entry), drcn);
		font.setName(entry.name);
		return font;
	}

	@Override
	public void write(File path, Font font, GmFile gmf) throws IOException {
		new FontXmlFormat().write(font, getXmlFile(path, font));
	}
}
