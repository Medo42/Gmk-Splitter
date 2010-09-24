/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * Copyright (C) 2008, 2009 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.Resource.Kind;
import org.lateralgm.resources.ResourceReference;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredPropertyReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;

public class GmObjectXmlFormat extends XmlFormat<GmObject> {

	@Override
	public void write(GmObject gmObject, XmlWriter out) {
		out.startElement("object");
		{
			writeIdAttribute(gmObject, out);
			writeResourceRef(out, "sprite", (ResourceReference<?>) gmObject.get(PGmObject.SPRITE));
			out.putElement("solid", gmObject.get(PGmObject.SOLID));
			out.putElement("visible", gmObject.get(PGmObject.VISIBLE));
			out.putElement("depth", gmObject.get(PGmObject.DEPTH));
			out.putElement("persistent", gmObject.get(PGmObject.PERSISTENT));
			writeResourceRef(out, "parent", (ResourceReference<?>) gmObject.get(PGmObject.PARENT));
			writeResourceRef(out, "mask", (ResourceReference<?>) gmObject.get(PGmObject.MASK));
		}
		out.endElement();
	}

	@Override
	public GmObject read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		GmObject gmObject = new GmObject();
		reader.enterElement("object");
		{
			readIdAttribute(gmObject, reader);
			String spriteRef = readResourceRef(reader, "sprite");
			DeferredReferenceCreator rc = new DeferredPropertyReferenceCreator<PGmObject>(
					gmObject.properties, PGmObject.SPRITE, Kind.SPRITE, spriteRef);
			notifier.addDeferredReferenceCreator(rc);
			gmObject.put(PGmObject.SOLID, reader.getBoolElement("solid"));
			gmObject.put(PGmObject.VISIBLE, reader.getBoolElement("visible"));
			gmObject.put(PGmObject.DEPTH, reader.getIntElement("depth"));
			gmObject.put(PGmObject.PERSISTENT, reader.getBoolElement("persistent"));
			String parentRef = readResourceRef(reader, "parent");
			rc = new DeferredPropertyReferenceCreator<PGmObject>(
					gmObject.properties, PGmObject.PARENT, Kind.OBJECT, parentRef);
			notifier.addDeferredReferenceCreator(rc);
			String maskRef = readResourceRef(reader, "mask");
			rc = new DeferredPropertyReferenceCreator<PGmObject>(
					gmObject.properties, PGmObject.MASK, Kind.SPRITE, maskRef);
			notifier.addDeferredReferenceCreator(rc);
		}
		reader.leaveElement();
		return gmObject;
	}
}
