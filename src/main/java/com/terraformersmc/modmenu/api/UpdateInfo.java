package com.terraformersmc.modmenu.api;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface UpdateInfo {
	/**
	 * @return whether an update is available
	 */
	boolean isUpdateAvailable();

	@Nullable
	default Text getUpdateMessage() {
		return null;
	}

	/**
	 * @return the URL to download the update
	 */
	String getDownloadLink();

	/**
	 * @return the stability channel of the update
	 */
	UpdateChannel getUpdateChannel();
}