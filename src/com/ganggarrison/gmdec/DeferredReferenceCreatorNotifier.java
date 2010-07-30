/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.util.HashSet;
import java.util.Set;

import org.lateralgm.file.GmFile;

/**
 * This class maintains a list of DeferredReferenceCreator objects which will
 * need to create references in their wrapped model objects to other model
 * objects. See DeferredReferenceCreator for details.
 * 
 * When the GmFile is read completely from the file tree, createReferences will
 * be called on all DeferredReferenceCreator objects in this list.
 */
public class DeferredReferenceCreatorNotifier {
	private Set<DeferredReferenceCreator> drcSet = new HashSet<DeferredReferenceCreator>();

	public void addDeferredReferenceCreator(DeferredReferenceCreator drc) {
		drcSet.add(drc);
	}

	public void createReferences(GmFile gmf) {
		for (DeferredReferenceCreator drc : drcSet) {
			drc.createReferences(gmf);
		}
	}
}
