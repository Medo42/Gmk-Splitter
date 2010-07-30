/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileTools {
	public static void writeFile(File file, String content) throws IOException {
		writeFile(file, content.getBytes("UTF-8"));
	}

	public static void writeFile(File file, byte[] content) throws IOException {
		FileOutputStream fos = null;
		DataOutputStream dos = null;

		try {
			fos = new FileOutputStream(file);
			dos = new DataOutputStream(fos);
			dos.write(content);
		} finally {
			tryToClose(fos);
			tryToClose(dos);
		}
	}

	/**
	 * Modify the given String so that it can be used as part of a filename
	 * without causing problems from illegal/special characters.
	 * 
	 * The result should be similar to the input, but isn't necessarily
	 * reversible. The implementation might change in the future, so don't rely
	 * on the output staying the same.
	 */
	public static String replaceBadChars(String name) {
		if (name == null || name.trim().isEmpty())
			return "_";
		name = name.trim();
		for (String badChar : badChars) {
			name = name.replace(badChar, "_");
		}
		return name;
	}

	public static boolean isGoodFilename(String name) {
		if (name == null || name.trim().isEmpty())
			return false;
		if (!name.trim().equals(name)) {
			return false;
		}
		for (String s : badChars) {
			if (name.contains(s))
				return false;
		}
		return true;
	}

	private static final String[] badChars = new String[] { "/", "\\", ":", "*", "?", "\"", "<", ">", "|", ".", "\0" };

	public static String readFileAsString(File file) throws IOException {
		return new String(readWholeFileBytes(file), "UTF-8");
	}

	public static byte[] readWholeFileBytes(File file) throws IOException {
		FileInputStream fis = null;
		DataInputStream dis = null;
		try {
			fis = new FileInputStream(file);
			dis = new DataInputStream(fis);
			byte[] buffer = new byte[(int) file.length()];
			dis.readFully(buffer);

			return buffer;
		} finally {
			tryToClose(fis);
			tryToClose(dis);
		}
	}

	private static void tryToClose(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}
}
