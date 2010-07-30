/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sound.PSound;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;

public class SoundXmlFormat extends XmlFormat<Sound> {
	@Override
	public void write(Sound sound, XmlWriter writer) {
		writer.startElement("sound");
		{
			writer.putAttribute("id", sound.getId());
			writer.putElement("filename", sound.get(PSound.FILE_NAME));
			writer.putElement("filetype", sound.get(PSound.FILE_TYPE));
			writer.putElement("kind", sound.get(PSound.KIND));
			writer.putElement("pan", sound.get(PSound.PAN));
			writer.putElement("volume", sound.get(PSound.VOLUME));
			writer.putElement("preload", sound.get(PSound.PRELOAD));
			writer.startElement("effects");
			{
				writer.putElement("chorus", sound.get(PSound.CHORUS));
				writer.putElement("echo", sound.get(PSound.ECHO));
				writer.putElement("flanger", sound.get(PSound.FLANGER));
				writer.putElement("gargle", sound.get(PSound.GARGLE));
				writer.putElement("reverb", sound.get(PSound.REVERB));
			}
			writer.endElement();
		}
		writer.endElement();
	}

	@Override
	public Sound read(XmlReader reader,
			DeferredReferenceCreatorNotifier notifier) {
		Sound sound = new Sound();
		reader.enterElement("sound");
		{
			sound.setId(reader.getIntAttribute("id"));
			sound.put(PSound.FILE_NAME, reader.getStringElement("filename"));
			sound.put(PSound.FILE_TYPE, reader.getStringElement("filetype"));
			String kind = reader.getStringElement("kind");
			sound.put(PSound.KIND, Sound.SoundKind.valueOf(kind));
			sound.put(PSound.PAN, reader.getDoubleElement("pan"));
			sound.put(PSound.VOLUME, reader.getDoubleElement("volume"));
			sound.put(PSound.PRELOAD, reader.getBoolElement("preload"));

			reader.enterElement("effects");
			{
				sound.put(PSound.CHORUS, reader.getBoolElement("chorus"));
				sound.put(PSound.ECHO, reader.getBoolElement("echo"));
				sound.put(PSound.FLANGER, reader.getBoolElement("flanger"));
				sound.put(PSound.GARGLE, reader.getBoolElement("gargle"));
				sound.put(PSound.REVERB, reader.getBoolElement("reverb"));
			}
			reader.leaveElement();
		}
		reader.leaveElement();
		return sound;
	}

}
