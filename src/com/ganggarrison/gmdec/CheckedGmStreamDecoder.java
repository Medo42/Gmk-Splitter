/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 *
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.lateralgm.file.GmStreamDecoder;

/**
 * A GmStreamDecoder that keeps track of strings which could not be decoded
 * properly with the active charset. Such strings are decoded with replacement
 * characters (like the base class does) and counted, so that the user can be
 * warned that the charset used for the game file is probably wrong.
 */
public class CheckedGmStreamDecoder extends GmStreamDecoder {
	private final CharsetProblems problems = new CharsetProblems();
	private Charset decoderCharset;
	private CharsetDecoder strictDecoder;

	public CheckedGmStreamDecoder(InputStream in) {
		super(in);
	}

	public CharsetProblems getProblems() {
		return problems;
	}

	@Override
	public String readStr() throws IOException {
		byte data[] = new byte[read4()];
		read(data);
		return decode(data);
	}

	@Override
	public String readStr1() throws IOException {
		byte data[] = new byte[read()];
		read(data);
		return decode(data);
	}

	private String decode(byte[] data) {
		Charset charset = getCharset();
		String result = new String(data, charset);
		if (isAscii(data)) {
			return result;
		}
		problems.countNonAscii();
		try {
			getStrictDecoder(charset).decode(ByteBuffer.wrap(data));
		} catch (CharacterCodingException e) {
			problems.addProblem(result);
		}
		return result;
	}

	private CharsetDecoder getStrictDecoder(Charset charset) {
		if (strictDecoder == null || !charset.equals(decoderCharset)) {
			decoderCharset = charset;
			strictDecoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT);
		}
		strictDecoder.reset();
		return strictDecoder;
	}

	private static boolean isAscii(byte[] data) {
		for (byte b : data) {
			if (b < 0) {
				return false;
			}
		}
		return true;
	}
}
