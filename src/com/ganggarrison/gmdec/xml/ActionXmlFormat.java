/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * Copyright (C) 2008, 2009 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import java.util.ArrayList;
import java.util.List;

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Resource.Kind;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Argument;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.LgmConst;
import com.ganggarrison.gmdec.Tools;

public class ActionXmlFormat extends XmlFormat<Action> {
	@Override
	public void write(Action act, XmlWriter out) {
		out.startElement("action");
		{
			LibAction la = act.getLibAction();
			out.putAttribute("library", la.parent != null ? la.parent.id : la.parentId);
			out.putAttribute("id", la.id);
			if (la.name != null && !la.name.trim().isEmpty()) {
				out.putComment("action name: " + la.name);
			}
			out.putElement("kind", LgmConst.toString(la.actionKind, ActionKind.class));
			out.putElement("allowRelative", la.allowRelative);
			out.putElement("question", la.question);
			out.putElement("canApplyTo", la.canApplyTo);
			out.putElement("actionType", LgmConst.toString(la.execType, ExecType.class));
			out.putElement("functionName", la.execInfo);
			out.putElement("relative", act.isRelative());
			out.putElement("not", act.isNot());

			ResourceReference<GmObject> at = act.getAppliesTo();
			if (at == GmObject.OBJECT_OTHER) {
				out.putElement("appliesTo", ".other");
			} else if (at == GmObject.OBJECT_SELF) {
				out.putElement("appliesTo", ".self");
			} else {
				writeResourceRef(out, "appliesTo", at);
			}

			out.startElement("arguments");
			List<Argument> args = act.getArguments();
			for (Argument arg : args) {
				out.startElement("argument");
				out.putAttribute("kind", LgmConst.toString(arg.kind, ArgumentKind.class));
				if (Argument.getResourceKind(arg.kind) != null) {
					ResourceReference<? extends Resource<?, ?>> ref = arg.getRes();
					out.putText(getRefStr(ref));
				} else if (convertLineEndings && la.execType == Action.EXEC_CODE && arg.kind == Argument.ARG_STRING) {
					out.putText(Tools.toLf(arg.getVal()));
				} else {
					out.putText(arg.getVal());
				}
				out.endElement();
			}
			out.endElement();
		}
		out.endElement();
	}

	@Override
	public Action read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		reader.enterElement("action");

		int library = reader.getIntAttribute("library");
		int id = reader.getIntAttribute("id");

		LibAction la = LibManager.getLibAction(library, id);
		if (la == null) {
			la = new LibAction();
		}

		la.id = id;
		la.parentId = library;
		la.actionKind = LgmConst.fromString(reader.getStringElement("kind"), ActionKind.class);
		la.allowRelative = reader.getBoolElement("allowRelative");
		la.question = reader.getBoolElement("question");
		la.canApplyTo = reader.getBoolElement("canApplyTo");
		la.execType = LgmConst.fromString(reader.getStringElement("actionType"), ExecType.class);
		la.execInfo = reader.getStringElement("functionName");

		boolean relative = reader.getBoolElement("relative");
		boolean not = reader.getBoolElement("not");

		String appliesTo = reader.getStringElement("appliesTo");

		reader.enterElement("arguments");
		List<Argument> args = new ArrayList<Argument>();
		while (reader.hasNextElement()) {
			reader.enterElement("argument");
			byte kind = LgmConst.fromString(reader.getStringAttribute("kind"), ArgumentKind.class);
			Argument arg = new Argument(kind);
			if (Argument.getResourceKind(kind) != null) {
				String ref = reader.getTextContent();
				if (!ref.isEmpty()) {
					ArgumentReferenceCreator arc = new ArgumentReferenceCreator(arg);
					arc.setReference(ref, Argument.getResourceKind(kind));
					notifier.addDeferredReferenceCreator(arc);
				}
			} else if (convertLineEndings && la.execType == Action.EXEC_CODE && arg.kind == Argument.ARG_STRING) {
				arg.setVal(Tools.toCrlf(reader.getTextContent()));
			} else {
				arg.setVal(reader.getTextContent());
			}
			args.add(arg);
			reader.leaveElement();
		}
		reader.leaveElement();
		reader.leaveElement();

		Action act = new Action(la, args.toArray(new Argument[args.size()]));

