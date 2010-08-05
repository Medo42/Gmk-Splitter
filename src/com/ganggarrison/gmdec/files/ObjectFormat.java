/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.files;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.EventNamer;
import com.ganggarrison.gmdec.xml.EventXmlFormat;
import com.ganggarrison.gmdec.xml.GmObjectXmlFormat;

public class ObjectFormat extends ResourceFormat<GmObject> {
	@Override
	public GmObject read(File path, String resourceName, DeferredReferenceCreatorNotifier drcn) throws IOException {
		GmObject gmObject = new GmObjectXmlFormat().read(getXmlFile(path, resourceName), drcn);
		gmObject.setName(resourceName);

		File subdir = new File(path, defaultFilestring(resourceName) + ".events");
		if (subdir.isDirectory()) {
			File[] xmlFiles = subdir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isFile()
							&& pathname.getName().toLowerCase().endsWith(".xml");
				}
			});

			for (File file : xmlFiles) {
				Event event = new EventXmlFormat().read(file, drcn);
				try {
					gmObject.mainEvents.get(event.mainId).events.add(event);
				} catch (IndexOutOfBoundsException e) {
					throw new IOException("Unknown main event type: " + event.mainId, e);
				}
			}
		}
		return gmObject;
	}

	@Override
	public void write(File path, GmObject gmObject, GmFile gmf) throws IOException {
		new GmObjectXmlFormat().write(gmObject, getXmlFile(path, gmObject));

		File subdir = new File(path, defaultFilestring(gmObject) + ".events");

		boolean subdirCreated = false;
		for (MainEvent me : gmObject.mainEvents) {
			for (Event e : me.events) {
				if (!subdirCreated) {
					if (!subdir.mkdir()) {
						throw new IOException("Directory " + subdir + " already exists.");
					}
					subdirCreated = true;
				}
				String eventName = EventNamer.createName(e, gmf);
				File eventFile = new File(subdir, eventName + ".xml");
				new EventXmlFormat().write(e, eventFile);
			}
		}
	}
}
