/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 *
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.lateralgm.file.GmStreamEncoder;

/**
 * A GmStreamEncoder that keeps track of strings which contain characters that
 * cannot be represented in the active charset. Such strings are written with
 * replacement characters (like the base class does) and counted, so that the
 * user can be warned about the lossy conversion.
 */
public class CheckedGmStreamEncoder extends GmStreamEncoder {
	private final CharsetProblems problems = new CharsetProblems();
	private Charset encoderCharset;
	private CharsetEncoder encoder;

	public CheckedGmStreamEncoder(OutputStream o) {
		super(o);
	}

	public CharsetProblems getProblems() {
		return problems;
	}

	@Override
	public void writeStr(String str) throws IOException {
		check(str);
		super.writeStr(str);
	}

	@Override
	public void writeStr1(String str) throws IOException {
		check(str);
		super.writeStr1(str);
	}

	private void check(String str) {
		if (isAscii(str)) {
			return;
		}
		problems.countNonAscii();
		if (!getEncoder(getCharset()).canEncode(str)) {
			problems.addProblem(str);
		}
	}

	private CharsetEncoder getEncoder(Charset charset) {
		if (encoder == null || !charset.equals(encoderCharset)) {
			encoderCharset = charset;
			encoder = charset.newEncoder();
		}
		return encoder;
	}

	private static boolean isAscii(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) >= 128) {
				return false;
			}
		}
		return true;
	}
}
