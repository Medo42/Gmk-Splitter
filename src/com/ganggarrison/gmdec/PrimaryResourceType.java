/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import org.lateralgm.resources.Resource;

import com.ganggarrison.gmdec.files.BackgroundFormat;
import com.ganggarrison.gmdec.files.FontFormat;
import com.ganggarrison.gmdec.files.ObjectFormat;
import com.ganggarrison.gmdec.files.PathFormat;
import com.ganggarrison.gmdec.files.FileTreeFormat;
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
	SPRITES("Sprites", new SpriteFormat(), Resource.Kind.SPRITE),
	SOUNDS("Sounds", new SoundFormat(), Resource.Kind.SOUND),
	BACKGROUNDS("Backgrounds", new BackgroundFormat(), Resource.Kind.BACKGROUND),
	PATHS("Paths", new PathFormat(), Resource.Kind.PATH),
	SCRIPTS("Scripts", new ScriptFormat(), Resource.Kind.SCRIPT),
	FONTS("Fonts", new FontFormat(), Resource.Kind.FONT),
	TIMELINES("Time Lines", new TimelineFormat(), Resource.Kind.TIMELINE),
	OBJECTS("Objects", new ObjectFormat(), Resource.Kind.OBJECT),
	ROOMS("Rooms", new RoomFormat(), Resource.Kind.ROOM);

	public FileTreeFormat<?> format;
	public final String pathName;
	public final Resource.Kind resourceKind;

	private PrimaryResourceType(String pathName, FileTreeFormat<?> format, Resource.Kind resKind) {
		this.format = format;
		this.pathName = pathName;
		this.resourceKind = resKind;
	}
}