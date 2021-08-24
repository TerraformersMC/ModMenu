package com.terraformersmc.modmenu.updates;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AvailableUpdate {
	private final @NotNull String version;
	private final @Nullable String url;
	private final @Nullable String changelog;
	private final @NotNull String provider;

	public AvailableUpdate(@NotNull String version,
						   @Nullable String url,
						   @Nullable String changelog,
						   @NotNull String provider) {
		this.version = version;
		this.url = url;
		this.changelog = changelog;
		this.provider = provider;
	}

	/**
	 * @return the new version available
	 */
	public @NotNull String getVersion() {
		return version;
	}

	/**
	 * @return the url where the user can download the update.
	 */
	public @Nullable String getUrl() {
		return url;
	}

	/**
	 * @return changelog for this update.
	 */
	public @Nullable String getChangelog() {
		return changelog;
	}

	/**
	 * @return where there update is from
	 */
	public @NotNull String getProvider() {
		return provider;
	}
}
