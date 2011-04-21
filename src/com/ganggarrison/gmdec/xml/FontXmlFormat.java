/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import org.lateralgm.resources.Font;
import org.lateralgm.resources.Font.PFont;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;

public class FontXmlFormat extends XmlFormat<Font> {

	@Override
	public void write(Font font, XmlWriter writer) {
		writer.startElement("font");
		{
			writeIdAttribute(font, writer);
			writer.putElement("fontName", font.get(PFont.FONT_NAME));
			writer.putElement("bold", font.get(PFont.BOLD));
			writer.putElement("italic", font.get(PFont.ITALIC));
			writer.putElement("rangeMin", font.get(PFont.RANGE_MIN));
			writer.putElement("rangeMax", font.get(PFont.RANGE_MAX));
			writer.putElement("size", font.get(PFont.SIZE));
			writer.putElement("charset", font.get(PFont.CHARSET));
			writer.putElement("antialias", font.get(PFont.ANTIALIAS));
		}
		writer.endElement();
	}

	@Override
	public Font read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		Font font = new Font();
		reader.enterElement("font");
		{
			readIdAttribute(font, reader);
			font.put(PFont.FONT_NAME, reader.getStringElement("fontName"));
			font.put(PFont.BOLD, reader.getBoolElement("bold"));
			font.put(PFont.ITALIC, reader.getBoolElement("italic"));
			font.put(PFont.RANGE_MIN, reader.getIntElement("rangeMin"));
			font.put(PFont.RANGE_MAX, reader.getIntElement("rangeMax"));
			font.put(PFont.SIZE, reader.getIntElement("size"));
			if (reader.hasNextElement()) {
				font.put(PFont.CHARSET, reader.getIntElement("charset"));
			}
			if (reader.hasNextElement()) {
				font.put(PFont.ANTIALIAS, reader.getIntElement("antialias"));
			}
		}
		reader.leaveElement();
		return font;
	}
}
