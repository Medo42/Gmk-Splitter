/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import java.util.ArrayList;
import java.util.List;

import org.lateralgm.resources.sub.Constant;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;

public class ConstantsXmlFormat extends XmlFormat<List<Constant>> {

	@Override
	public void write(List<Constant> constants, XmlWriter writer) {
		writer.startElement("constants");
		for (Constant c : constants) {
			writer.startElement("constant");
			writer.putAttribute("name", c.name);
			writer.putAttribute("value", c.value);
			writer.endElement();
		}
		writer.endElement();
	}

	@Override
	public List<Constant> read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		return read(reader);
	}

	public List<Constant> read(XmlReader reader) {
		ArrayList<Constant> constants = new ArrayList<Constant>();
		reader.enterElement("constants");
		while (reader.hasNextElement()) {
			Constant c = new Constant();
			reader.enterElement("constant");
			c.name = reader.getStringAttribute("name");
			c.value = reader.getStringAttribute("value");
			reader.leaveElement();
			constants.add(c);
		}
		reader.leaveElement();
		return constants;
	}
}
