package com.terraformersmc.modmenu.api;

import com.terraformersmc.modmenu.config.ModMenuConfig;

/** Supported update channels, ordered from least to most stable. */
public enum UpdateChannel {
	ALPHA,
	BETA,
	RELEASE;

	/**
	 * @return the user's preferred update channel
	 */
	public static UpdateChannel getUserPreference() {
		return ModMenuConfig.UPDATE_CHANNEL.getValue();
	}
}