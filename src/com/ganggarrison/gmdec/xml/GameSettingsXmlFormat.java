/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GameSettings.ColorDepth;
import org.lateralgm.resources.GameSettings.Frequency;
import org.lateralgm.resources.GameSettings.IncludeFolder;
import org.lateralgm.resources.GameSettings.PGameSettings;
import org.lateralgm.resources.GameSettings.Priority;
import org.lateralgm.resources.GameSettings.ProgressBar;
import org.lateralgm.resources.GameSettings.Resolution;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.GmkSplitter;
import com.ganggarrison.gmdec.Tools;

public class GameSettingsXmlFormat extends XmlFormat<GameSettings> {
	public static interface MappedEnum<T extends Enum<T>> {
		T getMapped();
	}

	private static final EnumMap<ColorDepth, String> colorDepthStrings = new EnumMap<ColorDepth, String>(
			GameSettings.ColorDepth.class);
	private static final EnumMap<Resolution, String> resolutionStrings = new EnumMap<Resolution, String>(
			Resolution.class);
	private static final EnumMap<Frequency, String> frequencyStrings = new EnumMap<Frequency, String>(
			Frequency.class);
	private static final EnumMap<Priority, String> priorityStrings = new EnumMap<Priority, String>(
			Priority.class);
	private static final EnumMap<ProgressBar, String> progressBarStrings = new EnumMap<GameSettings.ProgressBar, String>(
			ProgressBar.class);

	static {
		colorDepthStrings.put(ColorDepth.NO_CHANGE, "COLOR_NOCHANGE");
		colorDepthStrings.put(ColorDepth.BIT_16, "COLOR_16");
		colorDepthStrings.put(ColorDepth.BIT_32, "COLOR_32");

		resolutionStrings.put(Resolution.NO_CHANGE, "RES_NOCHANGE");
		resolutionStrings.put(Resolution.RES_320X240, "RES_320X240");
		resolutionStrings.put(Resolution.RES_640X480, "RES_640X480");
		resolutionStrings.put(Resolution.RES_800X600, "RES_800X600");
		resolutionStrings.put(Resolution.RES_1024X768, "RES_1024X768");
		resolutionStrings.put(Resolution.RES_1280X1024, "RES_1280X1024");
		resolutionStrings.put(Resolution.RES_1600X1200, "RES_1600X1200");

		frequencyStrings.put(Frequency.NO_CHANGE, "FREQ_NOCHANGE");
		frequencyStrings.put(Frequency.FREQ_60, "FREQ_60");
		frequencyStrings.put(Frequency.FREQ_70, "FREQ_70");
		frequencyStrings.put(Frequency.FREQ_85, "FREQ_85");
		frequencyStrings.put(Frequency.FREQ_100, "FREQ_100");
		frequencyStrings.put(Frequency.FREQ_120, "FREQ_120");

		priorityStrings.put(Priority.NORMAL, "PRIORITY_NORMAL");
		priorityStrings.put(Priority.HIGH, "PRIORITY_HIGH");
		priorityStrings.put(Priority.HIGHEST, "PRIORITY_HIGHEST");

		progressBarStrings.put(ProgressBar.NONE, "LOADBAR_NONE");
		progressBarStrings.put(ProgressBar.DEFAULT, "LOADBAR_DEFAULT");
		progressBarStrings.put(ProgressBar.CUSTOM, "LOADBAR_CUSTOM");
	}

