/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

public class EventNamer {
	/**
	 * Creates a nice, human-readable and consistent (i.e. independent of
	 * locale) name for a given event.
	 * 
	 * The event should be complete, that is, the reference to its collision
	 * object should exist if it is a collision event, otherwise the name will
	 * not be optimal.
	 * 
	 * The reason why this function is needed and Gmk splitter can't just rely
	 * on Event.toString() is that the resulting string is used as the filename
	 * for storing the event data, and the files are supposed to be good for
	 * version control. Therefore it would be bad if different collaborators in
	 * different countries got different filenames.
	 */
	public static String createName(Event event, GmFile gmf) {
		switch (event.mainId) {
		case MainEvent.EV_CREATE:
			return "Create";
		case MainEvent.EV_DESTROY:
			return "Destroy";
		case MainEvent.EV_ALARM:
			return "Alarm " + event.id;
		case MainEvent.EV_STEP:
			return stepEventNames.get(event.id);
		case MainEvent.EV_COLLISION:
			if (event.other == null || event.other.get() == null) {
				return "Collision with Object " + event.id;
			} else {
				return "Collision with " + event.other.get().getName();
			}
		case MainEvent.EV_KEYBOARD:
			String keyname = KEY_NAMES.get(event.id);
			if (keyname != null) {
				return "Key " + keyname;
			} else {
				return "Key code " + event.id;
			}
		case MainEvent.EV_MOUSE:
			String eventName = MOUSE_EVENT_NAMES.get(event.id);
			if (eventName != null) {
				return eventName;
			} else {
				return "Mouse unknown " + event.id;
			}
		case MainEvent.EV_OTHER:
			if (OTHER_EVENT_NAMES.containsKey(event.id)) {
				return OTHER_EVENT_NAMES.get(event.id);
			} else if (event.id >= 10 && event.id <= 25) {
				return "User Event " + (event.id - 10);
			} else if (event.id >= 40 && event.id <= 47) {
				return "Outside View " + (event.id - 40);
			} else if (event.id >= 50 && event.id <= 57) {
				return "Boundary View " + (event.id - 50);
			} else {
				return "Other Event " + event.id;
			}
		case MainEvent.EV_DRAW:
			return "Draw";
		case MainEvent.EV_KEYPRESS:
			keyname = KEY_NAMES.get(event.id);
			if (keyname != null) {
				return "Key " + keyname + " pressed";
			} else {
				return "Key code " + event.id + " pressed";
			}
		case MainEvent.EV_KEYRELEASE:
			keyname = KEY_NAMES.get(event.id);
			if (keyname != null) {
				return "Key " + keyname + " released";
			} else {
				return "Key code " + event.id + " released";
			}
		case MainEvent.EV_TRIGGER:
			if (event.id >= 0 && event.id < gmf.triggers.size() && gmf.triggers.get(event.id) != null) {
				return "Trigger " + gmf.triggers.get(event.id).name;
			} else {
				return "Trigger id " + event.id;
			}
		default:
			System.err.println("Warning: Unknown event category " + event.mainId + " encountered.");
			return Integer.toString(event.mainId);
		}
	}

	private static List<String> stepEventNames = Arrays.asList(new String[] {
			"Step",
			"Begin Step",
			"End Step"
	});

	private static final Map<Integer, String> OTHER_EVENT_NAMES = createOtherEventNames();

	private static Map<Integer, String> createOtherEventNames() {
		Map<Integer, String> result = new HashMap<Integer, String>();
		result.put(0, "Outside Room");
		result.put(1, "Intersect Boundary");
		result.put(2, "Game Start");
		result.put(3, "Game End");
		result.put(4, "Room Start");
		result.put(5, "Room End");
		result.put(6, "No more lives");
		result.put(7, "Animation end");
		result.put(8, "End of Path");
		result.put(9, "No more health");
		result.put(30, "Close Button");
		return Collections.unmodifiableMap(result);
	}

	private static final Map<Integer, String> MOUSE_EVENT_NAMES = createMouseEventNames();

