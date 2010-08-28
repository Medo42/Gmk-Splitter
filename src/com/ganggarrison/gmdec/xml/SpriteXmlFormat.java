/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import java.awt.Point;

import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Sprite.PSprite;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.GmkSplitter;

public class SpriteXmlFormat extends XmlFormat<Sprite> {
	@Override
	public void write(Sprite sprite, XmlWriter writer) {
		writer.startElement("sprite");
		{
			writeIdAttribute(sprite, writer);
			int originX = sprite.get(PSprite.ORIGIN_X);
			int originY = sprite.get(PSprite.ORIGIN_Y);
			writePoint(writer, "origin", new Point(originX, originY));
			writer.startElement("mask");
			{
				writer.putElement("separate", sprite.get(PSprite.SEPARATE_MASK));
				writer.putElement("shape", sprite.get(PSprite.SHAPE));
				writer.startElement("bounds");
				{
					Sprite.BBMode mode = sprite.get(PSprite.BB_MODE);
					writer.putAttribute("mode", mode);
					writer.putAttribute("alphaTolerance", sprite.get(PSprite.ALPHA_TOLERANCE));
					if (mode == Sprite.BBMode.MANUAL || !GmkSplitter.omitDisabledFields) {
						writer.putElement("left", sprite.get(PSprite.BB_LEFT));
						writer.putElement("right", sprite.get(PSprite.BB_RIGHT));
						writer.putElement("top", sprite.get(PSprite.BB_TOP));
						writer.putElement("bottom", sprite.get(PSprite.BB_BOTTOM));
					}
				}
				writer.endElement();
			}
			writer.endElement();
			writer.putElement("preload", sprite.get(PSprite.PRELOAD));
			writer.putElement("smoothEdges", sprite.get(PSprite.SMOOTH_EDGES));
			writer.putElement("transparent", sprite.get(PSprite.TRANSPARENT));
		}
		writer.endElement();
	}

	@Override
	public Sprite read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		Sprite sprite = new Sprite();
		reader.enterElement("sprite");
		{
			readIdAttribute(sprite, reader);
			Point origin = readPoint(reader, "origin");
			sprite.put(PSprite.ORIGIN_X, origin.x);
			sprite.put(PSprite.ORIGIN_Y, origin.y);
			reader.enterElement("mask");
			{
				sprite.put(PSprite.SEPARATE_MASK, reader.getBoolElement("separate"));
				String shape = reader.getStringElement("shape");
				sprite.put(PSprite.SHAPE, Sprite.MaskShape.valueOf(shape));
				reader.enterElement("bounds");
				{
					Sprite.BBMode mode = Sprite.BBMode.valueOf(reader.getStringAttribute("mode"));
					sprite.put(PSprite.BB_MODE, mode);
					sprite.put(PSprite.ALPHA_TOLERANCE, reader.getIntAttribute("alphaTolerance"));
					if (mode == Sprite.BBMode.MANUAL || !GmkSplitter.omitDisabledFields) {
						sprite.put(PSprite.BB_LEFT, reader.getIntElement("left"));
						sprite.put(PSprite.BB_RIGHT, reader.getIntElement("right"));
						sprite.put(PSprite.BB_TOP, reader.getIntElement("top"));
						sprite.put(PSprite.BB_BOTTOM, reader.getIntElement("bottom"));
					}
				}
				reader.leaveElement();
			}
			reader.leaveElement();
			sprite.put(PSprite.PRELOAD, reader.getBoolElement("preload"));
			sprite.put(PSprite.SMOOTH_EDGES, reader.getBoolElement("smoothEdges"));
			sprite.put(PSprite.TRANSPARENT, reader.getBoolElement("transparent"));
		}
		reader.leaveElement();
		return sprite;
	}
}
