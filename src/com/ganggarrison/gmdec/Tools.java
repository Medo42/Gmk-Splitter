/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.awt.Color;

public class Tools {
	public static String colorToString(Color color) {
		if (color.getAlpha() == 255) {
			long rgb = color.getRGB() & 0xffffffL;
			return String.format("%06X", rgb);
		} else {
			long rgb = color.getRGB() & 0xffffffffL;
			return String.format("%08X", rgb);
		}
	}

	public static Color stringToColor(String value) {
		try {
			long l = Long.parseLong(value, 16);
			if (l < 0) {
				throw new IllegalArgumentException("Color string " + value + " is invalid.");
			}
			if (value.length() == 6) {
				return new Color((int) l, false);
			} else if (value.length() == 8) {
				return new Color((int) l, true);
			} else {
				throw new IllegalArgumentException("Color string " + value
						+ " must consist of either 6 or 8 hex digits.");
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String toLf(String val) {
		return val.replace("\r\n", "\n").replace("\r", "\n");
	}

	public static String toCrlf(String val) {
		return toLf(val).replace("\n", "\r\n");
	}
}
