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
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.Background;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.xml.BackgroundXmlFormat;

public class BackgroundFormat extends ResourceFormat<Background> {
	@Override
	public Background read(File path, String resourceName, DeferredReferenceCreatorNotifier drcn)
			throws IOException {
		File imageFile = new File(path, defaultFilestring(resourceName) + ".png");
		Background background = new BackgroundXmlFormat()
				.read(getXmlFile(path, resourceName), drcn);
		background.setName(resourceName);

		if (imageFile.isFile()) {
			BufferedImage bg = ImageIO.read(imageFile);
			background.setBackgroundImage(bg);
		}
		return background;
	}

	@Override
	public void write(File path, Background background, GmFile gmf) throws IOException {
		new BackgroundXmlFormat().write(background, getXmlFile(path, background));

		BufferedImage image = background.getBackgroundImage();
		if (image != null) {
			ImageIO.write(image, "PNG", new File(path, defaultFilestring(background) + ".png"));
		}
	}

}
