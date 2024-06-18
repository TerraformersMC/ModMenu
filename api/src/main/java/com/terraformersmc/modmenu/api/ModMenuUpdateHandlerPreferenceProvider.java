package com.terraformersmc.modmenu.api;

/**
 * This is used to provide a user's preferences for the updates API in Mod Menu.
 * To use it, implement this as a "modmenu-update-preferences" entrypoint to your mod.
 */
public interface ModMenuUpdateHandlerPreferenceProvider {
	/**
	 * Used for your GUI provider to indicate a user's update channel preferences to users of Mod Menu API
	 *
	 * @return The update channel the user prefers
	 */
	UpdateChannel getUpdateChannelPreference();
}
