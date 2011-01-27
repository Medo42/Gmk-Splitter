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
import org.lateralgm.resources.Timeline;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.ResourceTreeEntry;
import com.ganggarrison.gmdec.xml.TimelineXmlFormat;

public class TimelineFormat extends ResourceFormat<Timeline> {
	@Override
	public Timeline read(File path, ResourceTreeEntry entry, DeferredReferenceCreatorNotifier drcn) throws IOException {
		Timeline timeline = new TimelineXmlFormat().read(getXmlFile(path, entry), drcn);
		timeline.setName(entry.name);
		return timeline;
	}

	@Override
	public void write(File path, Timeline timeline, GmFile gmf) throws IOException {
		new TimelineXmlFormat().write(timeline, getXmlFile(path, timeline));
	}
}
