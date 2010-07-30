/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import org.lateralgm.resources.GameSettings;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.Tools;

public class GameSettingsXmlFormat extends XmlFormat<GameSettings> {
	public static interface LgmConstProvider {
		byte getLgmConst();
	}

	public enum ColorDepth implements LgmConstProvider {
		COLOR_NOCHANGE(GameSettings.COLOR_NOCHANGE),
		COLOR_16(GameSettings.COLOR_16),
		COLOR_32(GameSettings.COLOR_32);

		public final byte gameSettingsConst;

		public byte getLgmConst() {
			return gameSettingsConst;
		}

		private ColorDepth(byte constant) {
			gameSettingsConst = constant;
		}
	}

	public enum Resolution implements LgmConstProvider {
		RES_NOCHANGE(GameSettings.RES_NOCHANGE),
		RES_320X240(GameSettings.RES_320X240),
		RES_640X480(GameSettings.RES_640X480),
		RES_800X600(GameSettings.RES_800X600),
		RES_1024X768(GameSettings.RES_1024X768),
		RES_1280X1024(GameSettings.RES_1280X1024),
		RES_1600X1200(GameSettings.RES_1600X1200);

		public final byte gameSettingsConst;

		public byte getLgmConst() {
			return gameSettingsConst;
		}

		private Resolution(byte constant) {
			gameSettingsConst = constant;
		}
	}

	public enum Frequency implements LgmConstProvider {
		FREQ_NOCHANGE(GameSettings.FREQ_NOCHANGE),
		FREQ_60(GameSettings.FREQ_60),
		FREQ_70(GameSettings.FREQ_70),
		FREQ_85(GameSettings.FREQ_85),
		FREQ_100(GameSettings.FREQ_100),
		FREQ_120(GameSettings.FREQ_120);

		public final byte gameSettingsConst;

		public byte getLgmConst() {
			return gameSettingsConst;
		}

		private Frequency(byte constant) {
			gameSettingsConst = constant;
		}
	}

	public enum Priority implements LgmConstProvider {
		PRIORITY_NORMAL(GameSettings.PRIORITY_NORMAL),
		PRIORITY_HIGH(GameSettings.PRIORITY_HIGH),
		PRIORITY_HIGHEST(GameSettings.PRIORITY_HIGHEST);

		public final byte gameSettingsConst;

		public byte getLgmConst() {
			return gameSettingsConst;
		}

		private Priority(byte constant) {
			gameSettingsConst = constant;
		}
	}

	public enum Loadbar implements LgmConstProvider {
		LOADBAR_NONE(GameSettings.LOADBAR_NONE),
		LOADBAR_DEFAULT(GameSettings.LOADBAR_DEFAULT),
		LOADBAR_CUSTOM(GameSettings.LOADBAR_CUSTOM);

		public final byte gameSettingsConst;

		public byte getLgmConst() {
			return gameSettingsConst;
		}

		private Loadbar(byte constant) {
			gameSettingsConst = constant;
		}
	}

