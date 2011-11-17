/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import org.lateralgm.resources.Background;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;

import com.ganggarrison.gmdec.files.BackgroundFormat;
import com.ganggarrison.gmdec.files.FontFormat;
import com.ganggarrison.gmdec.files.ObjectFormat;
import com.ganggarrison.gmdec.files.PathFormat;
import com.ganggarrison.gmdec.files.ResourceFormat;
import com.ganggarrison.gmdec.files.RoomFormat;
import com.ganggarrison.gmdec.files.ScriptFormat;
import com.ganggarrison.gmdec.files.SoundFormat;
import com.ganggarrison.gmdec.files.SpriteFormat;
import com.ganggarrison.gmdec.files.TimelineFormat;

/**
 * This enum lists all "primary" resource types, that is, the main resource
 * directories in the Game Maker resource tree. They are ordered in the same
 * order as they appear in normal GMK files.
 */
public enum PrimaryResourceType {
	SPRITES("Sprites", new SpriteFormat(), Sprite.class),
	SOUNDS("Sounds", new SoundFormat(), Sound.class),
	BACKGROUNDS("Backgrounds", new BackgroundFormat(), Background.class),
	PATHS("Paths", new PathFormat(), Path.class),
	SCRIPTS("Scripts", new ScriptFormat(), Script.class),
	FONTS("Fonts", new FontFormat(), Font.class),
	TIMELINES("Time Lines", new TimelineFormat(), Timeline.class),
	OBJECTS("Objects", new ObjectFormat(), GmObject.class),
	ROOMS("Rooms", new RoomFormat(), Room.class);

	public ResourceFormat<?> format;
	public final String pathName;
	public final Class<? extends InstantiableResource<?, ?>> resourceKind;

	private <T extends InstantiableResource<T, ?>> PrimaryResourceType(String pathName, ResourceFormat<?> format,
			Class<T> resKind) {
		this.format = format;
		this.pathName = pathName;
		this.resourceKind = resKind;
	}
}