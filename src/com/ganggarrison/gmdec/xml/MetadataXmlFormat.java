/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 *
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.TreeMetadata;

/**
 * Format of the _metadata.xml file in the root of a split tree. All elements
 * are optional, and unknown elements are ignored so that trees written by other
 * versions of the tool can still be read.
 */
public class MetadataXmlFormat extends XmlFormat<TreeMetadata> {
	@Override
	public void write(TreeMetadata metadata, XmlWriter writer) {
		writer.startElement("metadata");
		writer.putComment(" Information about this tree that has no equivalent in the game file. ");
		if (metadata.charset != null) {
			writer.putComment(" Charset for the .gmk file this tree was created from. Used by default when converting back to a .gmk file (ignored for .gm81 files). ");
			writer.putElement("charset", metadata.charset.name());
		}
		writer.endElement();
	}

	@Override
	public TreeMetadata read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		return read(reader);
	}

	public TreeMetadata read(XmlReader reader) {
		TreeMetadata metadata = new TreeMetadata();
		reader.enterElement("metadata");
		while (reader.hasNextElement()) {
			if (reader.hasNextElement("charset")) {
				metadata.charset = parseCharset(reader.getStringElement("charset").trim());
			} else {
				reader.skipElement();
			}
		}
		reader.leaveElement();
		return metadata;
	}

	private static Charset parseCharset(String name) {
		try {
			return Charset.forName(name);
		} catch (IllegalCharsetNameException e) {
			throw new IllegalArgumentException("Unsupported charset in " + TreeMetadata.FILENAME + ": " + name);
		} catch (UnsupportedCharsetException e) {
			throw new IllegalArgumentException("Unsupported charset in " + TreeMetadata.FILENAME + ": " + name);
		}
	}
}
