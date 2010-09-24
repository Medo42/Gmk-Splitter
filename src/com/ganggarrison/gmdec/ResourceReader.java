/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Tile.PTile;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.gmdec.ResourceTreeEntry.Type;
import com.ganggarrison.gmdec.files.ExtensionsFormat;
import com.ganggarrison.gmdec.files.GameInfoFormat;
import com.ganggarrison.gmdec.files.GameSettingsFormat;
import com.ganggarrison.gmdec.files.FileTreeFormat;
import com.ganggarrison.gmdec.xml.ResourceListXmlFormat;

public class ResourceReader {
	private static final PrimaryResourceType[] resTypeReadingOrder = new PrimaryResourceType[] {
			PrimaryResourceType.BACKGROUNDS, PrimaryResourceType.FONTS, PrimaryResourceType.SCRIPTS,
			PrimaryResourceType.SPRITES, PrimaryResourceType.SOUNDS, PrimaryResourceType.OBJECTS,
			PrimaryResourceType.ROOMS, PrimaryResourceType.PATHS, PrimaryResourceType.TIMELINES
	};

	public static void readTree(ResNode root, GmFile gmf, File sourcePath) throws IOException {
		DeferredReferenceCreatorNotifier notifier = new DeferredReferenceCreatorNotifier();
		EnumMap<PrimaryResourceType, ResNode> primaryNodes = new EnumMap<PrimaryResourceType, ResNode>(
				PrimaryResourceType.class);

		for (PrimaryResourceType prt : PrimaryResourceType.values()) {
			ResNode typeNode = root.addChild(prt.pathName, ResNode.STATUS_PRIMARY, prt.resourceKind);
			primaryNodes.put(prt, typeNode);
		}

		for (PrimaryResourceType prt : resTypeReadingOrder) {
			File subdir = new File(sourcePath, prt.pathName);
			if (subdir.isDirectory()) {
				new SubtreeReader(gmf, prt, notifier).readSubtree(primaryNodes.get(prt), subdir);
			}
		}

		GameInformation gameInfo = new GameInfoFormat().read(sourcePath, null, notifier);
		new GameInfoFormat().addResToGmFile(gameInfo, gmf, root);
		GameSettings gameSettings = new GameSettingsFormat().read(sourcePath, null, notifier);
		new GameSettingsFormat().addResToGmFile(gameSettings, gmf, root);
		List<String> extensions = new ExtensionsFormat().read(sourcePath, null, notifier);
		new ExtensionsFormat().addResToGmFile(extensions, gmf, root);

		postprocessTiles(gmf);
		postprocessInstances(gmf);

		notifier.createReferences(gmf);
	}

	private static final int FIRST_TILE_ID = 10000001;
	
	private static void postprocessTiles(GmFile gmf) {
		HashSet<Integer> usedTileNums = new HashSet<Integer>();
		ArrayList<Tile> tilesWithoutId = new ArrayList<Tile>();
		int duplicates = 0;
		
		gmf.lastTileId = FIRST_TILE_ID-1;
		for (Room room : gmf.rooms) {
			for (Tile tile : room.tiles) {
				int id = tile.properties.get(PTile.ID);
				gmf.lastTileId = Math.max(id, gmf.lastTileId);
				if (id < FIRST_TILE_ID) {
					tilesWithoutId.add(tile);
				} else if(usedTileNums.contains(id)) {
					duplicates++;
					tilesWithoutId.add(tile);
				} else {
					usedTileNums.add(id);
				}
			}
		}

		for (Tile tile : tilesWithoutId) {
			tile.properties.put(PTile.ID, ++gmf.lastTileId);
		}

		if (duplicates > 0) {
			System.err.println(duplicates + " duplicate tile IDs have been changed.");
		}
	}

	private static final int FIRST_INSTANCE_ID = 100001;
	
