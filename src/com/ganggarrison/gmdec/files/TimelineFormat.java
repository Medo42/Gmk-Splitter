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
import org.lateralgm.resources.Timeline;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;

public class TimelineFormat extends ResourceFormat<Timeline> {
	@Override
	public Timeline read(File path, String resourceName, DeferredReferenceCreatorNotifier drcn) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addResToGmFile(Timeline resource, GmFile gmf, ResNode parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(File path, Timeline timeline, GmFile gmf) throws IOException {
		// TODO Auto-generated method stub

	}

}
