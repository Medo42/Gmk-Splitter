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
	public Type type;

	public ResourceTreeEntry(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public ResourceTreeEntry() {
	}

	public static enum Type {
		RESOURCE, GROUP
	}
}