/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * Copyright (C) 2008, 2009 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import org.lateralgm.resources.Background;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Room.PRoom;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.BackgroundDef.PBackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.View;
import org.lateralgm.resources.sub.View.PView;
import org.lateralgm.util.PropertyMap;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredPropertyReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreator;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.GmkSplitter;
import com.ganggarrison.gmdec.Tools;

public class RoomXmlFormat extends XmlFormat<Room> {
	@Override
	public void write(Room room, XmlWriter writer) {
		writer.startElement("room");
		{
			writeIdAttribute(room, writer);
			writer.putElement("caption", room.get(PRoom.CAPTION));
			int width = room.get(PRoom.WIDTH);
			int height = room.get(PRoom.HEIGHT);
			writeDimension(writer, "size", new Dimension(width, height));
			writer.startElement("grid");
			{
				writer.putElement("isometric", room.get(PRoom.ISOMETRIC));
				int snapX = room.get(PRoom.SNAP_X);
				int snapY = room.get(PRoom.SNAP_Y);
				writePoint(writer, "snap", new Point(snapX, snapY));
			}
			writer.endElement();
			writer.putElement("speed", room.get(PRoom.SPEED));
			writer.putElement("persistent", room.get(PRoom.PERSISTENT));
			String creationCode = room.get(PRoom.CREATION_CODE);
			if (GmkSplitter.convertLineEndings) {
				creationCode = Tools.toLf(creationCode);
			}
			writer.putElement("creationCode", creationCode);
			Color backgroundColor = room.get(PRoom.BACKGROUND_COLOR);
			writer.putElement("backgroundColor", Tools.colorToString(backgroundColor));
			writer.putElement("drawBackgroundColor", room.get(PRoom.DRAW_BACKGROUND_COLOR));
			writer.startElement("backgrounds");
			for (BackgroundDef bg : room.backgroundDefs) {
				writeBackgroundDef(writer, bg);
			}
			writer.endElement();
			boolean enableViews = room.get(PRoom.ENABLE_VIEWS);
			writer.putElement("enableViews", enableViews);
			if (enableViews || !GmkSplitter.omitDisabledFields) {
				writer.startElement("views");
				for (View view : room.views) {
					writeView(writer, view);
				}
				writer.endElement();
			}
			writer.startElement("instances");
			InstanceXmlFormat instFormat = new InstanceXmlFormat(room);
			for (Instance instance : room.instances) {
				instFormat.write(instance, writer);
			}
			writer.endElement();
			writer.startElement("tiles");
			for (Tile tile : room.tiles) {
				new TileXmlFormat(room).write(tile, writer);
			}
			writer.endElement();
			writer.startElement("editorSettings");
			{
				boolean rememberEditorSettings = room.get(PRoom.REMEMBER_WINDOW_SIZE);
				writer.putAttribute("remember", rememberEditorSettings);
				if (rememberEditorSettings || !GmkSplitter.omitDisabledFields) {
					int editorWidth = room.get(PRoom.EDITOR_WIDTH);
					int editorHeight = room.get(PRoom.EDITOR_HEIGHT);
					writeDimension(writer, "size", new Dimension(editorWidth, editorHeight));
					writer.putElement("showGrid", room.get(PRoom.SHOW_GRID));
					writer.putElement("showObjects", room.get(PRoom.SHOW_OBJECTS));
					writer.putElement("showTiles", room.get(PRoom.SHOW_TILES));
					writer.putElement("showBackgrounds", room.get(PRoom.SHOW_BACKGROUNDS));
					writer.putElement("showForegrounds", room.get(PRoom.SHOW_FOREGROUNDS));
					writer.putElement("showViews", room.get(PRoom.SHOW_VIEWS));
					writer.putElement("deleteUnderlyingObjects", room.get(PRoom.DELETE_UNDERLYING_OBJECTS));
					writer.putElement("deleteUnderlyingTiles", room.get(PRoom.DELETE_UNDERLYING_TILES));
					writer.putElement("currentTab", room.get(PRoom.CURRENT_TAB));
					writer.putElement("horizontalScrollPosition", room.get(PRoom.SCROLL_BAR_X));
					writer.putElement("verticalScrollPosition", room.get(PRoom.SCROLL_BAR_Y));
				}
			}
			writer.endElement();
		}
		writer.endElement();
	}