	private static void postprocessInstances(GmFile gmf) {
		HashSet<Integer> usedInstanceNums = new HashSet<Integer>();
		ArrayList<Instance> instancesWithoutId = new ArrayList<Instance>();
		int duplicates = 0;
		
		gmf.lastInstanceId = FIRST_INSTANCE_ID-1;
		for (Room room : gmf.rooms) {
			for (Instance instance : room.instances) {
				int id = instance.properties.get(PInstance.ID);
				gmf.lastInstanceId = Math.max(id, gmf.lastInstanceId);
				if (id < FIRST_INSTANCE_ID) {
					instancesWithoutId.add(instance);
				} else if (usedInstanceNums.contains(id)) {
					duplicates++;
					instancesWithoutId.add(instance);
				} else {
					usedInstanceNums.add(id);
				}
			}
		}

		for (Instance instance : instancesWithoutId) {
			instance.properties.put(PInstance.ID, ++gmf.lastInstanceId);
		}

		if (duplicates > 0) {
			System.err.println(duplicates + " duplicate instance IDs have been changed.");
		}
	}

	private static class SubtreeReader {
		private final GmFile gmFile;
		private final PrimaryResourceType prt;
		private final DeferredReferenceCreatorNotifier notifier;

		public SubtreeReader(GmFile gmf, PrimaryResourceType type, DeferredReferenceCreatorNotifier notifier) {
			this.gmFile = gmf;
			this.prt = type;
			this.notifier = notifier;
		}

		public void readSubtree(ResNode node, File dir)
				throws IOException {
			List<ResourceTreeEntry> resources = readResourceList(dir);
			for (ResourceTreeEntry rte : resources) {
				if (rte.type == Type.GROUP) {
					String childName = rte.name;
					File subdir;
					if (FileTools.isGoodFilename(childName)) {
						subdir = new File(dir, childName);
					} else {
						throw new IOException("Bad resource group name: " + childName);
					}
					ResNode child = node.addChild(childName, ResNode.STATUS_GROUP, node.kind);
					if (!subdir.isDirectory()) {
						throw new IOException("Resource group directory: " + subdir + " not found!");
					}
					readSubtree(child, subdir);
				} else {
					readResource(dir, rte.name, node, prt.format);
				}
			}
		}

		private <T> void readResource(File dir, String name, ResNode node, FileTreeFormat<T> format) throws IOException {
			T res = format.read(dir, name, notifier);
			format.addResToGmFile(res, gmFile, node);
		}

		/**
		 * Read the resource list file and return the list of resources and
		 * groups in this path.
		 * 
		 * For completeness, this function scans the directory for any files and
		 * directories that could be resources or groups but aren't accounted
		 * for. A warning is written to stderr for these. This function could be
		 * changed to actually add additional resources and groups to the end of
		 * the list and remove nonexistant resources from it, for higher
		 * tolerance to manual editing of the resource tree.
		 * 
		 * @param subdir
		 * @return
		 * @throws IOException
		 */
		private static List<ResourceTreeEntry> readResourceList(File subdir) throws IOException {
			File listFile = new File(subdir, "_resources.list.xml");
			if (!listFile.isFile()) {
				System.err.print("Directory " + subdir + " doesn't contain a resource list file. ");
				System.err.println("No resources from this directory or its subdirectories will be processed.");
				return Collections.emptyList();
			}

			List<ResourceTreeEntry> resources = new ResourceListXmlFormat().read(new XmlReader(listFile));

			File[] resFiles = subdir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					String filename = pathname.getName().toLowerCase();
					if (pathname.isDirectory()) {
						return !filename.contains(".");
					} else if (pathname.isFile()) {
						return (filename.endsWith(".xml") || filename.endsWith(".gml"))
								&& !filename.equals("_resources.list.xml");
					} else {
						return false;
					}
				}
			});

			for (File resFile : resFiles) {
				if (!resourcesContainFile(resources, resFile)) {
					System.err.println("Potential resource/group not in list file and won't be processed: " + resFile);
				}
			}

			return resources;
		}

		private static boolean resourcesContainFile(List<ResourceTreeEntry> resources, File resFile) {
			String resName = getResourceName(resFile);
			Type resType = resFile.isDirectory() ? Type.GROUP : Type.RESOURCE;
			for (ResourceTreeEntry rte : resources) {
				if (rte.name.equals(resName) && rte.type == resType) {
					return true;
				}
			}
			return false;
		}

		private static String getResourceName(File resFile) {
			String name = resFile.getName();
			String lowName = name.toLowerCase();
			if (resFile.isFile() && (lowName.endsWith(".xml") || lowName.endsWith(".gml"))) {
				name = name.substring(0, name.length() - 4);
			}
			return name;
		}
	}
}
