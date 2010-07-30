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
import java.io.File;
import java.io.IOException;

import org.lateralgm.resources.ResourceReference;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;

public abstract class XmlFormat<T> {
	protected boolean convertLineEndings = true;
	protected boolean omitDisabledFields = true;

	public abstract void write(T object, XmlWriter writer);

	public abstract T read(XmlReader reader, DeferredReferenceCreatorNotifier notifier);

	public final void write(T object, File file) throws IOException {
		if (file.exists()) {
			throw new IOException("File " + file + " already exists.");
		}
		XmlWriter out = new XmlWriter();
		write(object, out);
		out.write(file);
	}

	public final T read(File file, DeferredReferenceCreatorNotifier notifier) throws IOException {
		XmlReader reader = new XmlReader(file);
		return read(reader, notifier);
	}

	protected static String getRefStr(ResourceReference<?> ref) {
		if (ref != null && ref.get() != null) {
			return ref.get().getName();
		} else {
			return "";
		}
	}

	protected static void writeResourceRef(XmlWriter out, String elemName, ResourceReference<?> ref) {
		out.putElement(elemName, getRefStr(ref));
	}

	protected static String readResourceRef(XmlReader in, String elemName) {
		return in.getStringElement(elemName);
	}

	protected static void writePoint(XmlWriter writer, String elementName, Point point) {
		writer.startElement(elementName);
		writer.putAttribute("x", point.x);
		writer.putAttribute("y", point.y);
		writer.endElement();
	}

	protected static Point readPoint(XmlReader reader, String elementName) {
		reader.enterElement(elementName);
		int x = reader.getIntAttribute("x");
		int y = reader.getIntAttribute("y");
		reader.leaveElement();
		return new Point(x, y);
	}

	protected static void writeDimension(XmlWriter writer, String elementName, Dimension d) {
		writer.startElement(elementName);
		writer.putAttribute("width", d.width);
		writer.putAttribute("height", d.height);
		writer.endElement();
	}

	protected static Dimension readDimension(XmlReader reader, String elementName) {
		reader.enterElement(elementName);
		int width = reader.getIntAttribute("width");
		int height = reader.getIntAttribute("height");
		reader.leaveElement();
		return new Dimension(width, height);
	}
}