	private void writeBackgroundDef(XmlWriter writer, BackgroundDef backgroundDef) {
		writer.startElement("backgroundDef");
		{
			PropertyMap<PBackgroundDef> properties = backgroundDef.properties;
			writer.putElement("visibleOnRoomStart", properties.get(PBackgroundDef.VISIBLE));
			writer.putElement("isForeground", properties.get(PBackgroundDef.FOREGROUND));
			ResourceReference<Background> imageRef = properties.get(PBackgroundDef.BACKGROUND);
			writeResourceRef(writer, "backgroundImage", imageRef);
			int offsetX = properties.get(PBackgroundDef.X);
			int offsetY = properties.get(PBackgroundDef.Y);
			writePoint(writer, "offset", new Point(offsetX, offsetY));
			int speedX = properties.get(PBackgroundDef.H_SPEED);
			int speedY = properties.get(PBackgroundDef.V_SPEED);
			writePoint(writer, "speed", new Point(speedX, speedY));
			writer.putElement("tileHorizontally", properties.get(PBackgroundDef.TILE_HORIZ));
			writer.putElement("tileVertically", properties.get(PBackgroundDef.TILE_VERT));
			writer.putElement("stretch", properties.get(PBackgroundDef.STRETCH));
		}
		writer.endElement();
	}

	private void writeView(XmlWriter writer, View view) {
		writer.startElement("view");
		{
			PropertyMap<PView> properties = view.properties;
			writer.putElement("visibleOnRoomStart", properties.get(PView.VISIBLE));
			writer.startElement("viewInRoom");
			{
				writer.putAttribute("x", properties.get(PView.VIEW_X));
				writer.putAttribute("y", properties.get(PView.VIEW_Y));
				writer.putAttribute("width", properties.get(PView.VIEW_W));
				writer.putAttribute("height", properties.get(PView.VIEW_H));
			}
			writer.endElement();
			writer.startElement("portOnScreen");
			{
				writer.putAttribute("x", properties.get(PView.PORT_X));
				writer.putAttribute("y", properties.get(PView.PORT_Y));
				writer.putAttribute("width", properties.get(PView.PORT_W));
				writer.putAttribute("height", properties.get(PView.PORT_H));
			}
			writer.endElement();
			writer.startElement("objectFollowing");
			{
				ResourceReference<GmObject> objFollowing = properties.get(PView.OBJECT);
				writer.putText(getRefStr(objFollowing));
				writer.putAttribute("hBorder", properties.get(PView.BORDER_H));
				writer.putAttribute("vBorder", properties.get(PView.BORDER_V));
				writer.putAttribute("hSpeed", properties.get(PView.SPEED_H));
				writer.putAttribute("vSpeed", properties.get(PView.SPEED_V));
			}
			writer.endElement();
		}
		writer.endElement();
	}

