/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 *
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.util.ArrayList;
import java.util.List;

/**
 * Statistics about the charset conversion of the strings in a game file, used
 * to generate warnings about wrong or lossy charset choices.
 */
public class CharsetProblems {
	private static final int MAX_EXAMPLES = 3;
	private static final int MAX_EXAMPLE_LENGTH = 40;

	private int nonAsciiStrings = 0;
	private int problemStrings = 0;
	private final List<String> examples = new ArrayList<String>();

	void countNonAscii() {
		nonAsciiStrings++;
	}

	void addProblem(String str) {
		problemStrings++;
		if (examples.size() < MAX_EXAMPLES) {
			examples.add(abbreviate(str));
		}
	}

	/**
	 * Number of strings that contained anything but plain ASCII characters, i.e.
	 * strings whose bytes actually depend on the charset.
	 */
	public int getNonAsciiStringCount() {
		return nonAsciiStrings;
	}

	/**
	 * Number of strings that could not be converted without loss.
	 */
	public int getProblemStringCount() {
		return problemStrings;
	}

	/**
	 * A few examples of strings that could not be converted without loss, for
	 * display in warning messages. Single-line and abbreviated.
	 */
	public List<String> getExamples() {
		return examples;
	}

	private static String abbreviate(String str) {
		String oneLine = str.replaceAll("\\s+", " ").trim();
		if (oneLine.length() > MAX_EXAMPLE_LENGTH) {
			oneLine = oneLine.substring(0, MAX_EXAMPLE_LENGTH - 3) + "...";
		}
		return oneLine;
	}
}