		act.setRelative(relative);
		act.setNot(not);

		if (".self".equals(appliesTo.toLowerCase())) {
			act.setAppliesTo(GmObject.OBJECT_SELF);
		} else if (".other".equals(appliesTo.toLowerCase())) {
			act.setAppliesTo(GmObject.OBJECT_OTHER);
		} else {
			ActionReferenceCreator rc = new ActionReferenceCreator(act);
			rc.setReference(appliesTo);
			notifier.addDeferredReferenceCreator(rc);
		}
		return act;
	}

	private static enum ArgumentKind implements LgmConst.Provider {
		EXPRESSION(Argument.ARG_EXPRESSION),
		STRING(Argument.ARG_STRING),
		BOTH(Argument.ARG_BOTH),
		BOOLEAN(Argument.ARG_BOOLEAN),
		MENU(Argument.ARG_MENU),
		SPRITE(Argument.ARG_SPRITE),
		SOUND(Argument.ARG_SOUND),
		BACKGROUND(Argument.ARG_BACKGROUND),
		PATH(Argument.ARG_PATH),
		SCRIPT(Argument.ARG_SCRIPT),
		GMOBJECT(Argument.ARG_GMOBJECT),
		ROOM(Argument.ARG_ROOM),
		FONT(Argument.ARG_FONT),
		COLOR(Argument.ARG_COLOR),
		TIMELINE(Argument.ARG_TIMELINE),
		FONTSTRING(Argument.ARG_FONTSTRING);

		public final byte lgmconst;

		private ArgumentKind(byte lgmconst) {
			this.lgmconst = lgmconst;
		}

		@Override
		public byte getLgmConst() {
			return lgmconst;
		}
	}

	private static enum ExecType implements LgmConst.Provider {
		NONE(Action.EXEC_NONE),
		FUNCTION(Action.EXEC_FUNCTION),
		CODE(Action.EXEC_CODE);

		public final byte lgmconst;

		private ExecType(byte lgmconst) {
			this.lgmconst = lgmconst;
		}

		@Override
		public byte getLgmConst() {
			return lgmconst;
		}
	}

	private static enum ActionKind implements LgmConst.Provider {
		NORMAL(Action.ACT_NORMAL),
		BEGIN(Action.ACT_BEGIN),
		END(Action.ACT_END),
		ELSE(Action.ACT_ELSE),
		EXIT(Action.ACT_EXIT),
		REPEAT(Action.ACT_REPEAT),
		VARIABLE(Action.ACT_VARIABLE),
		CODE(Action.ACT_CODE),
		PLACEHOLDER(Action.ACT_PLACEHOLDER),
		SEPARATOR(Action.ACT_SEPARATOR),
		LABEL(Action.ACT_LABEL);

		public final byte lgmconst;

		private ActionKind(byte lgmconst) {
			this.lgmconst = lgmconst;
		}

		@Override
		public byte getLgmConst() {
			return lgmconst;
		}
	}

	private static class ActionReferenceCreator implements DeferredReferenceCreator {
		private Action action;
		private String name;

		public ActionReferenceCreator(Action action) {
			this.action = action;
		}

		public void setReference(String name) {
			this.name = name;
		}

		@Override
		public void createReferences(GmFile gmf) {
			if (name != null && !name.isEmpty()) {
				GmObject refObj = gmf.gmObjects.get(name);
				if (refObj != null) {
					action.setAppliesTo(refObj.reference);
				} else {
					System.err.println("Warning: Action references unknown "
							+ "object " + name);
				}
			}
		}
	}

	private static class ArgumentReferenceCreator implements DeferredReferenceCreator {
		private Argument arg;
		private String name;
		private Kind reskind;

		public ArgumentReferenceCreator(Argument arg) {
			this.arg = arg;
		}

		public void setReference(String ref, Kind reskind) {
			this.name = ref;
			this.reskind = reskind;
		}

		@Override
		public void createReferences(GmFile gmf) {
			if (name != null && !name.isEmpty()) {
				Resource<?, ?> refObj = gmf.getList(reskind).get(name);
				if (refObj != null) {
					arg.setRes(refObj.reference);
				} else {
					System.err.println("Warning: Argument references unknown "
							+ reskind + " " + name);
				}
			}
		}
	}
}
