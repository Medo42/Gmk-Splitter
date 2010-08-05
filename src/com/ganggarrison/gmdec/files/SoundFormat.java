/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.files;

import java.io.File;
import java.io.IOException;

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sound.PSound;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.FileTools;
import com.ganggarrison.gmdec.xml.SoundXmlFormat;

public class SoundFormat extends ResourceFormat<Sound> {
	@Override
	public Sound read(File path, String resourceName, DeferredReferenceCreatorNotifier drcn) throws IOException {
		Sound sound = new SoundXmlFormat().read(getXmlFile(path, resourceName), drcn);
		sound.setName(resourceName);

		File soundfile = getSoundfile(path, sound);
		if (soundfile.isFile()) {
			sound.data = FileTools.readWholeFileBytes(soundfile);
		}
		return sound;
	}

	@Override
	public void write(File path, Sound sound, GmFile gmf) throws IOException {
		new SoundXmlFormat().write(sound, getXmlFile(path, sound));
		boolean dataEmpty = (sound.data == null || sound.data.length == 0);
		if (!dataEmpty) {
			File soundfile = getSoundfile(path, sound);
			FileTools.writeFile(soundfile, sound.data);
		}
	}

	private File getSoundfile(File path, Sound sound) throws IOException {
		String filename = defaultFilestring(sound.getName());
		String filetype = sound.get(PSound.FILE_TYPE);
		if (filetype != null && filetype.startsWith(".")
				&& FileTools.isGoodFilename(filename + filetype.substring(1))) {
			return new File(path, filename + filetype);
		} else {
			System.err.print("Bad file extension \"" + filetype + "\" in sound \"" + filename + "\", ");
			System.err.println("not using any extension for this file.");
			return new File(path, filename);
		}
	}
}
