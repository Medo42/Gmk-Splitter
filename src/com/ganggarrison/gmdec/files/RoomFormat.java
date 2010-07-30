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
import org.lateralgm.resources.Resource.Kind;
import org.lateralgm.resources.Room;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.xml.RoomXmlFormat;

public class RoomFormat extends ResourceFormat<Room> {
	@Override
	public Room read(File path, String resourceName, DeferredReferenceCreatorNotifier drcn) throws IOException {
		Room room = new RoomXmlFormat().read(getXmlFile(path, resourceName), drcn);
		room.setName(resourceName);
		return room;
	}

	@Override
	public void addResToGmFile(Room resource, GmFile gmf, ResNode parent) {
		addResToTree(resource, gmf, parent, Kind.ROOM);
	}

	@Override
	public void write(File path, Room room, GmFile gmf) throws IOException {
		new RoomXmlFormat().write(room, getXmlFile(path, room));
	}

}
