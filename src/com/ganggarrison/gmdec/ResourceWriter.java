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

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.file.ResourceList;
import org.lateralgm.resources.Extensions;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Resource;

import com.ganggarrison.gmdec.ResourceTreeEntry.Type;
import com.ganggarrison.gmdec.files.ExtensionsFormat;
import com.ganggarrison.gmdec.files.GameInfoFormat;
import com.ganggarrison.gmdec.files.GameSettingsFormat;
import com.ganggarrison.gmdec.files.FileTreeFormat;
import com.ganggarrison.gmdec.files.ResourceFormat;
import com.ganggarrison.gmdec.xml.ResourceListXmlFormat;

public class ResourceWriter {
	public static void writeTree(ResNode root, GmFile gmf, File startPath) throws IOException {
		for (Class<?> resKind : gmf.resMap.keySet()) {
			if (InstantiableResource.class.isAssignableFrom(resKind)) {
				ResourceList<?> list = gmf.resMap.getList((Class) resKind);
				if (list != null) {
					ResourceFormat.checkDuplicateNames(list, resKind);
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
				if (GameInformation.class.equals(child.kind)) {
					new GameInfoFormat().write(startPath, gmf.gameInfo, gmf);
				} else if (Extensions.class.equals(child.kind)) {
					new ExtensionsFormat().write(startPath, gmf.packages, gmf);
				} else if (GameSettings.class.equals(child.kind)) {
					new GameSettingsFormat().write(startPath, gmf.gameSettings, gmf);
				} else {
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
			if (child.status != ResNode.STATUS_SECONDARY) {
				String childName = (String) child.getUserObject();
				String filename = FileTools.replaceBadChars(childName);
				File subPath = new File(path, filename);
				writeTreeRecursive(child, subPath, type, gmf);
				groupResList.add(new ResourceTreeEntry(childName, filename, Type.GROUP));
			} else {
				if (child.getRes() != null) {
					FileTreeFormat format = type.format;
					Resource<?, ?> resource = child.getRes().get();
					format.write(path, resource, gmf);
					groupResList.add(format.createResourceTreeEntry(resource));
				} else {
					System.err.println("Ressource without reference in tree: " + child.getUserObject());
				}
			}
		}
		new ResourceListXmlFormat().write(groupResList, new File(path, "_resources.list.xml"));
	}
}
