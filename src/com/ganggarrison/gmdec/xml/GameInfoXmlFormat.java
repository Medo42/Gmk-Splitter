/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import java.awt.Color;

import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameInformation.PGameInformation;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.Tools;

public class GameInfoXmlFormat extends XmlFormat<GameInformation> {

	@Override
	public void write(GameInformation info, XmlWriter writer) {
		writer.startElement("gameInformation");
		{
			writer.startElement("windowPosition");
			{
				writer.putElement("left", info.get(PGameInformation.LEFT));
				writer.putElement("top", info.get(PGameInformation.TOP));
				writer.putElement("width", info.get(PGameInformation.WIDTH));
				writer.putElement("height", info.get(PGameInformation.HEIGHT));
			}
			writer.endElement();
			writer.putElement("allowResize", info.get(PGameInformation.ALLOW_RESIZE));
			writer.putElement("backgroundColor",
					Tools.colorToString((Color) info.get(PGameInformation.BACKGROUND_COLOR)));
			writer.putElement("formCaption", info.get(PGameInformation.FORM_CAPTION));
			writer.putElement("mimicGameWindow", info.get(PGameInformation.MIMIC_GAME_WINDOW));
			writer.putElement("pauseGame", info.get(PGameInformation.PAUSE_GAME));
			writer.putElement("showBorder", info.get(PGameInformation.SHOW_BORDER));
			writer.putElement("stayOnTop", info.get(PGameInformation.STAY_ON_TOP));
		}
		writer.endElement();
	}

	@Override
	public GameInformation read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		GameInformation info = new GameInformation();
		reader.enterElement("gameInformation");
		{
			reader.enterElement("windowPosition");
			{
				info.put(PGameInformation.LEFT, reader.getIntElement("left"));
				info.put(PGameInformation.TOP, reader.getIntElement("top"));
				info.put(PGameInformation.WIDTH, reader.getIntElement("width"));
				info.put(PGameInformation.HEIGHT, reader.getIntElement("height"));
			}
			reader.leaveElement();
			info.put(PGameInformation.ALLOW_RESIZE, reader.getBoolElement("allowResize"));
			info.put(PGameInformation.BACKGROUND_COLOR, Tools.stringToColor(reader.getStringElement("backgroundColor")));
			info.put(PGameInformation.FORM_CAPTION, reader.getStringElement("formCaption"));
			info.put(PGameInformation.MIMIC_GAME_WINDOW, reader.getBoolElement("mimicGameWindow"));
			info.put(PGameInformation.PAUSE_GAME, reader.getBoolElement("pauseGame"));
			info.put(PGameInformation.SHOW_BORDER, reader.getBoolElement("showBorder"));
			info.put(PGameInformation.STAY_ON_TOP, reader.getBoolElement("stayOnTop"));
		}
		reader.leaveElement();
		return info;
	}
}
