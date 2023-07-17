package com.terraformersmc.modmenu.util;

import java.util.List;

public final class VersionUtil {
	private static final List<String> PREFIXES = List.of("version", "ver", "v");

	private VersionUtil() {
		return;
	}

	public static String stripPrefix(String version) {
		version = version.trim();

		for (String prefix : PREFIXES) {
			if (version.startsWith(prefix)) {
				return version.substring(prefix.length());
			}
		}

		return version;
	}

	public static String getPrefixedVersion(String version) {
		return "v" + stripPrefix(version);
	}
}
