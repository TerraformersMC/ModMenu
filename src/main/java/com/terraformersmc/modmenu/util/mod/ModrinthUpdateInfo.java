package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.util.VersionUtil;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record ModrinthUpdateInfo(String projectId, String versionId, String versionNumber,
                                 UpdateChannel getUpdateChannel) implements UpdateInfo {
    private static final Component MODRINTH_TEXT = Component.translatable("modmenu.modrinth");

    @Override
    public boolean isUpdateAvailable() {
        return true;
    }

	@Override
	public @NotNull Component getUpdateMessage() {
        String versionNoPrefix = VersionUtil.stripPrefix(this.versionNumber);
        String key = "modmenu.updateText";
        if (!Character.isDigit(versionNoPrefix.charAt(0))) {
            key += ".nonNumerical";
        }
        return Component.translatable(key, versionNoPrefix, MODRINTH_TEXT);
	}

    @Override
    public String getDownloadLink() {
        return "https://modrinth.com/project/%s/version/%s".formatted(projectId, versionId);
    }
}
