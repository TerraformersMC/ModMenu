package com.terraformersmc.modmenu.api;

public interface UpdateChecker {
	/**
	 * Gets called when ModMenu is checking for updates.
	 * This is done in a separate thread, so this call can/should be blocking.
	 *
	 * <p>Your update checker should aim to return an update on the same or a more stable channel than the user's preference which you can get via {@link UpdateChannel#getUserPreference()}.</p>
	 *
	 * @return The update info
	 */
	UpdateInfo checkForUpdates();
}