	@Override
	public void write(GameSettings settings, XmlWriter writer) {
		writer.startElement("settings");
		writer.startElement("graphics");
		{
			writer.putElement("scalingPercent", settings.get(PGameSettings.SCALING));
			writer.putElement("displayCursor", settings.get(PGameSettings.DISPLAY_CURSOR));
			writer.putElement("useVsync", settings.get(PGameSettings.USE_SYNCHRONIZATION));
			writer.putElement("interpolateColors", settings.get(PGameSettings.INTERPOLATE));
			writer.putElement("colorOutsideRoom",
					Tools.colorToString((Color) settings.get(PGameSettings.COLOR_OUTSIDE_ROOM)));
		}
		writer.endElement();
		writer.startElement("windowing");
		{
			writer.putElement("startFullscreen", settings.get(PGameSettings.START_FULLSCREEN));
			writer.putElement("dontDrawBorder", settings.get(PGameSettings.DONT_DRAW_BORDER));
			writer.putElement("allowWindowResize", settings.get(PGameSettings.ALLOW_WINDOW_RESIZE));
			writer.putElement("alwaysOnTop", settings.get(PGameSettings.ALWAYS_ON_TOP));
			writer.putElement("dontShowButtons", settings.get(PGameSettings.DONT_SHOW_BUTTONS));
			writer.putElement("switchVideoMode", settings.get(PGameSettings.SET_RESOLUTION));
			if ((Boolean) settings.get(PGameSettings.SET_RESOLUTION) || !GmkSplitter.omitDisabledFields) {
				writer.startElement("videoMode");
				writer.putElement("colorDepth", colorDepthStrings.get(settings.get(PGameSettings.COLOR_DEPTH)));
				writer.putElement("resolution", resolutionStrings.get(settings.get(PGameSettings.RESOLUTION)));
				writer.putElement("frequency", frequencyStrings.get(settings.get(PGameSettings.FREQUENCY)));
				writer.endElement();
			}
		}
		writer.endElement();
		writer.startElement("splashImage");
		{
			writer.putElement("showCustom", settings.get(PGameSettings.SHOW_CUSTOM_LOAD_IMAGE));
			writer.putElement("partiallyTransparent", settings.get(PGameSettings.IMAGE_PARTIALLY_TRANSPARENTY));
			writer.putElement("alphaTransparency", settings.get(PGameSettings.LOAD_IMAGE_ALPHA));
		}
		writer.endElement();
		writer.startElement("progressBar");
		{
			writer.putElement("mode", progressBarStrings.get(settings.get(PGameSettings.LOAD_BAR_MODE)));
			writer.putElement("scaleImage", settings.get(PGameSettings.SCALE_PROGRESS_BAR));
		}
		writer.endElement();
		writer.startElement("keys");
		{
			writer.putElement("letF1ShowGameInfo", settings.get(PGameSettings.LET_F1_SHOW_GAME_INFO));
			writer.putElement("letF4SwitchFullscreen", settings.get(PGameSettings.LET_F4_SWITCH_FULLSCREEN));
			writer.putElement("letF5SaveF6Load", settings.get(PGameSettings.LET_F5_SAVE_F6_LOAD));
			writer.putElement("letF9Screenshot", settings.get(PGameSettings.LET_F9_SCREENSHOT));
			writer.putElement("letEscEndGame", settings.get(PGameSettings.LET_ESC_END_GAME));
			writer.putElement("treatCloseAsEscape", settings.get(PGameSettings.TREAT_CLOSE_AS_ESCAPE));
		}
		writer.endElement();
		writer.startElement("errors");
		{
			writer.putElement("displayErrors", settings.get(PGameSettings.DISPLAY_ERRORS));
			writer.putElement("writeToLog", settings.get(PGameSettings.WRITE_TO_LOG));
			writer.putElement("abortOnError", settings.get(PGameSettings.ABORT_ON_ERROR));
			writer.putElement("treatUninitializedAsZero", settings.get(PGameSettings.TREAT_UNINIT_AS_0));
			if (GmkSplitter.targetVersion >= 810) {
				writer.putElement("checkScriptArgumentCount", settings.get(PGameSettings.ERROR_ON_ARGS));
			}
		}
		writer.endElement();
		writer.startElement("gameInfo");
		{
			writer.putElement("gameId", settings.get(PGameSettings.GAME_ID));
			writer.putElement("author", settings.get(PGameSettings.AUTHOR));
			writer.putElement("version", settings.get(PGameSettings.VERSION));
			writer.putElement("information", settings.get(PGameSettings.INFORMATION));

			writer.putElement("versionMajor", settings.get(PGameSettings.VERSION_MAJOR));
			writer.putElement("versionMinor", settings.get(PGameSettings.VERSION_MINOR));
			writer.putElement("versionRelease", settings.get(PGameSettings.VERSION_RELEASE));
			writer.putElement("versionBuild", settings.get(PGameSettings.VERSION_BUILD));

			writer.putElement("company", settings.get(PGameSettings.COMPANY));
			writer.putElement("product", settings.get(PGameSettings.PRODUCT));
			writer.putElement("copyright", settings.get(PGameSettings.COPYRIGHT));
			writer.putElement("description", settings.get(PGameSettings.DESCRIPTION));

			byte[] binDplayGuid = settings.get(PGameSettings.DPLAY_GUID);
			StringBuilder directPlayGuid = new StringBuilder();
			for (int i = 0; i < 16; i++) {
				String hex = Integer.toHexString(binDplayGuid[i] & 0xff);
				if (hex.length() == 1) {
					directPlayGuid.append('0');
				}
				directPlayGuid.append(hex);
			}
			writer.putElement("directPlayGuid", directPlayGuid);
		}
		writer.endElement();
		writer.startElement("system");
		{
			writer.putElement("processPriority", priorityStrings.get(settings.get(PGameSettings.GAME_PRIORITY)));
			writer.putElement("disableScreensavers", settings.get(PGameSettings.DISABLE_SCREENSAVERS));
			writer.putElement("freezeOnLoseFocus", settings.get(PGameSettings.FREEZE_ON_LOSE_FOCUS));
		}
		writer.endElement();
		writer.startElement("includes");
		{
			writer.putElement("overwriteExisting", settings.get(PGameSettings.OVERWRITE_EXISTING));
			writer.putElement("removeAtGameEnd", settings.get(PGameSettings.REMOVE_AT_GAME_END));
			writer.putElement("useTempFolder",
					settings.get(PGameSettings.INCLUDE_FOLDER) == GameSettings.IncludeFolder.TEMP);
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
			settings.put(PGameSettings.SCALING, reader.getIntElement("scalingPercent"));
			settings.put(PGameSettings.DISPLAY_CURSOR, reader.getBoolElement("displayCursor"));
			settings.put(PGameSettings.USE_SYNCHRONIZATION, reader.getBoolElement("useVsync"));
			settings.put(PGameSettings.INTERPOLATE, reader.getBoolElement("interpolateColors"));
			settings.put(PGameSettings.COLOR_OUTSIDE_ROOM,
					Tools.stringToColor(reader.getStringElement("colorOutsideRoom")));
		}
		reader.leaveElement();
		reader.enterElement("windowing");
		{
			settings.put(PGameSettings.START_FULLSCREEN, reader.getBoolElement("startFullscreen"));
			settings.put(PGameSettings.DONT_DRAW_BORDER, reader.getBoolElement("dontDrawBorder"));
			settings.put(PGameSettings.ALLOW_WINDOW_RESIZE, reader.getBoolElement("allowWindowResize"));
			settings.put(PGameSettings.ALWAYS_ON_TOP, reader.getBoolElement("alwaysOnTop"));
			settings.put(PGameSettings.DONT_SHOW_BUTTONS, reader.getBoolElement("dontShowButtons"));
			settings.put(PGameSettings.SET_RESOLUTION, reader.getBoolElement("switchVideoMode"));
			if ((Boolean) settings.get(PGameSettings.SET_RESOLUTION) || !GmkSplitter.omitDisabledFields) {
				reader.enterElement("videoMode");
				settings.put(PGameSettings.COLOR_DEPTH,
						lookupReverse(colorDepthStrings, reader.getStringElement("colorDepth")));
				settings.put(PGameSettings.RESOLUTION,
						lookupReverse(resolutionStrings, reader.getStringElement("resolution")));
				settings.put(PGameSettings.FREQUENCY,
						lookupReverse(frequencyStrings, reader.getStringElement("frequency")));
				reader.leaveElement();
			}
		}
		reader.leaveElement();
		reader.enterElement("splashImage");
		{
			settings.put(PGameSettings.SHOW_CUSTOM_LOAD_IMAGE, reader.getBoolElement("showCustom"));
			settings.put(PGameSettings.IMAGE_PARTIALLY_TRANSPARENTY, reader.getBoolElement("partiallyTransparent"));
			settings.put(PGameSettings.LOAD_IMAGE_ALPHA, reader.getIntElement("alphaTransparency"));
		}
		reader.leaveElement();
		reader.enterElement("progressBar");
		{
			settings.put(PGameSettings.LOAD_BAR_MODE,
					lookupReverse(progressBarStrings, reader.getStringElement("mode")));
			settings.put(PGameSettings.SCALE_PROGRESS_BAR, reader.getBoolElement("scaleImage"));
		}
		reader.leaveElement();
		reader.enterElement("keys");
		{
			settings.put(PGameSettings.LET_F1_SHOW_GAME_INFO, reader.getBoolElement("letF1ShowGameInfo"));
			settings.put(PGameSettings.LET_F4_SWITCH_FULLSCREEN, reader.getBoolElement("letF4SwitchFullscreen"));
			settings.put(PGameSettings.LET_F5_SAVE_F6_LOAD, reader.getBoolElement("letF5SaveF6Load"));
			settings.put(PGameSettings.LET_F9_SCREENSHOT, reader.getBoolElement("letF9Screenshot"));
			settings.put(PGameSettings.LET_ESC_END_GAME, reader.getBoolElement("letEscEndGame"));
			settings.put(PGameSettings.TREAT_CLOSE_AS_ESCAPE, reader.getBoolElement("treatCloseAsEscape"));
		}
		reader.leaveElement();
		reader.enterElement("errors");
		{
			settings.put(PGameSettings.DISPLAY_ERRORS, reader.getBoolElement("displayErrors"));
			settings.put(PGameSettings.WRITE_TO_LOG, reader.getBoolElement("writeToLog"));
			settings.put(PGameSettings.ABORT_ON_ERROR, reader.getBoolElement("abortOnError"));
			settings.put(PGameSettings.TREAT_UNINIT_AS_0, reader.getBoolElement("treatUninitializedAsZero"));
			if (reader.hasNextElement()) {
				settings.put(PGameSettings.ERROR_ON_ARGS, reader.getBoolElement("checkScriptArgumentCount"));
				if (GmkSplitter.targetVersion < 810) {
					GmkSplitter.issueVersionWarning("GameSettings/checkScriptArgumentCount");
				}
			} else {
				/*
				 * The default is true, but GM sets it to false when converting
				 * from older formats, so we do that too.
				 */
				settings.put(PGameSettings.ERROR_ON_ARGS, false);
			}
		}
		reader.leaveElement();
		reader.enterElement("gameInfo");
		{
			settings.put(PGameSettings.GAME_ID, reader.getIntElement("gameId"));
			settings.put(PGameSettings.AUTHOR, reader.getStringElement("author"));
			settings.put(PGameSettings.VERSION, reader.getStringElement("version"));
			settings.put(PGameSettings.INFORMATION, reader.getStringElement("information"));

			settings.put(PGameSettings.VERSION_MAJOR, reader.getIntElement("versionMajor"));
			settings.put(PGameSettings.VERSION_MINOR, reader.getIntElement("versionMinor"));
			settings.put(PGameSettings.VERSION_RELEASE, reader.getIntElement("versionRelease"));
			settings.put(PGameSettings.VERSION_BUILD, reader.getIntElement("versionBuild"));

			settings.put(PGameSettings.COMPANY, reader.getStringElement("company"));
			settings.put(PGameSettings.PRODUCT, reader.getStringElement("product"));
			settings.put(PGameSettings.COPYRIGHT, reader.getStringElement("copyright"));
			settings.put(PGameSettings.DESCRIPTION, reader.getStringElement("description"));

			byte[] binDplayGuid = new byte[16];
			if (reader.hasNextElement()) {
				String directPlayGuid = reader.getStringElement("directPlayGuid");
				for (int i = 0; i < 16; i++) {
					String hexByte = directPlayGuid.substring(i * 2, i * 2 + 2);
					binDplayGuid[i] = (byte) Integer.parseInt(hexByte, 16);
				}
			}
			settings.put(PGameSettings.DPLAY_GUID, binDplayGuid);
		}
		reader.leaveElement();
		reader.enterElement("system");
		{
			settings.put(PGameSettings.GAME_PRIORITY, lookupReverse(priorityStrings, reader.getStringElement("processPriority")));
			settings.put(PGameSettings.DISABLE_SCREENSAVERS, reader.getBoolElement("disableScreensavers"));
			settings.put(PGameSettings.FREEZE_ON_LOSE_FOCUS, reader.getBoolElement("freezeOnLoseFocus"));
		}
		reader.leaveElement();
		reader.enterElement("includes");
		{
			settings.put(PGameSettings.OVERWRITE_EXISTING, reader.getBoolElement("overwriteExisting"));
			settings.put(PGameSettings.REMOVE_AT_GAME_END, reader.getBoolElement("removeAtGameEnd"));
			settings.put(PGameSettings.INCLUDE_FOLDER, reader.getBoolElement("useTempFolder")
					? IncludeFolder.TEMP
					: IncludeFolder.MAIN);
		}
		reader.leaveElement();
		reader.leaveElement();
		return settings;
	}

	private <T, U> T lookupReverse(Map<T, U> map, U value) {
		for (Entry<T, U> entry : map.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		return null;
	}
}
