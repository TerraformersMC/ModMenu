package com.terraformersmc.modmenu.api;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface UpdateInfo {
	/**
	 * @return If an update for the mod is available.
	 */
	boolean isUpdateAvailable();

	/**
	 * @return The message that is getting displayed when an update is available or <code>null</code> to let ModMenu handle displaying the message.
	 */
	@Nullable
	default Text getUpdateMessage() {
		return null;
	}

	/**
	 * @return The URL to the mod download.
	 */
	String getDownloadLink();

	/**
	 * @return The update channel this update is available for.
	 */
	UpdateChannel getUpdateChannel();
}
