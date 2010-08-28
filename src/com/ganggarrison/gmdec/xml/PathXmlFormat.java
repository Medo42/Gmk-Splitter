/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import java.awt.Point;

import org.lateralgm.resources.Path;
import org.lateralgm.resources.Path.PPath;
import org.lateralgm.resources.Resource.Kind;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.sub.PathPoint;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredPropertyReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;

public class PathXmlFormat extends XmlFormat<Path> {

	@Override
	public void write(Path path, XmlWriter writer) {
		writer.startElement("path");
		{
			writeIdAttribute(path, writer);
			writer.startElement("points");
			for (PathPoint pp : path.points) {
				writer.startElement("point");
				writer.putAttribute("x", pp.getX());
				writer.putAttribute("y", pp.getY());
				writer.putAttribute("speed", pp.getSpeed());
				writer.endElement();
			}
			writer.endElement();
			writeResourceRef(writer, "backgroundRoom", (ResourceReference<?>) path.get(PPath.BACKGROUND_ROOM));
			writer.putElement("closed", path.get(PPath.CLOSED));
			writer.putElement("precision", path.get(PPath.PRECISION));
			writer.putElement("smooth", path.get(PPath.SMOOTH));

			int snapX = path.get(PPath.SNAP_X);
			int snapY = path.get(PPath.SNAP_Y);
			writePoint(writer, "snap", new Point(snapX, snapY));
		}
		writer.endElement();
	}

	@Override
	public Path read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		Path path = new Path();

		reader.enterElement("path");
		{
			readIdAttribute(path, reader);
			reader.enterElement("points");
			while (reader.hasNextElement()) {
				reader.enterElement("point");
				PathPoint pp = path.addPoint();
				pp.setX(reader.getIntAttribute("x"));
				pp.setY(reader.getIntAttribute("y"));
				pp.setSpeed(reader.getIntAttribute("speed"));
				reader.leaveElement();
			}
			reader.leaveElement();
			String backgroundRoomRef = readResourceRef(reader, "backgroundRoom");
			DeferredReferenceCreator rc = new DeferredPropertyReferenceCreator<PPath>(
					path.properties, PPath.BACKGROUND_ROOM, Kind.ROOM, backgroundRoomRef);
			notifier.addDeferredReferenceCreator(rc);
			path.put(PPath.CLOSED, reader.getBoolElement("closed"));
			path.put(PPath.PRECISION, reader.getIntElement("precision"));
			path.put(PPath.SMOOTH, reader.getBoolElement("smooth"));

			Point snap = readPoint(reader, "snap");
			path.put(PPath.SNAP_X, snap.x);
			path.put(PPath.SNAP_Y, snap.y);
		}
		reader.leaveElement();
		return path;
	}
}
