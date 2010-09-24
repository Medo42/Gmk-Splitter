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

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.Resource;

import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.FileTools;
import com.ganggarrison.gmdec.xml.GameInfoXmlFormat;

public class GameInfoFormat extends FileTreeFormat<GameInformation> {
	private static final String filename = "Game Information";

	@Override
	public GameInformation read(File path, String resourceName, DeferredReferenceCreatorNotifier drcn)
			throws IOException {
		File xmlFile = new File(path, filename + ".xml");
		File textFile = new File(path, filename + ".txt");
		GameInformation info = new GameInfoXmlFormat().read(xmlFile, drcn);
		info.gameInfoStr = FileTools.readFileAsString(textFile);
		return info;
	}

	@Override
	public void addResToGmFile(GameInformation resource, GmFile gmf, ResNode parent) {
		gmf.gameInfo = resource;
		parent.addChild("Game Information", ResNode.STATUS_SECONDARY, Resource.Kind.GAMEINFO);
	}

	@Override
	public void write(File path, GameInformation gameInfo, GmFile gmf) throws IOException {
		File xmlFile = new File(path, filename + ".xml");
		new GameInfoXmlFormat().write(gameInfo, xmlFile);
		FileTools.writeFile(new File(path, filename + ".txt"), gameInfo.gameInfoStr);
	}
}
