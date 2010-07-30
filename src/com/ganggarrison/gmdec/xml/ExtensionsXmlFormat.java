/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import java.util.ArrayList;
import java.util.List;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;

public class ExtensionsXmlFormat extends XmlFormat<List<String>> {

	@Override
	public void write(List<String> extensions, XmlWriter writer) {
		writer.startElement("extensionPackages");
		for (String packageName : extensions) {
			writer.putElement("package", packageName);
		}
		writer.endElement();
	}

	@Override
	public List<String> read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		ArrayList<String> extensions = new ArrayList<String>();
		reader.enterElement("extensionPackages");
		while (reader.hasNextElement()) {
			extensions.add(reader.getStringElement("package"));
		}
		reader.leaveElement();
		return extensions;
	}

}
