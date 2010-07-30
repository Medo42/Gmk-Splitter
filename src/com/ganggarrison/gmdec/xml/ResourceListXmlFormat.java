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
import com.ganggarrison.gmdec.ResourceTreeEntry;
import com.ganggarrison.gmdec.ResourceTreeEntry.Type;

public class ResourceListXmlFormat extends XmlFormat<List<ResourceTreeEntry>> {

	@Override
	public void write(List<ResourceTreeEntry> resources, XmlWriter writer) {
		writer.startElement("resources");
		for (ResourceTreeEntry rte : resources) {
			writer.startElement("resource");
			writer.putAttribute("name", rte.name);
			writer.putAttribute("type", rte.type);
			writer.endElement();
		}
		writer.endElement();
	}

	@Override
	public List<ResourceTreeEntry> read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		return read(reader);
	}

	public List<ResourceTreeEntry> read(XmlReader reader) {
		List<ResourceTreeEntry> resources = new ArrayList<ResourceTreeEntry>();
		reader.enterElement("resources");
		while (reader.hasNextElement()) {
			reader.enterElement("resource");
			ResourceTreeEntry entry = new ResourceTreeEntry();
			entry.name = reader.getStringAttribute("name");
			entry.type = Type.valueOf(reader.getStringAttribute("type"));
			resources.add(entry);
			reader.leaveElement();
		}
		reader.leaveElement();
		return resources;
	}
}