	private static Map<Integer, String> createMouseEventNames() {
		Map<Integer, String> result = new HashMap<Integer, String>();
		result.put(0, "Mouse left button");
		result.put(1, "Mouse right button");
		result.put(2, "Mouse middle button");
		result.put(3, "Mouse no button");
		result.put(4, "Mouse left button pressed");
		result.put(5, "Mouse right button pressed");
		result.put(6, "Mouse middle button pressed");
		result.put(7, "Mouse left button released");
		result.put(8, "Mouse right button released");
		result.put(9, "Mouse middle button released");
		result.put(10, "Mouse enter");
		result.put(11, "Mouse leave");
		result.put(16, "Joystick 1 left");
		result.put(17, "Joystick 1 right");
		result.put(18, "Joystick 1 up");
		result.put(19, "Joystick 1 down");
		result.put(21, "Joystick 1 button 1");
		result.put(22, "Joystick 1 button 2");
		result.put(23, "Joystick 1 button 3");
		result.put(24, "Joystick 1 button 4");
		result.put(25, "Joystick 1 button 5");
		result.put(26, "Joystick 1 button 6");
		result.put(27, "Joystick 1 button 7");
		result.put(28, "Joystick 1 button 8");
		result.put(31, "Joystick 2 left");
		result.put(32, "Joystick 2 right");
		result.put(33, "Joystick 2 up");
		result.put(34, "Joystick 2 down");
		result.put(36, "Joystick 2 button 1");
		result.put(37, "Joystick 2 button 2");
		result.put(38, "Joystick 2 button 3");
		result.put(39, "Joystick 2 button 4");
		result.put(40, "Joystick 2 button 5");
		result.put(41, "Joystick 2 button 6");
		result.put(42, "Joystick 2 button 7");
		result.put(43, "Joystick 2 button 8");
		result.put(50, "Mouse global left button");
		result.put(51, "Mouse global right button");
		result.put(52, "Mouse global middle button");
		result.put(53, "Mouse global left pressed");
		result.put(54, "Mouse global right pressed");
		result.put(55, "Mouse global middle pressed");
		result.put(56, "Mouse global left released");
		result.put(57, "Mouse global right released");
		result.put(58, "Mouse global middle released");
		result.put(60, "Mouse wheel up");
		result.put(61, "Mouse wheel down");
		return Collections.unmodifiableMap(result);
	}

	private static final Map<Integer, String> KEY_NAMES = createKeyNames();

	private static Map<Integer, String> createKeyNames() {
		Map<Integer, String> result = new HashMap<Integer, String>();
		result.put(37, "left");
		result.put(39, "right");
		result.put(38, "up");
		result.put(40, "down");

		result.put(17, "control");
		result.put(18, "alt");
		result.put(16, "shift");
		result.put(32, "space");
		result.put(13, "enter");

		result.put(96, "numpad 0");
		result.put(97, "numpad 1");
		result.put(98, "numpad 2");
		result.put(99, "numpad 3");
		result.put(100, "numpad 4");
		result.put(101, "numpad 5");
		result.put(102, "numpad 6");
		result.put(103, "numpad 7");
		result.put(104, "numpad 8");
		result.put(105, "numpad 9");

		result.put(111, "numpad divide");
		result.put(106, "numpad multiply");
		result.put(109, "numpad subtract");
		result.put(107, "numpad add");
		result.put(110, "numpad decimal");

		result.put(48, "0");
		result.put(49, "1");
		result.put(50, "2");
		result.put(51, "3");
		result.put(52, "4");
		result.put(53, "5");
		result.put(54, "6");
		result.put(55, "7");
		result.put(56, "8");
		result.put(57, "9");

		result.put(65, "A");
		result.put(66, "B");
		result.put(67, "C");
		result.put(68, "D");
		result.put(69, "E");
		result.put(70, "F");
		result.put(71, "G");
		result.put(72, "H");
		result.put(73, "I");
		result.put(74, "J");
		result.put(75, "K");
		result.put(76, "L");
		result.put(77, "M");
		result.put(78, "N");
		result.put(79, "O");
		result.put(80, "P");
		result.put(81, "Q");
		result.put(82, "R");
		result.put(83, "S");
		result.put(84, "T");
		result.put(85, "U");
		result.put(86, "V");
		result.put(87, "W");
		result.put(88, "X");
		result.put(89, "Y");
		result.put(90, "Z");

		result.put(112, "f1");
		result.put(113, "f2");
		result.put(114, "f3");
		result.put(115, "f4");
		result.put(116, "f5");
		result.put(117, "f6");
		result.put(118, "f7");
		result.put(119, "f8");
		result.put(120, "f9");
		result.put(121, "f10");
		result.put(122, "f11");
		result.put(123, "f12");

		result.put(8, "backspace");
		result.put(27, "escape");
		result.put(36, "home");
		result.put(35, "end");
		result.put(33, "pageup");
		result.put(34, "pagedown");
		result.put(46, "delete");
		result.put(45, "insert");

		result.put(0, "no key");
		result.put(1, "any key");
		return Collections.unmodifiableMap(result);
	}
}
