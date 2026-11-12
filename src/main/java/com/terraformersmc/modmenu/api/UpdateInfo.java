package com.terraformersmc.modmenu.api;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
public interface UpdateInfo {
    /**
     * @return If an update for the mod is available.
     */
    boolean isUpdateAvailable();

    /**
     * @return The message that is getting displayed when an update is available or <code>null</code> to let ModMenu handle displaying the message.
     */
    default @Nullable Component getUpdateMessage() {
        return null;
    }

    /**
     * @return The URL to the mod download.
     */
    @Nullable String getDownloadLink();

    /**
     * @return The update channel this update is available for.
     */
    UpdateChannel getUpdateChannel();
}
