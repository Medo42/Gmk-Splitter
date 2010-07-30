/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import org.lateralgm.file.GmFile;

/**
 * Some model objects from LateralGM hold references to other model objects.
 * These references can't be created directly when reading the objects, since
 * the target objects (denoted by resource id) might not have been read yet.
 * 
 * To solve this problem, all wrapper beans that are used for reading model
 * objects which may reference other model objects should implement this
 * interface. The method createReferences will be called after all objects have
 * been read, so that the references can be created properly.
 * 
 * In order for this to work, all code that creates DeferredReferenceCreators
 * must make sure that they are added to a DeferredReferenceCreatorNotifier, to
 * ensure that the method will actually get called.
 */
public interface DeferredReferenceCreator {
	void createReferences(GmFile gmf);
}
