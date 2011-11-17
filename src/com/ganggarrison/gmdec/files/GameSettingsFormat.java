/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.files;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GameSettings.PGameSettings;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.FileTools;
import com.ganggarrison.gmdec.ResourceTreeEntry;
import com.ganggarrison.gmdec.ResourceTreeEntry.Type;
import com.ganggarrison.gmdec.xml.GameSettingsXmlFormat;

public class GameSettingsFormat extends FileTreeFormat<GameSettings> {
	private static final String baseFilename = "Global Game Settings";
	private static final String xmlFilename = baseFilename + ".xml";
	private static final String frontLoadBarFilename = "loadbar front.png";
	private static final String backLoadBarFilename = "loadbar background.png";
	private static final String loadingImageFilename = "loading image.png";
	private static final String iconFilename = "game icon.ico";

	@Override
	public GameSettings read(File path, ResourceTreeEntry entry, DeferredReferenceCreatorNotifier drcn)
			throws IOException {
		GameSettings settings = new GameSettingsXmlFormat().read(new File(path, xmlFilename), drcn);

		File frontLoadBarFile = new File(path, frontLoadBarFilename);
		if (frontLoadBarFile.isFile()) {
			settings.put(PGameSettings.FRONT_LOAD_BAR, ImageIO.read(frontLoadBarFile));
		}

		File backLoadBarFile = new File(path, backLoadBarFilename);
		if (backLoadBarFile.isFile()) {
			settings.put(PGameSettings.BACK_LOAD_BAR, ImageIO.read(backLoadBarFile));
		}

		File loadingImageFile = new File(path, loadingImageFilename);
		if (loadingImageFile.isFile()) {
			settings.put(PGameSettings.LOADING_IMAGE, ImageIO.read(loadingImageFile));
		}

		File iconFile = new File(path, iconFilename);
		if (iconFile.isFile()) {
			settings.put(PGameSettings.GAME_ICON, new ICOFile(FileTools.readWholeFileBytes(iconFile)));
		}
		return settings;
	}

	@Override
	public void addResToTree(GameSettings resource, ResNode parent) {
		parent.addChild("Global Game Settings", ResNode.STATUS_SECONDARY, GameSettings.class);
	}

	@Override
	public ResourceTreeEntry createResourceTreeEntry(GameSettings resource) {
		return new ResourceTreeEntry(baseFilename, baseFilename, Type.RESOURCE);
	}

	@Override
	public void addAllResourcesToGmFile(List<GameSettings> resources, GmFile gmf) {
		if (resources.size() != 1) {
			throw new IllegalArgumentException("There is only one game settings object.");
		}
		gmf.gameSettings = resources.get(0);
	}

	@Override
	public void write(File path, GameSettings settings, GmFile gmf) throws IOException {
		new GameSettingsXmlFormat().write(settings, new File(path, xmlFilename));

		if (settings.get(PGameSettings.FRONT_LOAD_BAR) != null) {
			ImageIO.write((RenderedImage) settings.get(PGameSettings.FRONT_LOAD_BAR), "PNG", new File(path,
					frontLoadBarFilename));
		}

		if (settings.get(PGameSettings.BACK_LOAD_BAR) != null) {
			ImageIO.write((RenderedImage) settings.get(PGameSettings.BACK_LOAD_BAR), "PNG", new File(path,
					backLoadBarFilename));
		}

		if (settings.get(PGameSettings.LOADING_IMAGE) != null) {
			ImageIO.write((RenderedImage) settings.get(PGameSettings.LOADING_IMAGE), "PNG", new File(path,
					loadingImageFilename));
		}

		if (settings.get(PGameSettings.GAME_ICON) != null) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File(path, iconFilename));
				((ICOFile) settings.get(PGameSettings.GAME_ICON)).write(fos);
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		}
	}
}
