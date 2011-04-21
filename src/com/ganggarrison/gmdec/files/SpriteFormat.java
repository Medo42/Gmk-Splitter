/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.files;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.Sprite;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.ResourceTreeEntry;
import com.ganggarrison.gmdec.xml.SpriteXmlFormat;

public class SpriteFormat extends ResourceFormat<Sprite> {
	@Override
	public Sprite read(File path, ResourceTreeEntry entry, DeferredReferenceCreatorNotifier drcn) throws IOException {
		Sprite sprite = new SpriteXmlFormat().read(getXmlFile(path, entry), drcn);
		sprite.setName(entry.name);

		File imagesDir = new File(path, baseFilename(entry) + ".images");
		if (imagesDir.isDirectory()) {
			readImages(sprite, imagesDir);
		}
		return sprite;
	}

	private void readImages(Sprite sprite, File imagesDir) throws IOException {
		File[] imageFiles = imagesDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toLowerCase().matches("image \\d+\\.png");
			}
		});

		int index = 0;
		boolean imageFound = true;
		do {
			File imageFile = new File(imagesDir, "image " + index + ".png");
			if (imageFile.isFile()) {
				sprite.subImages.add(ImageIO.read(imageFile));
			} else {
				imageFound = false;
			}
			index++;
		} while (imageFound);
		if (index - 1 != imageFiles.length) {
			System.err.println("The image directory " + imagesDir
					+ " contains non-consecutive indices. Images after the first index gap won't be processed.");
		}
	}

	@Override
	public void write(File path, Sprite sprite, GmFile gmf) throws IOException {
		new SpriteXmlFormat().write(sprite, getXmlFile(path, sprite));

		File subPath = null;
		if (sprite.subImages.size() > 0) {
			subPath = new File(path, baseFilename(sprite) + ".images");
			if (!subPath.mkdirs()) {
				throw new IOException("Cannot create path " + subPath + ", it already exists.");
			}
		}

		for (int i = 0; i < sprite.subImages.size(); i++) {
			BufferedImage image = sprite.subImages.get(i);
			ImageIO.write(image, "PNG", new File(subPath, "image " + i + ".png"));
		}
	}
}
