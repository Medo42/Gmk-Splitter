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
import java.nio.charset.Charset;
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
import com.ganggarrison.gmdec.xml.MetadataXmlFormat;

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

			decompose(gmkFile, dir, options.getCharset());
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

			compose(dir, gmkFile, options.getCharset());
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
		decompose(sourceGmk, destinationPath, null);
	}

	/**
	 * @param gmkCharset The charset to use for the text of .gmk (pre-8.1) files, or
	 *        null to use the code page of the current system. Ignored for .gm81 files.
	 */
	public static void decompose(File sourceGmk, File destinationPath, Charset gmkCharset) throws IOException {
		LibManager.autoLoad();
		if (gmkCharset == null) {
			gmkCharset = getSystemCharset();
		}
		try {
			ResNode root = new ResNode("Root", (byte) 0, null, null);
			FileInputStream fis = new FileInputStream(sourceGmk);
			GmFile gmf;
			CheckedGmStreamDecoder in = new CheckedGmStreamDecoder(fis);
			try {
				gmf = GmFileReader.readGmFile(in, sourceGmk.toURI(), root, gmkCharset);
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
			// Get actually used charset - depending on the file's version, the decoder may have selected UTF-8
			Charset usedCharset = in.getCharset();
			if (targetVersion < 810) {
				System.out.println("Text in the source file is decoded using charset " + usedCharset.name() + ".");
			}
			warnAboutDecodingProblems(in.getProblems(), usedCharset, targetVersion);

			ResourceWriter.writeTree(root, gmf, destinationPath);

			writeConstants(gmf, destinationPath);
			writeIncludedFiles(gmf, destinationPath);

			TreeMetadata metadata = new TreeMetadata();
			if (targetVersion < 810) {
				metadata.charset = usedCharset;
			}
			writeMetadata(metadata, destinationPath);
		} catch (GmFormatException e) {
			throw new IOException(e);
		}
	}

	public static void compose(File sourcePath, File destinationGmk) throws IOException {
		compose(sourcePath, destinationGmk, null);
	}

	/**
	 * @param gmkCharset The charset to use for the text of a .gmk (8.0) file, or null
	 *        to use the charset stored in the tree's metadata, falling back to the
	 *        code page of the current system. Ignored for .gm81 files.
	 */
	public static void compose(File sourcePath, File destinationGmk, Charset gmkCharset) throws IOException {
		LibManager.autoLoad();
		GmFile gmf = new GmFile();
		gmf.uri = destinationGmk.toURI();
		targetVersion = destinationGmk.getName().toLowerCase().endsWith(".gmk") ? 800 : 810;
		ResNode root = new ResNode("Root", (byte) 0, null, null);

		boolean charsetGivenExplicitly = gmkCharset != null;
		TreeMetadata metadata = readMetadata(sourcePath);
		if (gmkCharset == null) {
			gmkCharset = metadata.charset;
		}
		if (gmkCharset == null) {
			gmkCharset = getSystemCharset();
		}

		new ResourceReader().readTree(root, gmf, sourcePath);

		readConstants(gmf, sourcePath);
		readIncludedFiles(gmf, sourcePath);

		FileOutputStream fos = new FileOutputStream(destinationGmk);
		CheckedGmStreamEncoder out = new CheckedGmStreamEncoder(fos);
		try {
			GmFileWriter.writeGmFile(out, gmf, root, targetVersion, gmkCharset);
		} finally {
			fos.close();
		}

		if (targetVersion < 810) {
			Charset usedCharset = out.getCharset();
			System.out.println("Text in the destination file is encoded using charset " + usedCharset.name() + ".");
			warnAboutEncodingProblems(out.getProblems(), usedCharset, charsetGivenExplicitly);
		}
	}

	/**
	 * The charset Game Maker 8.0 would use for text on this system: the system
	 * code page. Java >= 17 reports it in the "native.encoding" property. Before
	 * Java 18 it is also what Charset.defaultCharset() returns, so we can fall
	 * back to that on older Java versions.
	 */
	private static Charset getSystemCharset() {
		String nativeEncoding = System.getProperty("native.encoding");
		if (nativeEncoding != null) {
			try {
				return Charset.forName(nativeEncoding);
			} catch (IllegalArgumentException e) {
				// Fall through to the default charset
			}
		}
		return Charset.defaultCharset();
	}

	private static void warnAboutDecodingProblems(CharsetProblems problems, Charset charset, int version) {
		if (problems.getProblemStringCount() == 0) {
			return;
		}
		System.err.println("Warning: " + problems.getProblemStringCount() + " of the "
				+ problems.getNonAsciiStringCount() + " strings with non-ASCII characters in the source file "
				+ "could not be decoded properly using charset " + charset.name() + ".");
		System.err.println("         Examples: " + formatExamples(problems));
		if (version < 810) {
			System.err.println("         The file was probably created on a system with a different code page.");
			System.err.println("         Use the --charset option to specify the correct one, e.g. --charset windows-1252 or --charset MS949.");
			Charset systemCharset = getSystemCharset();
			if (!systemCharset.equals(charset)) {
				System.err.println("         The code page of this system is " + systemCharset.name() + ".");
			}
		} else {
			System.err.println("         GM 8.1 files should always contain UTF-8 text. The file may be damaged.");
		}
	}

	private static void warnAboutEncodingProblems(CharsetProblems problems, Charset charset,
			boolean charsetGivenExplicitly) {
		if (problems.getProblemStringCount() > 0) {
			System.err.println("Warning: " + problems.getProblemStringCount() + " of the "
					+ problems.getNonAsciiStringCount() + " strings with non-ASCII characters contain characters "
					+ "that cannot be represented in charset " + charset.name() + ".");
			System.err.println("         Those characters have been replaced. Examples: " + formatExamples(problems));
			System.err.println("         Use the --charset option to select a charset containing all required characters.");
		} else if (!charsetGivenExplicitly && problems.getNonAsciiStringCount() > 0
				&& charset.equals(Charset.forName("UTF-8"))) {
			System.err.println("Warning: The created .gmk file contains non-ASCII text encoded as UTF-8.");
			System.err.println("         Game Maker 8.0 interprets text using the Windows ANSI code page of the system it runs on,");
			System.err.println("         so this text will be displayed incorrectly unless that code page is UTF-8.");
			System.err.println("         Use the --charset option to select the code page of the target system, e.g. --charset windows-1252 or --charset MS949.");
		}
	}

	private static String formatExamples(CharsetProblems problems) {
		StringBuilder sb = new StringBuilder();
		for (String example : problems.getExamples()) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append('"').append(example).append('"');
		}
		return sb.toString();
	}

	private static void writeMetadata(TreeMetadata metadata, File destinationPath) throws IOException {
		File metadataFile = new File(destinationPath, TreeMetadata.FILENAME);
		new MetadataXmlFormat().write(metadata, metadataFile);
	}

	/**
	 * Read the tree metadata. Trees created by older versions of the tool have no
	 * metadata file, in which case empty metadata is returned.
	 */
	private static TreeMetadata readMetadata(File sourcePath) throws IOException {
		File metadataFile = new File(sourcePath, TreeMetadata.FILENAME);
		if (!metadataFile.isFile()) {
			return new TreeMetadata();
		}
		try {
			return new MetadataXmlFormat().read(new XmlReader(metadataFile));
		} catch (IllegalArgumentException e) {
			throw new IOException("Error reading " + metadataFile + ": " + e.getMessage(), e);
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
