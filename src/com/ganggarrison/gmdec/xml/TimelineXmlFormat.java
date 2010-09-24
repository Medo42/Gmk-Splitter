/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Moment;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;

public class TimelineXmlFormat extends XmlFormat<Timeline> {

	@Override
	public void write(Timeline timeline, XmlWriter writer) {
		writer.startElement("timeline");
		{
			writeIdAttribute(timeline, writer);
			for (Moment moment : timeline.moments) {
				writer.startElement("moment");
				writer.putAttribute("stepNo", moment.stepNo);
				ActionXmlFormat actionFormat = new ActionXmlFormat();
				for (Action action : moment.actions) {
					actionFormat.write(action, writer);
				}
				writer.endElement();
			}
		}
		writer.endElement();
	}

	@Override
	public Timeline read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		Timeline timeline = new Timeline();
		reader.enterElement("timeline");
		{
			readIdAttribute(timeline, reader);
			while (reader.hasNextElement()) {
				Moment moment = timeline.addMoment();
				reader.enterElement("moment");
				moment.stepNo = reader.getIntAttribute("stepNo");
				ActionXmlFormat actionFormat = new ActionXmlFormat();
				while (reader.hasNextElement()) {
					moment.actions.add(actionFormat.read(reader, notifier));
				}
				reader.leaveElement();
			}
		}
		reader.leaveElement();
		return timeline;
	}
}
