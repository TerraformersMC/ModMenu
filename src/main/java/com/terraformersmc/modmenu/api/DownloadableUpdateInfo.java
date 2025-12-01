package com.terraformersmc.modmenu.api;

import org.jetbrains.annotations.Nullable;

// This interface signals that we have enough information to perform an automated download.
public interface DownloadableUpdateInfo extends UpdateInfo {
    /**
     * @return The direct URL to download the mod's JAR file.
     */
    String getDownloadUrl();

    /**
     * @return The intended filename for the downloaded JAR (e.g., "my-mod-1.2.0.jar").
     */
    String getFileName();

    /**
     * @return The SHA512 hash of the file for verification, if available.
     */
    @Nullable
    String getFileHash();
}