	@Override
	public void write(GameSettings settings, XmlWriter writer) {
		writer.startElement("settings");
		writer.startElement("graphics");
		{
			writer.putElement("scalingPercent", settings.scaling);
			writer.putElement("displayCursor", settings.displayCursor);
			writer.putElement("useVsync", settings.useSynchronization);
			writer.putElement("interpolateColors", settings.interpolate);
			writer.putElement("colorOutsideRoom", Tools.colorToString(settings.colorOutsideRoom));
		}
		writer.endElement();
		writer.startElement("windowing");
		{
			writer.putElement("startFullscreen", settings.startFullscreen);
			writer.putElement("dontDrawBorder", settings.dontDrawBorder);
			writer.putElement("allowWindowResize", settings.allowWindowResize);
			writer.putElement("alwaysOnTop", settings.alwaysOnTop);
			writer.putElement("dontShowButtons", settings.dontShowButtons);
			writer.putElement("switchVideoMode", settings.setResolution);
			if (settings.setResolution || !omitDisabledFields) {
				writer.startElement("videoMode");
				writer.putElement("colorDepth", lgmConstToString(settings.colorDepth, ColorDepth.class));
				writer.putElement("resolution", lgmConstToString(settings.resolution, Resolution.class));
				writer.putElement("frequency", lgmConstToString(settings.frequency, Frequency.class));
				writer.endElement();
			}
		}
		writer.endElement();
		writer.startElement("splashImage");
		{
			writer.putElement("showCustom", settings.showCustomLoadImage);
			writer.putElement("partiallyTransparent", settings.imagePartiallyTransparent);
			writer.putElement("alphaTransparency", settings.loadImageAlpha);
		}
		writer.endElement();
		writer.startElement("progressBar");
		{
			writer.putElement("mode", lgmConstToString(settings.loadBarMode, Loadbar.class));
			writer.putElement("scaleImage", settings.scaleProgressBar);
		}
		writer.endElement();
		writer.startElement("keys");
		{
			writer.putElement("letF1ShowGameInfo", settings.letF1ShowGameInfo);
			writer.putElement("letF4SwitchFullscreen", settings.letF4SwitchFullscreen);
			writer.putElement("letF5SaveF6Load", settings.letF5SaveF6Load);
			writer.putElement("letF9Screenshot", settings.letF9Screenshot);
			writer.putElement("letEscEndGame", settings.letEscEndGame);
			writer.putElement("treatCloseAsEscape", settings.treatCloseAsEscape);
		}
		writer.endElement();
		writer.startElement("errors");
		{
			writer.putElement("displayErrors", settings.displayErrors);
			writer.putElement("writeToLog", settings.writeToLog);
			writer.putElement("abortOnError", settings.abortOnError);
			writer.putElement("treatUninitializedAsZero", settings.treatUninitializedAs0);
		}
		writer.endElement();
		writer.startElement("gameInfo");
		{
			writer.putElement("gameId", settings.gameId);
			writer.putElement("author", settings.author);
			writer.putElement("version", settings.version);
			writer.putElement("information", settings.information);

			writer.putElement("versionMajor", settings.versionMajor);
			writer.putElement("versionMinor", settings.versionMinor);
			writer.putElement("versionRelease", settings.versionRelease);
			writer.putElement("versionBuild", settings.versionBuild);

			writer.putElement("company", settings.company);
			writer.putElement("product", settings.product);
			writer.putElement("copyright", settings.copyright);
			writer.putElement("description", settings.description);
		}
		writer.endElement();
		writer.startElement("system");
		{
			writer.putElement("processPriority", lgmConstToString(settings.gamePriority, Priority.class));
			writer.putElement("disableScreensavers", settings.disableScreensavers);
			writer.putElement("freezeOnLoseFocus", settings.freezeOnLoseFocus);
		}
		writer.endElement();
		writer.startElement("includes");
		{
			writer.putElement("overwriteExisting", settings.overwriteExisting);
			writer.putElement("removeAtGameEnd", settings.removeAtGameEnd);
			writer.putElement("useTempFolder", settings.includeFolder == GameSettings.INCLUDE_TEMP);
		}
		writer.endElement();
		writer.endElement();
	}

