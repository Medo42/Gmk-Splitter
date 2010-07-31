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
import org.lateralgm.resources.sub.Trigger;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.xml.TriggerXmlFormat;

public class TriggerFormat extends ResourceFormat<Trigger> {
	@Override
	public Trigger read(File path, String resourceName, DeferredReferenceCreatorNotifier drcn) throws IOException {
		Trigger trigger = new TriggerXmlFormat().read(getXmlFile(path, resourceName), drcn);
		trigger.name = resourceName;
		return trigger;
	}

	@Override
	public void addResToGmFile(Trigger resource, GmFile gmf, ResNode parent) {
		// TODO
	}

	@Override
	public void write(File path, Trigger trigger, GmFile gmf) throws IOException {
		new TriggerXmlFormat().write(trigger, getXmlFile(path, trigger.name));
	}
}
