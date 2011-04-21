package com.ganggarrison.gmdec.files;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.Include;

import com.ganggarrison.gmdec.FileTools;
import com.ganggarrison.gmdec.xml.IncludedFileXmlFormat;

public class IncludedFileFormat {
	public static void read(File path, GmFile gmf) throws IOException {
		File[] xmlFiles = path.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".meta.xml");
			}
		});

		for (File xmlFile : xmlFiles) {
			File dataFile = new File(path, createDataFilename(xmlFile.getName()));
			gmf.includes.add(read(xmlFile, dataFile));
		}
	}

	public static void write(File path, Collection<Include> includes) throws IOException {
		for (Include include : includes) {
			write(path, include);
		}
	}

	private static Include read(File xmlPath, File dataPath) throws IOException {
		Include include = new IncludedFileXmlFormat().read(xmlPath, null);
		if (dataPath.isFile()) {
			if (include.data == null) {
				System.err.println("Warning: Included file \"" + include.filename + "\" found but not expected.");
			}
			include.data = FileTools.readWholeFileBytes(dataPath);
		} else {
			if (include.data != null) {
				System.err.println("Warning: Missing included file \"" + include.filename + "\".");
			}
			include.data = null;
		}
		return include;
	}

	private static void write(File path, Include include) throws IOException {
		String filename;
		String extension;
		
		int extStart = include.filename.lastIndexOf(".");
		if (extStart >= 0) {
			filename = FileTools.replaceBadChars(include.filename.substring(0, extStart));
			extension = FileTools.replaceBadChars(include.filename.substring(extStart + 1));
		} else {
			filename = FileTools.replaceBadChars(include.filename);
			extension = "";
		}
		
		String dataFileName = filename;
		File xmlFile = new File(path, createXmlFilename(dataFileName, extension));

		int i=2;
		while (xmlFile.exists()) {
			dataFileName = filename + "_" + i;
			xmlFile = new File(path, createXmlFilename(dataFileName, extension));
			i++;
		}
		
		new IncludedFileXmlFormat().write(include, xmlFile);

		if (include.data != null) {
			File dataFile = new File(path, createDataFilename(xmlFile.getName()));
			FileTools.writeFile(dataFile, include.data);
		}
	}

	private static String createXmlFilename(String basename, String extension) {
		String name;
		if (extension.equals("")) {
			name = basename;
		} else {
			name = basename + "." + extension;
		}
		return name.toLowerCase() + ".meta.xml";
	}

	private static String createDataFilename(String xmlFilename) {
		assert xmlFilename.endsWith(".meta.xml");
		return xmlFilename.replace(".meta.xml", "");
	}
}