	@Override
	public GameSettings read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		GameSettings settings = new GameSettings();
		reader.enterElement("settings");
		reader.enterElement("graphics");
		{
			settings.scaling = reader.getIntElement("scalingPercent");
			settings.displayCursor = reader.getBoolElement("displayCursor");
			settings.useSynchronization = reader.getBoolElement("useVsync");
			settings.interpolate = reader.getBoolElement("interpolateColors");
			settings.colorOutsideRoom = Tools.stringToColor(reader.getStringElement("colorOutsideRoom"));
		}
		reader.leaveElement();
		reader.enterElement("windowing");
		{
			settings.startFullscreen = reader.getBoolElement("startFullscreen");
			settings.dontDrawBorder = reader.getBoolElement("dontDrawBorder");
			settings.allowWindowResize = reader.getBoolElement("allowWindowResize");
			settings.alwaysOnTop = reader.getBoolElement("alwaysOnTop");
			settings.dontShowButtons = reader.getBoolElement("dontShowButtons");
			settings.setResolution = reader.getBoolElement("switchVideoMode");
			if (settings.setResolution || !omitDisabledFields) {
				reader.enterElement("videoMode");
				settings.colorDepth = stringToLgmConst(reader.getStringElement("colorDepth"), ColorDepth.class);
				settings.resolution = stringToLgmConst(reader.getStringElement("resolution"), Resolution.class);
				settings.frequency = stringToLgmConst(reader.getStringElement("frequency"), Frequency.class);
				reader.leaveElement();
			}
		}
		reader.leaveElement();
		reader.enterElement("splashImage");
		{
			settings.showCustomLoadImage = reader.getBoolElement("showCustom");
			settings.imagePartiallyTransparent = reader.getBoolElement("partiallyTransparent");
			settings.loadImageAlpha = reader.getIntElement("alphaTransparency");
		}
		reader.leaveElement();
		reader.enterElement("progressBar");
		{
			settings.loadBarMode = stringToLgmConst(reader.getStringElement("mode"), Loadbar.class);
			settings.scaleProgressBar = reader.getBoolElement("scaleImage");
		}
		reader.leaveElement();
		reader.enterElement("keys");
		{
			settings.letF1ShowGameInfo = reader.getBoolElement("letF1ShowGameInfo");
			settings.letF4SwitchFullscreen = reader.getBoolElement("letF4SwitchFullscreen");
			settings.letF5SaveF6Load = reader.getBoolElement("letF5SaveF6Load");
			settings.letF9Screenshot = reader.getBoolElement("letF9Screenshot");
			settings.letEscEndGame = reader.getBoolElement("letEscEndGame");
			settings.treatCloseAsEscape = reader.getBoolElement("treatCloseAsEscape");
		}
		reader.leaveElement();
		reader.enterElement("errors");
		{
			settings.displayErrors = reader.getBoolElement("displayErrors");
			settings.writeToLog = reader.getBoolElement("writeToLog");
			settings.abortOnError = reader.getBoolElement("abortOnError");
			settings.treatUninitializedAs0 = reader.getBoolElement("treatUninitializedAsZero");
		}
		reader.leaveElement();
		reader.enterElement("gameInfo");
		{
			settings.gameId = reader.getIntElement("gameId");
			settings.author = reader.getStringElement("author");
			settings.version = reader.getStringElement("version");
			settings.information = reader.getStringElement("information");

			settings.versionMajor = reader.getIntElement("versionMajor");
			settings.versionMinor = reader.getIntElement("versionMinor");
			settings.versionRelease = reader.getIntElement("versionRelease");
			settings.versionBuild = reader.getIntElement("versionBuild");

			settings.company = reader.getStringElement("company");
			settings.product = reader.getStringElement("product");
			settings.copyright = reader.getStringElement("copyright");
			settings.description = reader.getStringElement("description");
		}
		reader.leaveElement();
		reader.enterElement("system");
		{
			settings.gamePriority = stringToLgmConst(reader.getStringElement("processPriority"), Priority.class);
			settings.disableScreensavers = reader.getBoolElement("disableScreensavers");
			settings.freezeOnLoseFocus = reader.getBoolElement("freezeOnLoseFocus");
		}
		reader.leaveElement();
		reader.enterElement("includes");
		{
			settings.overwriteExisting = reader.getBoolElement("overwriteExisting");
			settings.removeAtGameEnd = reader.getBoolElement("removeAtGameEnd");
			settings.includeFolder = reader.getBoolElement("useTempFolder")
					? GameSettings.INCLUDE_TEMP
					: GameSettings.INCLUDE_MAIN;
		}
		reader.leaveElement();
		reader.leaveElement();
		return settings;
	}

	public <T extends Enum<? extends LgmConstProvider>> String lgmConstToString(byte lgmConst, Class<T> enumType) {
		for (LgmConstProvider lcp : (LgmConstProvider[]) enumType.getEnumConstants()) {
			if (lcp.getLgmConst() == lgmConst) {
				return lcp.toString();
			}
		}
		throw new IllegalArgumentException();
	}

	public <T extends Enum<? extends LgmConstProvider>> byte stringToLgmConst(String string, Class<T> enumType) {
		return ((LgmConstProvider) Enum.valueOf((Class) enumType, string)).getLgmConst();
	}
}
