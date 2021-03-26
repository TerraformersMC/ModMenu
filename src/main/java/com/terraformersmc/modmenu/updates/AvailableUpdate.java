package com.terraformersmc.modmenu.updates;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AvailableUpdate {
	private String version;
	private String url;
	private String changeLog;
	private String provider;

	public AvailableUpdate(String version, String url, String changeLog, String provider) {
		this.version = version;
		this.url = url;
		this.changeLog = changeLog;
		this.provider = provider;
	}

	/**
	 *
	 * @return the new version available
	 */
	public @NotNull String getVersion() {
		return version;
	}

	/**
	 *
	 * @return the url where the user can download the update.
	 */
	public @Nullable String getUrl() {
		return url;
	}

	/**
	 *
	 * @return changelog for this update.
	 */
	public @Nullable String getChangeLog() {
		return changeLog;
	}

	/**
	 *
	 * @return where there update is from
	 */
	public @NotNull String getProvider() {
		return provider;
	}
}
