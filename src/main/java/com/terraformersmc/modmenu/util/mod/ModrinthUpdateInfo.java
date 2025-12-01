package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.api.DownloadableUpdateInfo;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.util.VersionUtil;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public record ModrinthUpdateInfo(String projectId, String versionId, String versionNumber,
                                             UpdateChannel updateChannel, String downloadUrl, String fileName, String fileHash)
        implements DownloadableUpdateInfo {
    private static final Text MODRINTH_TEXT = Text.translatable("modmenu.modrinth");

    @Override
    public boolean isUpdateAvailable() {
        return true;
    }

    @Override
    public @NotNull Text getUpdateMessage() {
        return Text.translatable("modmenu.updateText", VersionUtil.stripPrefix(this.versionNumber), MODRINTH_TEXT);
    }

    @Override
    public String getDownloadLink() {
        return "https://modrinth.com/project/%s/version/%s".formatted(projectId, versionId);
    }

    @Override
    public UpdateChannel getUpdateChannel() {
        return this.updateChannel;
    }

    @Override
    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getFileHash() {
        return this.fileHash;
    }
}
