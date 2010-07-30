/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import org.lateralgm.resources.GameInformation;

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
				writer.putElement("left", info.left);
				writer.putElement("top", info.top);
				writer.putElement("width", info.width);
				writer.putElement("height", info.height);
			}
			writer.endElement();
			writer.putElement("allowResize", info.allowResize);
			writer.putElement("backgroundColor", Tools.colorToString(info.backgroundColor));
			writer.putElement("formCaption", info.formCaption);
			writer.putElement("mimicGameWindow", info.mimicGameWindow);
			writer.putElement("pauseGame", info.pauseGame);
			writer.putElement("showBorder", info.showBorder);
			writer.putElement("stayOnTop", info.stayOnTop);
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
				info.left = reader.getIntElement("left");
				info.top = reader.getIntElement("top");
				info.width = reader.getIntElement("width");
				info.height = reader.getIntElement("height");
			}
			reader.leaveElement();
			info.allowResize = reader.getBoolElement("allowResize");
			info.backgroundColor = Tools.stringToColor(reader.getStringElement("backgroundColor"));
			info.formCaption = reader.getStringElement("formCaption");
			info.mimicGameWindow = reader.getBoolElement("mimicGameWindow");
			info.pauseGame = reader.getBoolElement("pauseGame");
			info.showBorder = reader.getBoolElement("showBorder");
			info.stayOnTop = reader.getBoolElement("stayOnTop");
		}
		reader.leaveElement();
		return info;
	}
}
