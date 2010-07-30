/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;

public class EventXmlFormat extends XmlFormat<Event> {
	@Override
	public void write(Event event, XmlWriter out) {
		out.startElement("event");
		{
			out.putAttribute("category", mainEventTypeToString(event.mainId));
			// TODO: Trigger events probably need special attention too.
			if (event.mainId == MainEvent.EV_COLLISION) {
				out.putAttribute("with", getRefStr(event.other));
			} else {
				out.putAttribute("id", event.id);
			}
			out.startElement("actions");
			for (Action act : event.actions) {
				new ActionXmlFormat().write(act, out);
			}
			out.endElement();
		}
		out.endElement();
	}

	@Override
	public Event read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		Event event = new Event();

		reader.enterElement("event");
		{
			int category = stringToMainEventType(reader.getStringAttribute("category"));
			event.mainId = category;

			if (event.mainId == MainEvent.EV_COLLISION) {
				ReferenceCreator rc = new ReferenceCreator(event);
				rc.setRef(reader.getStringAttribute("with"));
				notifier.addDeferredReferenceCreator(rc);
			} else {
				event.id = reader.getIntAttribute("id");
			}

			reader.enterElement("actions");
			while (reader.hasNextElement()) {
				event.actions.add(new ActionXmlFormat().read(reader, notifier));
			}
			reader.leaveElement();
		}
		reader.leaveElement();
		return event;
	}

	private static enum MainEventType {
		CREATE(0),
		DESTROY(1),
		ALARM(2),
		STEP(3),
		COLLISION(4),
		KEYBOARD(5),
		MOUSE(6),
		OTHER(7),
		DRAW(8),
		KEYPRESS(9),
		KEYRELEASE(10),
		TRIGGER(11);

		public final int lgmconst;

		private MainEventType(int lgmconst) {
			this.lgmconst = lgmconst;
		}
	}

	private static String mainEventTypeToString(int mainEventType) {
		for (MainEventType met : MainEventType.values()) {
			if (met.lgmconst == mainEventType) {
				return met.toString();
			}
		}
		throw new IllegalArgumentException("Unknown argument mainEventType " + mainEventType);
	}

	private static byte stringToMainEventType(String str) {
		return (byte) MainEventType.valueOf(str.toUpperCase()).lgmconst;
	}

	private static class ReferenceCreator implements DeferredReferenceCreator {
		private Event event;
		private String name;

		public ReferenceCreator(Event event) {
			this.event = event;
		}

		public void setRef(String name) {
			this.name = name;
		}

		@Override
		public void createReferences(GmFile gmf) {
			GmObject collisionObject = gmf.gmObjects.get(name);
			if (collisionObject != null) {
				event.other = collisionObject.reference;
				event.id = collisionObject.getId();
			} else {
				System.err.println("Warning: GM-Event references unknown "
						+ "collision object " + name);
			}
		}
	}
}
