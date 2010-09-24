/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.file.ResourceList;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Resource.Kind;

import com.ganggarrison.gmdec.ResourceTreeEntry.Type;
import com.ganggarrison.gmdec.files.ExtensionsFormat;
import com.ganggarrison.gmdec.files.GameInfoFormat;
import com.ganggarrison.gmdec.files.GameSettingsFormat;
import com.ganggarrison.gmdec.files.FileTreeFormat;
import com.ganggarrison.gmdec.xml.ResourceListXmlFormat;

public class ResourceWriter {
	public static void writeTree(ResNode root, GmFile gmf, File startPath) throws IOException {
		for (Kind resKind : Kind.values()) {
			ResourceList<?> list = gmf.getList(resKind);
			if (list != null) {
				HashSet<String> names = new HashSet<String>();
				for (Resource<?, ?> res : list) {
					if (names.contains(res.getName())) {
						throw new IOException("Duplicate " + resKind + " name " + res.getName());
					}
				}
			}
		}
		if (startPath.exists()) {
			throw new IOException("Output folder already exists! Overwriting is not supported.");
		}
		int numChilds = root.getChildCount();
		for (int i = 0; i < numChilds; i++) {
			ResNode child = (ResNode) root.getChildAt(i);
			if (child.status == ResNode.STATUS_PRIMARY) {
				writeChildTree(startPath, child, gmf);
			} else if (child.status == ResNode.STATUS_SECONDARY) {
				switch (child.kind) {
				case GAMEINFO:
					new GameInfoFormat().write(startPath, gmf.gameInfo, gmf);
					break;
				case EXTENSIONS:
					new ExtensionsFormat().write(startPath, gmf.packages, gmf);
					break;
				case GAMESETTINGS:
					new GameSettingsFormat().write(startPath, gmf.gameSettings, gmf);
					break;
				default:
					throw new IOException("Unexpected secondary resource kind " + child.kind
							+ " in first level of the tree.");
				}
			} else {
				throw new IOException("Unexpected resource group \"" + child.getUserObject()
						+ "\" in resource tree root.");
			}
		}

	}

	// TODO: create a subclass like in the Reader case to reduce parameter count
	private static void writeChildTree(File startPath, ResNode child, GmFile gmf) throws IOException {
		for (PrimaryResourceType type : PrimaryResourceType.values()) {
			if (type.pathName.equals(child.getUserObject())) {
				writeTreeRecursive(child, new File(startPath, type.pathName), type, gmf);
				return;
			}
		}
		throw new IOException("Unknown primary resource group \"" + child.getUserObject() + "\"");
	}

	private static void writeTreeRecursive(ResNode node, File path, PrimaryResourceType type, GmFile gmf)
			throws IOException {
		if (!path.mkdirs()) {
			throw new IOException("Duplicate resource group: \"" + node.getUserObject() + "\"");
		}
		Enumeration<ResNode> children = node.children();
		ArrayList<ResourceTreeEntry> groupResList = new ArrayList<ResourceTreeEntry>();
		while (children.hasMoreElements()) {
			ResNode child = children.nextElement();
			String childName = (String) child.getUserObject();
			if (child.status != ResNode.STATUS_SECONDARY) {
				File subPath;
				if (FileTools.isGoodFilename(childName)) {
					subPath = new File(path, childName);
				} else {
					throw new IOException("Bad resource group name: " + childName);
				}
				writeTreeRecursive(child, subPath, type, gmf);
				groupResList.add(new ResourceTreeEntry(childName, Type.GROUP));
			} else {
				if (child.getRes() != null) {
					FileTreeFormat format = type.format;
					Resource<?, ?> resource = child.getRes().get();
					format.write(path, resource, gmf);
					groupResList.add(new ResourceTreeEntry(resource.getName(), Type.RESOURCE));
				} else {
					System.err.println("Ressource without reference in tree: " + child.getUserObject());
				}
			}
		}
		new ResourceListXmlFormat().write(groupResList, new File(path, "_resources.list.xml"));
	}
}
