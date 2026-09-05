/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 *
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.nio.charset.Charset;

/**
 * Additional information about a split directory tree that has no equivalent
 * in the game file. Stored in _metadata.xml in the root of the tree.
 */
public class TreeMetadata {
	public static final String FILENAME = "_metadata.xml";

	/**
	 * The charset that was used to decode the text of the source .gmk file when
	 * the tree was created. Null if unknown, or if the source was a .gm81 file
	 * (whose text is always UTF-8 and doesn't depend on a system code page).
	 */
	public Charset charset;
}
