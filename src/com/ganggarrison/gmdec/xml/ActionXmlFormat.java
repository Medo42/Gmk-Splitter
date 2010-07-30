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
			out.putElement("kind", actionKindToString(la.actionKind));
			out.putElement("allowRelative", la.allowRelative);
			out.putElement("question", la.question);
			out.putElement("canApplyTo", la.canApplyTo);
			out.putElement("actionType", actionTypeToString(la.execType));
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
				out.putAttribute("kind", argumentKindToString(arg.kind));
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
		la.actionKind = stringToActionKind(reader.getStringElement("kind"));
		la.allowRelative = reader.getBoolElement("allowRelative");
		la.question = reader.getBoolElement("question");
		la.canApplyTo = reader.getBoolElement("canApplyTo");
		la.execType = stringToActionType(reader.getStringElement("actionType"));
		la.execInfo = reader.getStringElement("functionName");

		boolean relative = reader.getBoolElement("relative");
		boolean not = reader.getBoolElement("not");

		String appliesTo = reader.getStringElement("appliesTo");

		reader.enterElement("arguments");
		List<Argument> args = new ArrayList<Argument>();
		while (reader.hasNextElement()) {
			reader.enterElement("argument");
			byte kind = stringToArgumentKind(reader.getStringAttribute("kind"));
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

	private static enum ArgumentKind {
		EXPRESSION(0),
		STRING(1),
		BOTH(2),
		BOOLEAN(3),
		MENU(4),
		SPRITE(5),
		SOUND(6),
		BACKGROUND(7),
		PATH(8),
		SCRIPT(9),
		GMOBJECT(10),
		ROOM(11),
		FONT(12),
		COLOR(13),
		TIMELINE(14),
		FONTSTRING(15);

		public final int lgmconst;

		private ArgumentKind(int lgmconst) {
			this.lgmconst = lgmconst;
		}
	}

	private static String argumentKindToString(byte kind) {
		for (ArgumentKind ak : ArgumentKind.values()) {
			if (ak.lgmconst == kind) {
				return ak.toString();
			}
		}
		throw new IllegalArgumentException("Unknown argument kind " + kind);
	}

	private static byte stringToArgumentKind(String str) {
		return (byte) ArgumentKind.valueOf(str).lgmconst;
	}

	private static enum ActionType {
		NONE(0),
		FUNCTION(1),
		CODE(2);

		public final int lgmconst;

		private ActionType(int lgmconst) {
			this.lgmconst = lgmconst;
		}
	}

	private static String actionTypeToString(byte actionType) {
		for (ActionType at : ActionType.values()) {
			if (at.lgmconst == actionType) {
				return at.toString();
			}
		}
		throw new IllegalArgumentException("Unknown action type " + actionType);
	}

	private static byte stringToActionType(String str) {
		return (byte) ActionType.valueOf(str.toUpperCase()).lgmconst;
	}

	private static enum ActionKind {
		NORMAL(0),
		BEGIN(1),
		END(2),
		ELSE(3),
		EXIT(4),
		REPEAT(5),
		VARIABLE(6),
		CODE(7),
		PLACEHOLDER(8),
		SEPARATOR(9),
		LABEL(10);

		public final int lgmconst;

		private ActionKind(int lgmconst) {
			this.lgmconst = lgmconst;
		}
	}

	private static String actionKindToString(byte actionKind) {
		for (ActionKind ak : ActionKind.values()) {
			if (ak.lgmconst == actionKind) {
				return ak.toString();
			}
		}
		throw new IllegalArgumentException("Unknown action kind " + actionKind);
	}

	private static byte stringToActionKind(String str) {
		return (byte) ActionKind.valueOf(str.toUpperCase()).lgmconst;
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
