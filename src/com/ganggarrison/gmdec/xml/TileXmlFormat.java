/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import org.lateralgm.resources.Background;
import org.lateralgm.resources.Resource.Kind;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Tile.PTile;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredPropertyReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;

public class TileXmlFormat extends XmlFormat<Tile> {
	private Room room;

	public TileXmlFormat(Room room) {
		this.room = room;
	}

	@Override
	public void write(Tile tile, XmlWriter writer) {
		writer.startElement("tile");
		{
			writer.putAttribute("id", tile.properties.get(PTile.ID));
			ResourceReference<Background> bgRef = tile.properties.get(PTile.BACKGROUND);
			writeResourceRef(writer, "background", bgRef);
			writePoint(writer, "backgroundPosition", tile.getBackgroundPosition());
			writePoint(writer, "roomPosition", tile.getRoomPosition());
			writeDimension(writer, "size", tile.getSize());
			writer.putElement("depth", tile.getDepth());
			writer.putElement("locked", tile.isLocked());
		}
		writer.endElement();
	}

	@Override
	public Tile read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		Tile tile;
		reader.enterElement("tile");
		{
			tile = new Tile(room, reader.getIntAttribute("id"));
			String objRef = readResourceRef(reader, "background");
			DeferredReferenceCreator rc = new DeferredPropertyReferenceCreator<PTile>(
					tile.properties, PTile.BACKGROUND, Kind.BACKGROUND, objRef);
			notifier.addDeferredReferenceCreator(rc);
			tile.setBackgroundPosition(readPoint(reader, "backgroundPosition"));
			tile.setRoomPosition(readPoint(reader, "roomPosition"));
			tile.setSize(readDimension(reader, "size"));
			tile.setDepth(reader.getIntElement("depth"));
			tile.setLocked(reader.getBoolElement("locked"));
		}
		reader.leaveElement();
		return tile;
	}
}