	@Override
	public Room read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		Room room = new Room();
		reader.enterElement("room");
		{
			readIdAttribute(room, reader);
			room.put(PRoom.CAPTION, reader.getStringElement("caption"));
			Dimension size = readDimension(reader, "size");
			room.put(PRoom.WIDTH, size.width);
			room.put(PRoom.HEIGHT, size.height);
			reader.enterElement("grid");
			{
				room.put(PRoom.ISOMETRIC, reader.getBoolElement("isometric"));
				Point snap = readPoint(reader, "snap");
				room.put(PRoom.SNAP_X, snap.x);
				room.put(PRoom.SNAP_Y, snap.y);
			}
			reader.leaveElement();
			room.put(PRoom.SPEED, reader.getIntElement("speed"));
			room.put(PRoom.PERSISTENT, reader.getBoolElement("persistent"));
			String creationCode = reader.getStringElement("creationCode");
			if (GmkSplitter.convertLineEndings) {
				creationCode = Tools.toCrlf(creationCode);
			}
			room.put(PRoom.CREATION_CODE, creationCode);
			String backgroundColor = reader.getStringElement("backgroundColor");
			room.put(PRoom.BACKGROUND_COLOR, Tools.stringToColor(backgroundColor));
			room.put(PRoom.DRAW_BACKGROUND_COLOR, reader.getBoolElement("drawBackgroundColor"));
			reader.enterElement("backgrounds");
			for (int i = 0; i < room.backgroundDefs.size() && reader.hasNextElement(); i++) {
				readBackgroundDef(reader, room.backgroundDefs.get(i), notifier);
			}
			reader.leaveElement();
			boolean enableViews = reader.getBoolElement("enableViews");
			room.put(PRoom.ENABLE_VIEWS, enableViews);
			if (enableViews || !GmkSplitter.omitDisabledFields) {
				reader.enterElement("views");
				for (int i = 0; i < room.views.size() && reader.hasNextElement(); i++) {
					readView(reader, room.views.get(i), notifier);
				}
				reader.leaveElement();
			}
			reader.enterElement("instances");
			InstanceXmlFormat instFormat = new InstanceXmlFormat(room);
			while (reader.hasNextElement()) {
				Instance instance = instFormat.read(reader, notifier);
				room.instances.add(instance);
			}
			reader.leaveElement();
			reader.enterElement("tiles");
			TileXmlFormat tileFormat = new TileXmlFormat(room);
			while (reader.hasNextElement()) {
				Tile tile = tileFormat.read(reader, notifier);
				room.tiles.add(tile);
			}
			reader.leaveElement();
			reader.enterElement("editorSettings");
			{
				boolean rememberEditorSettings = reader.getBoolAttribute("remember");
				room.put(PRoom.REMEMBER_WINDOW_SIZE, rememberEditorSettings);
				if (rememberEditorSettings || !GmkSplitter.omitDisabledFields) {
					Dimension editorSize = readDimension(reader, "size");
					room.put(PRoom.EDITOR_WIDTH, editorSize.width);
					room.put(PRoom.EDITOR_HEIGHT, editorSize.height);
					room.put(PRoom.SHOW_GRID, reader.getBoolElement("showGrid"));
					room.put(PRoom.SHOW_OBJECTS, reader.getBoolElement("showObjects"));
					room.put(PRoom.SHOW_TILES, reader.getBoolElement("showTiles"));
					room.put(PRoom.SHOW_BACKGROUNDS, reader.getBoolElement("showBackgrounds"));
					room.put(PRoom.SHOW_FOREGROUNDS, reader.getBoolElement("showForegrounds"));
					room.put(PRoom.SHOW_VIEWS, reader.getBoolElement("showViews"));
					room.put(PRoom.DELETE_UNDERLYING_OBJECTS, reader.getBoolElement("deleteUnderlyingObjects"));
					room.put(PRoom.DELETE_UNDERLYING_TILES, reader.getBoolElement("deleteUnderlyingTiles"));
					room.put(PRoom.CURRENT_TAB, reader.getIntElement("currentTab"));
					room.put(PRoom.SCROLL_BAR_X, reader.getIntElement("horizontalScrollPosition"));
					room.put(PRoom.SCROLL_BAR_Y, reader.getIntElement("verticalScrollPosition"));
				}
			}
			reader.leaveElement();
		}
		reader.leaveElement();
		return room;
	}

	private void readBackgroundDef(XmlReader reader, BackgroundDef backgroundDef,
			DeferredReferenceCreatorNotifier notifier) {
		reader.enterElement("backgroundDef");
		{
			PropertyMap<PBackgroundDef> properties = backgroundDef.properties;
			properties.put(PBackgroundDef.VISIBLE, reader.getBoolElement("visibleOnRoomStart"));
			properties.put(PBackgroundDef.FOREGROUND, reader.getBoolElement("isForeground"));
			String backgroundRef = readResourceRef(reader, "backgroundImage");
			DeferredReferenceCreator rc = new DeferredPropertyReferenceCreator<PBackgroundDef>(
					properties, PBackgroundDef.BACKGROUND, Background.class, backgroundRef);
			notifier.addDeferredReferenceCreator(rc);
			Point offset = readPoint(reader, "offset");
			properties.put(PBackgroundDef.X, offset.x);
			properties.put(PBackgroundDef.Y, offset.y);
			Point speed = readPoint(reader, "speed");
			properties.put(PBackgroundDef.H_SPEED, speed.x);
			properties.put(PBackgroundDef.V_SPEED, speed.y);
			properties.put(PBackgroundDef.TILE_HORIZ, reader.getBoolElement("tileHorizontally"));
			properties.put(PBackgroundDef.TILE_VERT, reader.getBoolElement("tileVertically"));
			properties.put(PBackgroundDef.STRETCH, reader.getBoolElement("stretch"));
		}
		reader.leaveElement();
	}

	private void readView(XmlReader reader, View view, DeferredReferenceCreatorNotifier notifier) {
		reader.enterElement("view");
		{
			PropertyMap<PView> properties = view.properties;
			properties.put(PView.VISIBLE, reader.getBoolElement("visibleOnRoomStart"));
			reader.enterElement("viewInRoom");
			{
				properties.put(PView.VIEW_X, reader.getIntAttribute("x"));
				properties.put(PView.VIEW_Y, reader.getIntAttribute("y"));
				properties.put(PView.VIEW_W, reader.getIntAttribute("width"));
				properties.put(PView.VIEW_H, reader.getIntAttribute("height"));
			}
			reader.leaveElement();
			reader.enterElement("portOnScreen");
			{
				properties.put(PView.PORT_X, reader.getIntAttribute("x"));
				properties.put(PView.PORT_Y, reader.getIntAttribute("y"));
				properties.put(PView.PORT_W, reader.getIntAttribute("width"));
				properties.put(PView.PORT_H, reader.getIntAttribute("height"));
			}
			reader.leaveElement();
			reader.enterElement("objectFollowing");
			{
				String objRef = reader.getTextContent();
				if (!objRef.isEmpty()) {
					DeferredReferenceCreator rc = new DeferredPropertyReferenceCreator<PView>(
							properties, PView.OBJECT, GmObject.class, objRef);
					notifier.addDeferredReferenceCreator(rc);
				}
				properties.put(PView.BORDER_H, reader.getIntAttribute("hBorder"));
				properties.put(PView.BORDER_V, reader.getIntAttribute("vBorder"));
				properties.put(PView.SPEED_H, reader.getIntAttribute("hSpeed"));
				properties.put(PView.SPEED_V, reader.getIntAttribute("vSpeed"));
			}
			reader.leaveElement();
		}
		reader.leaveElement();
	}
}
