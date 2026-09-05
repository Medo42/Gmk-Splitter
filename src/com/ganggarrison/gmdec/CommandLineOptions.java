/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 *
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import com.ganggarrison.gmdec.GmkSplitter.IdPreservation;

public final class CommandLineOptions {
	public enum DuplicateIdType {
		TILES("tiles"), INSTANCES("instances");

		private final String optionValue;

		private DuplicateIdType(String optionValue) {
			this.optionValue = optionValue;
		}

		public String getOptionValue() {
			return optionValue;
		}

		private static DuplicateIdType fromOptionValue(String value) throws ParseException {
			for (DuplicateIdType type : values()) {
				if (type.optionValue.equals(value)) {
					return type;
				}
			}
			throw new ParseException("Invalid value for --allow-duplicate-ids: " + value
					+ ". Valid values are: tiles, instances");
		}
	}

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException(String message) {
			super(message);
		}
	}

	private final String source;
	private final String destination;
	private final IdPreservation idPreservation;
	private final EnumSet<DuplicateIdType> allowedDuplicateIds;
	private final Charset charset;

	private CommandLineOptions(String source, String destination, IdPreservation idPreservation,
			EnumSet<DuplicateIdType> allowedDuplicateIds, Charset charset) {
		this.source = source;
		this.destination = destination;
		this.idPreservation = idPreservation;
		this.allowedDuplicateIds = EnumSet.copyOf(allowedDuplicateIds);
		this.charset = charset;
	}

	public static CommandLineOptions parse(String[] args) throws ParseException {
		List<String> paths = new ArrayList<String>();
		IdPreservation idPreservation = IdPreservation.OBJECTS;
		EnumSet<DuplicateIdType> allowedDuplicateIds = EnumSet.noneOf(DuplicateIdType.class);
		Charset charset = null;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("--preserve-ids") || arg.startsWith("--preserve-ids=")) {
				String value;
				if (arg.startsWith("--preserve-ids=")) {
					value = arg.substring("--preserve-ids=".length());
				} else if (i + 1 < args.length) {
					value = args[++i];
				} else {
					throw new ParseException("Option --preserve-ids requires a value.");
				}
				try {
					idPreservation = IdPreservation.valueOf(value.toUpperCase(Locale.ROOT));
				} catch (IllegalArgumentException e) {
					throw new ParseException("Invalid value for --preserve-ids: " + value
							+ ". Valid values are: none, objects, all");
				}
			} else if (arg.equals("--allow-duplicate-ids") || arg.startsWith("--allow-duplicate-ids=")) {
				String value;
				if (arg.startsWith("--allow-duplicate-ids=")) {
					value = arg.substring("--allow-duplicate-ids=".length());
				} else if (i + 1 < args.length) {
					value = args[++i];
				} else {
					throw new ParseException("Option --allow-duplicate-ids requires a value.");
				}
				parseAllowedDuplicateIds(value, allowedDuplicateIds);
			} else if (arg.equals("--charset") || arg.startsWith("--charset=")) {
				String value;
				if (arg.startsWith("--charset=")) {
					value = arg.substring("--charset=".length());
				} else if (i + 1 < args.length) {
					value = args[++i];
				} else {
					throw new ParseException("Option --charset requires a value.");
				}
				charset = parseCharset(value);
			} else if (arg.startsWith("-")) {
				throw new ParseException("Unknown option: " + arg);
			} else {
				paths.add(arg);
			}
		}

		if (paths.size() != 2) {
			throw new ParseException("Expected a source and destination path.");
		}
		if (!allowedDuplicateIds.isEmpty() && idPreservation != IdPreservation.ALL) {
			throw new ParseException("Option --allow-duplicate-ids requires --preserve-ids all.");
		}

		return new CommandLineOptions(paths.get(0), paths.get(1), idPreservation, allowedDuplicateIds, charset);
	}

	private static Charset parseCharset(String value) throws ParseException {
		try {
			return Charset.forName(value.trim());
		} catch (IllegalCharsetNameException e) {
			throw new ParseException("Invalid value for --charset: " + value);
		} catch (UnsupportedCharsetException e) {
			throw new ParseException("Charset not supported by this Java installation: " + value);
		}
	}

	private static void parseAllowedDuplicateIds(String value, EnumSet<DuplicateIdType> result)
			throws ParseException {
		String[] parts = value.split(",", -1);
		for (String part : parts) {
			String normalized = part.trim().toLowerCase(Locale.ROOT);
			if (normalized.length() == 0) {
				throw new ParseException("Option --allow-duplicate-ids contains an empty value.");
			}
			DuplicateIdType type = DuplicateIdType.fromOptionValue(normalized);
			if (!result.add(type)) {
				throw new ParseException("Duplicate value for --allow-duplicate-ids: " + normalized);
			}
		}
	}

	public String getSource() {
		return source;
	}

	public String getDestination() {
		return destination;
	}

	public IdPreservation getIdPreservation() {
		return idPreservation;
	}

	public EnumSet<DuplicateIdType> getAllowedDuplicateIds() {
		return EnumSet.copyOf(allowedDuplicateIds);
	}

	/**
	 * The charset to use for text in .gmk files, or null if none was given.
	 */
	public Charset getCharset() {
		return charset;
	}

	public static void printUsage(PrintStream out) {
		out.println("Usage: java -jar GmkSplit.jar [options] <source> <dest>");
		out.println("One of <source> or <dest> must be the name of a .gmk or .gm81 file.");
		out.println("Using a .gmk file as destination will create a GM 8.0 file.");
		out.println("Using a .gm81 file as destination will create a GM 8.1 file.");
		out.println("The destination must not already exist. This tool won't overwrite.");
		out.println();
		out.println("Options:");
		out.println("  --preserve-ids <none|objects|all>");
		out.println("      Which numeric IDs to store in and read back from the directory tree.");
		out.println("      objects: Keep Object IDs only. (default)");
		out.println("      all:     Also keep the IDs of all other resources, instances and tiles.");
		out.println("      none:    Don't keep any IDs. May change instance execution order!");
		out.println("      Use the same setting for splitting and rejoining a project.");
		out.println("  --allow-duplicate-ids <tiles|instances|tiles,instances>");
		out.println("      Preserve valid duplicate tile and/or instance IDs when joining a tree.");
		out.println("      Missing or invalid IDs are still assigned above the existing maximum.");
		out.println("      Requires --preserve-ids all.");
		out.println("  --charset <name>");
		out.println("      Charset of the text in GM 8.0 (.gmk) files, e.g. windows-1252 or MS949.");
		out.println("      GM 8.0 stores text in the Windows ANSI code page of the system it runs");
		out.println("      on, so a .gmk file created on a Korean system needs MS949, and so on.");
		out.println("      When splitting, the default is the code page of the current system. The");
		out.println("      charset is stored in " + TreeMetadata.FILENAME + " in the tree and used again");
		out.println("      when rejoining, unless this option is given. GM 8.1 (.gm81) files are");
		out.println("      always UTF-8 and are not affected by this option.");
	}
}
