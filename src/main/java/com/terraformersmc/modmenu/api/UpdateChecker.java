package com.terraformersmc.modmenu.api;

import org.jetbrains.annotations.Nullable;

public interface UpdateChecker {
	/**
	 * Checks for an update for a mod. Mod Menu calls this method on a background thread.
	 *
	 * @return update information, or {@code null} when no update is available
	 */
	@Nullable
	UpdateInfo checkForUpdates();
}