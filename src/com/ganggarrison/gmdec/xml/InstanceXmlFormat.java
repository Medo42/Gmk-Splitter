/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource.Kind;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.util.PropertyMap;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredPropertyReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.GmkSplitter;
import com.ganggarrison.gmdec.Tools;
import com.ganggarrison.gmdec.GmkSplitter.IdPreservation;

public class InstanceXmlFormat extends XmlFormat<Instance> {
	private Room room;

	public InstanceXmlFormat(Room room) {
		this.room = room;
	}

	@Override
	public void write(Instance instance, XmlWriter writer) {
		writer.startElement("instance");
		{
			PropertyMap<PInstance> properties = instance.properties;
			if (preserveInstanceIds()) {
				writer.putAttribute("id", properties.get(PInstance.ID));
			}
			ResourceReference<GmObject> object = properties.get(PInstance.OBJECT);
			writeResourceRef(writer, "object", object);
			writePoint(writer, "position", instance.getPosition());
			String creationCode = instance.getCreationCode();
			if (GmkSplitter.convertLineEndings) {
				creationCode = Tools.toLf(creationCode);
			}
			writer.putElement("creationCode", creationCode);
			writer.putElement("locked", instance.isLocked());
		}
		writer.endElement();
	}

	private boolean preserveInstanceIds() {
		return GmkSplitter.preserveIds == IdPreservation.ALL
				|| GmkSplitter.preserveIds == IdPreservation.OBJECTS_INSTANCES;
	}

	@Override
	public Instance read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		Instance instance = new Instance(room);
		reader.enterElement("instance");
		{
			PropertyMap<PInstance> properties = instance.properties;
			if (preserveInstanceIds() && reader.hasAttribute("id")) {
				properties.put(PInstance.ID, reader.getIntAttribute("id"));
			}
			String objRef = readResourceRef(reader, "object");
			DeferredReferenceCreator rc = new DeferredPropertyReferenceCreator<PInstance>(
					properties, PInstance.OBJECT, Kind.OBJECT, objRef);
			notifier.addDeferredReferenceCreator(rc);
			instance.setPosition(readPoint(reader, "position"));
			String creationCode = reader.getStringElement("creationCode");
			if (GmkSplitter.convertLineEndings) {
				creationCode = Tools.toCrlf(creationCode);
			}
			instance.setCreationCode(creationCode);
			instance.setLocked(reader.getBoolElement("locked"));
		}
		reader.leaveElement();
		return instance;
	}

}
