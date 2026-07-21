/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.*;
import org.lateralgm.file.GmFile.FormatFlavor;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.sub.Constant;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.gmdec.CommandLineOptions.DuplicateIdType;
import com.ganggarrison.gmdec.CommandLineOptions.ParseException;
import com.ganggarrison.gmdec.files.IncludedFileFormat;
import com.ganggarrison.gmdec.xml.ConstantsXmlFormat;

public class GmkSplitter {
	private static final String CONSTANTS_FILENAME = "Constants.xml";

	public enum IdPreservation {
		NONE, OBJECTS, ALL
	};

	public static boolean convertLineEndings = true;
	public static boolean omitDisabledFields = true;
	public static IdPreservation preserveIds = IdPreservation.OBJECTS;
	public static int targetVersion = 800;
	private static EnumSet<DuplicateIdType> allowedDuplicateIds = EnumSet.noneOf(DuplicateIdType.class);

	public static void main(String[] args) throws IOException {
		int exitCode = run(args);
		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

	static int run(String[] args) throws IOException {
		CommandLineOptions options;
		try {
			options = CommandLineOptions.parse(args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			CommandLineOptions.printUsage(System.err);
			return 2;
		}

		preserveIds = options.getIdPreservation();
		allowedDuplicateIds = options.getAllowedDuplicateIds();
		String source = options.getSource();
		String dest = options.getDestination();

		if (isGmkFile(source)) {
			File gmkFile = new File(source);
			File dir = new File(dest);
			if (!gmkFile.isFile()) {
				System.err.println("Source file " + gmkFile + " not found.");
				return 1;
			}

			if (dir.exists()) {
				System.err.println("Destination directory " + dir + " already exists.");
				return 1;
			}

			decompose(gmkFile, dir);
		} else if (isGmkFile(dest)) {
			File dir = new File(source);
			File gmkFile = new File(dest);
			if (!dir.isDirectory()) {
				System.err.println("Source directory " + dir + " not found.");
				return 1;
			}

			if (gmkFile.exists()) {
				System.err.println("Destination file " + gmkFile + " already exists.");
				return 1;
			}

			compose(dir, gmkFile);
		} else {
			System.err.println("One of <source> or <dest> must be the name of a .gmk or .gm81 file.");
			CommandLineOptions.printUsage(System.err);
			return 2;
		}
		return 0;
	}

	public static boolean areDuplicateIdsAllowed(DuplicateIdType type) {
		return allowedDuplicateIds.contains(type);
	}

	private static boolean isGmkFile(String arg) {
		return arg.toLowerCase().endsWith(".gmk") || arg.toLowerCase().endsWith(".gm81");
	}

	public static void decompose(File sourceGmk, File destinationPath) throws IOException {
		LibManager.autoLoad();
		try {
			ResNode root = new ResNode("Root", (byte) 0, null, null);
			FileInputStream fis = new FileInputStream(sourceGmk);
			GmFile gmf;
			try {
				gmf = GmFileReader.readGmFile(fis, sourceGmk.toURI(), root);
				// Workaround for bug in LateralGM (fixed there in https://github.com/IsmAvatar/LateralGM/commit/c1826a829f1ebc9751015d05c9c15f87aa1488b9)
				// where they never filled the resource references that could not be resolved immediately
				// Can be removed if we ever update the LateralGM dependency
				PostponeRunner.runPostponedRefUpdates();
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
			if (gmf.format != FormatFlavor.GM_800 && gmf.format != FormatFlavor.GM_810) {
				System.err
						.println("Warning: The source file is not of GM version 8 or 8.1. GMK Splitter is *not tested* with this format.");
			}
			targetVersion = gmf.format.getVersion();
			ResourceWriter.writeTree(root, gmf, destinationPath);

			writeConstants(gmf, destinationPath);
			writeIncludedFiles(gmf, destinationPath);
		} catch (GmFormatException e) {
			throw new IOException(e);
		}
	}

	public static void compose(File sourcePath, File destinationGmk) throws IOException {
		LibManager.autoLoad();
		GmFile gmf = new GmFile();
		gmf.uri = destinationGmk.toURI();
		targetVersion = destinationGmk.getName().toLowerCase().endsWith(".gmk") ? 800 : 810;
		ResNode root = new ResNode("Root", (byte) 0, null, null);
		new ResourceReader().readTree(root, gmf, sourcePath);

		readConstants(gmf, sourcePath);
		readIncludedFiles(gmf, sourcePath);

		FileOutputStream fos = new FileOutputStream(destinationGmk);
		try {
			GmFileWriter.writeGmFile(fos, gmf, root, targetVersion);
		} finally {
			fos.close();
		}
	}

	private static void writeConstants(GmFile gmf, File destinationPath) throws IOException {
		File constantsFile = new File(destinationPath, CONSTANTS_FILENAME);
		new ConstantsXmlFormat().write(gmf.constants, constantsFile);
	}

	private static void readConstants(GmFile gmf, File sourcePath) throws IOException {
		File constantsFile = new File(sourcePath, CONSTANTS_FILENAME);
		List<Constant> constants = new ConstantsXmlFormat().read(new XmlReader(constantsFile));
		gmf.constants = new ArrayList<Constant>(constants);
	}

	private static String INCLUDED_FILES_DIR = "Included Files";

	private static void writeIncludedFiles(GmFile gmf, File destinationPath) throws IOException {
		if (!gmf.includes.isEmpty()) {
			File includedFilesPath = new File(destinationPath, INCLUDED_FILES_DIR);
			if (!includedFilesPath.mkdirs()) {
				throw new IOException("Unable to create path: " + includedFilesPath);
			}
			IncludedFileFormat.write(includedFilesPath, gmf.includes);
		}
	}

	private static void readIncludedFiles(GmFile gmf, File sourcePath) throws IOException {
		File includedFilesPath = new File(sourcePath, INCLUDED_FILES_DIR);
		if (includedFilesPath.isDirectory()) {
			IncludedFileFormat.read(includedFilesPath, gmf);
		}
	}

	private static HashSet<String> issuedVersionWarnings = new HashSet<String>();

	public static void issueVersionWarning(String info) {
		if (!issuedVersionWarnings.contains(info)) {
			System.err.println("Warning: The information \"" + info
					+ "\" cannot be represented in the target format.");
			issuedVersionWarnings.add(info);
		}
	}
}
