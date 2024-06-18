package com.terraformersmc.modmenu.api;

/**
 * Supported update channels, in ascending order by stability.
 */
public enum UpdateChannel {
	ALPHA, BETA, RELEASE;

	/**
	 * @return the user's preferred update channel.
	 */
	public static UpdateChannel getUserPreference() {
		return ModMenuApiImpl.getUpdateHandlerPreferenceProvider().getUpdateChannelPreference();
	}
}
