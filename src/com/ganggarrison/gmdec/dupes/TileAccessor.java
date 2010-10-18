/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec.dupes;

import java.util.ArrayList;
import java.util.List;

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Tile.PTile;

import com.ganggarrison.gmdec.GmkSplitter;
import com.ganggarrison.gmdec.GmkSplitter.IdPreservation;

public class TileAccessor implements ItemAccessor<Tile> {
	private final GmFile gmFile;

	public TileAccessor(GmFile gmFile) {
		this.gmFile = gmFile;
	}

	@Override
	public List<Tile> getItems() {
		ArrayList<Tile> items = new ArrayList<Tile>();
		for (Room room : gmFile.rooms) {
			for (Tile tile : room.tiles) {
				items.add(tile);
			}
		}
		return items;
	}

	@Override
	public Integer getId(Tile item) {
		Integer id = item.properties.get(PTile.ID);
		if (id == null || id < getFirstValidId()) {
			return null;
		} else {
			return id;
		}
	}

	@Override
	public void setId(Tile item, int id) {
		item.properties.put(PTile.ID, id);
	}

	@Override
	public int getFirstValidId() {
		return 10000001;
	}

	@Override
	public void setMaxId(int id) {
		gmFile.lastTileId = id;
	}

	@Override
	public String getItemName() {
		return "Tile";
	}

	@Override
	public boolean informAboutNewIds() {
		return GmkSplitter.preserveIds == IdPreservation.ALL;
	}
}