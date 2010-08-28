/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import java.awt.Dimension;
import java.awt.Point;

import org.lateralgm.resources.Background;
import org.lateralgm.resources.Background.PBackground;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.GmkSplitter;

public class BackgroundXmlFormat extends XmlFormat<Background> {
	@Override
	public void write(Background background, XmlWriter writer) {
		writer.startElement("background");
		{
			writeIdAttribute(background, writer);
			boolean useAsTileset = background.get(PBackground.USE_AS_TILESET);
			writer.putElement("useAsTileset", useAsTileset);
			if (useAsTileset || !GmkSplitter.omitDisabledFields) {
				writer.startElement("tiles");
				int width = background.get(PBackground.TILE_WIDTH);
				int height = background.get(PBackground.TILE_HEIGHT);
				writeDimension(writer, "size", new Dimension(width, height));

				int offsetX = background.get(PBackground.H_OFFSET);
				int offsetY = background.get(PBackground.V_OFFSET);
				writePoint(writer, "offset", new Point(offsetX, offsetY));

				int sepX = background.get(PBackground.H_SEP);
				int sepY = background.get(PBackground.V_SEP);
				writePoint(writer, "separation", new Point(sepX, sepY));
				writer.endElement();
			}
			writer.putElement("preload", background.get(PBackground.PRELOAD));
			writer.putElement("smoothEdges", background.get(PBackground.SMOOTH_EDGES));
			writer.putElement("transparent", background.get(PBackground.TRANSPARENT));
		}
		writer.endElement();
	}

	@Override
	public Background read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		Background background = new Background();
		reader.enterElement("background");
		{
			readIdAttribute(background, reader);
			boolean useAsTileset = reader.getBoolElement("useAsTileset");
			background.put(PBackground.USE_AS_TILESET, useAsTileset);
			if (useAsTileset || !GmkSplitter.omitDisabledFields) {
				reader.enterElement("tiles");
				Dimension size = readDimension(reader, "size");
				background.put(PBackground.TILE_WIDTH, size.width);
				background.put(PBackground.TILE_HEIGHT, size.height);

				Point offset = readPoint(reader, "offset");
				background.put(PBackground.H_OFFSET, offset.x);
				background.put(PBackground.V_OFFSET, offset.y);

				Point separation = readPoint(reader, "separation");
				background.put(PBackground.H_SEP, separation.x);
				background.put(PBackground.V_SEP, separation.y);
				reader.leaveElement();
			}
			background.put(PBackground.PRELOAD, reader.getBoolElement("preload"));
			background.put(PBackground.SMOOTH_EDGES, reader.getBoolElement("smoothEdges"));
			background.put(PBackground.TRANSPARENT, reader.getBoolElement("transparent"));
		}
		reader.leaveElement();
		return background;
	}
}
