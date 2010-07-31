/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import org.lateralgm.resources.sub.Trigger;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.LgmConst;
import com.ganggarrison.gmdec.Tools;

public class TriggerXmlFormat extends XmlFormat<Trigger> {

	public static enum MomentOfChecking implements LgmConst.Provider {
		STEP_BEGIN(0), STEP_MIDDLE(1), STEP_END(2);

		byte lgmConst;

		private MomentOfChecking(int lgmConst) {
			this.lgmConst = (byte) lgmConst;
		}

		@Override
		public byte getLgmConst() {
			return lgmConst;
		}
	}

	@Override
	public void write(Trigger trigger, XmlWriter writer) {
		writer.startElement("trigger");
		{
			String condition = trigger.condition;
			if(convertLineEndings) {
				condition = Tools.toLf(condition);
			}
			writer.putElement("condition", condition);
			String checkStep = LgmConst.toString((byte) trigger.checkStep, MomentOfChecking.class);
			writer.putElement("momentOfChecking", checkStep);
			writer.putElement("constantName", trigger.constant);
		}
		writer.endElement();
	}

	@Override
	public Trigger read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		Trigger trigger = new Trigger();
		reader.enterElement("trigger");
		{
			String condition = reader.getStringElement("condition");
			if (convertLineEndings) {
				condition = Tools.toCrlf(condition);
			}
			trigger.condition = condition;
			String checkStep = reader.getStringElement("momentOfChecking");
			trigger.checkStep = LgmConst.fromString(checkStep, MomentOfChecking.class);
			trigger.constant = reader.getStringElement("constantName");
		}
		reader.leaveElement();
		return trigger;
	}

}
