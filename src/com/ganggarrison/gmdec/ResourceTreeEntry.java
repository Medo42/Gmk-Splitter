/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

public class ResourceTreeEntry {
	public String name;
	public String filename;
	public Type type;

	public ResourceTreeEntry(String name, String filename, Type type) {
		this.name = name;
		this.filename = filename;
		this.type = type;
	}

	public ResourceTreeEntry() {
	}

	public String getFilename() {
		String result;
		if (filename != null && !filename.isEmpty()) {
			result = filename;
		} else {
			result = name;
		}
		if (FileTools.isGoodFilename(result)) {
			return result;
		} else {
			throw new IllegalStateException("Bad filename in resource list: " + result);
		}
	}

	public static enum Type {
		RESOURCE, GROUP
	}
}