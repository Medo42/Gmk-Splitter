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
import java.util.List;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.Resource;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.gmdec.ResourceTreeEntry.Type;
import com.ganggarrison.gmdec.dupes.InstanceAccessor;
import com.ganggarrison.gmdec.dupes.OrderPreservingDupeRemoval;
import com.ganggarrison.gmdec.dupes.TileAccessor;
import com.ganggarrison.gmdec.files.ExtensionsFormat;
import com.ganggarrison.gmdec.files.GameInfoFormat;
import com.ganggarrison.gmdec.files.GameSettingsFormat;
import com.ganggarrison.gmdec.files.ResourceFormat;
import com.ganggarrison.gmdec.xml.ResourceListXmlFormat;

public class ResourceReader {
	private static final PrimaryResourceType[] resTypeReadingOrder = new PrimaryResourceType[] {
			PrimaryResourceType.BACKGROUNDS, PrimaryResourceType.FONTS, PrimaryResourceType.SCRIPTS,
			PrimaryResourceType.SPRITES, PrimaryResourceType.SOUNDS, PrimaryResourceType.OBJECTS,
			PrimaryResourceType.ROOMS, PrimaryResourceType.PATHS, PrimaryResourceType.TIMELINES
	};

	private final EnumMap<PrimaryResourceType, List<Resource<?, ?>>> resources;

	public ResourceReader() {
		resources = new EnumMap<PrimaryResourceType, List<Resource<?, ?>>>(PrimaryResourceType.class);
		for (PrimaryResourceType prt : PrimaryResourceType.values()) {
			resources.put(prt, new ArrayList<Resource<?, ?>>());
		}
	}

	public void readTree(ResNode root, GmFile gmf, File sourcePath) throws IOException {
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
				new SubtreeReader(prt, notifier).readSubtree(primaryNodes.get(prt), subdir);
			}
		}

		GameInfoFormat gameInfoFormat = new GameInfoFormat();
		GameInformation gameInfo = gameInfoFormat.read(sourcePath, null, notifier);
		gameInfoFormat.addResToTree(gameInfo, root);
		gameInfoFormat.addAllResourcesToGmFile(Collections.singletonList(gameInfo), gmf);

		GameSettingsFormat gameSettingsFormat = new GameSettingsFormat();
		GameSettings gameSettings = gameSettingsFormat.read(sourcePath, null, notifier);
		gameSettingsFormat.addResToTree(gameSettings, root);
		gameSettingsFormat.addAllResourcesToGmFile(Collections.singletonList(gameSettings), gmf);

		ExtensionsFormat extensionsFormat = new ExtensionsFormat();
		List<String> extensions = extensionsFormat.read(sourcePath, null, notifier);
		extensionsFormat.addResToTree(extensions, root);
		extensionsFormat.addAllResourcesToGmFile(Collections.singletonList(extensions), gmf);

		for (PrimaryResourceType prt : PrimaryResourceType.values()) {
			addAllResourcesToGmFile(prt.format, resources.get(prt), gmf);
		}

		OrderPreservingDupeRemoval.perform(new TileAccessor(gmf));
		OrderPreservingDupeRemoval.perform(new InstanceAccessor(gmf));

		notifier.createReferences(gmf);
	}

	@SuppressWarnings("unchecked")
	private <T extends Resource<T, ?>> void addAllResourcesToGmFile(ResourceFormat<T> format, List<?> resources,
			GmFile gmf) {
		format.addAllResourcesToGmFile((List<T>) resources, gmf);
	}

	private class SubtreeReader {
		private final PrimaryResourceType prt;
		private final DeferredReferenceCreatorNotifier notifier;

		public SubtreeReader(PrimaryResourceType type, DeferredReferenceCreatorNotifier notifier) {
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
					readResource(dir, rte.name, node, prt);
				}
			}
		}

		private void readResource(File dir, String name, ResNode node, PrimaryResourceType prt) throws IOException {
			Resource<?, ?> resource = readResource(dir, name, node, prt.format);
			resources.get(prt).add(resource);
		}

		private <T extends Resource<T, ?>> T readResource(File dir, String name, ResNode node, ResourceFormat<T> format)
				throws IOException {
			T res = format.read(dir, name, notifier);
			format.addResToTree(res, node);
			return res;
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
		private List<ResourceTreeEntry> readResourceList(File subdir) throws IOException {
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

		private boolean resourcesContainFile(List<ResourceTreeEntry> resources, File resFile) {
			String resName = getResourceName(resFile);
			Type resType = resFile.isDirectory() ? Type.GROUP : Type.RESOURCE;
			for (ResourceTreeEntry rte : resources) {
				if (rte.name.equals(resName) && rte.type == resType) {
					return true;
				}
			}
			return false;
		}

		private String getResourceName(File resFile) {
			String name = resFile.getName();
			String lowName = name.toLowerCase();
			if (resFile.isFile() && (lowName.endsWith(".xml") || lowName.endsWith(".gml"))) {
				name = name.substring(0, name.length() - 4);
			}
			return name;
		}
	}
}
