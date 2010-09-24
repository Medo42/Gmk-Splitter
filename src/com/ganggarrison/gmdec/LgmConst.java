package com.ganggarrison.gmdec;

public final class LgmConst {
	public static interface Provider {
		byte getLgmConst();
	}

	private LgmConst() {
		throw new AssertionError("This class isn't supposed to be instantiated.");
	}

	public static <T extends Enum<? extends Provider>> String toString(byte lgmConst, Class<T> enumType) {
		for (Provider lcp : (Provider[]) enumType.getEnumConstants()) {
			if (lcp.getLgmConst() == lgmConst) {
				return lcp.toString();
			}
		}
		throw new IllegalArgumentException("Error: Found "+enumType+" with unknown integer constant "+lgmConst+".");
	}

	public static <T extends Enum<? extends Provider>> byte fromString(String string, Class<T> enumType) {
		return ((Provider) Enum.valueOf((Class) enumType, string)).getLgmConst();
	}
}
