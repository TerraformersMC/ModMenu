package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.util.VersionUtil;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ModrinthUpdateInfo implements UpdateInfo {
	protected final String projectId;
	protected final String versionId;
	protected final String versionNumber;
	protected final UpdateChannel updateChannel;

	private static final Text MODRINTH_TEXT = Text.translatable("modmenu.modrinth");

	public ModrinthUpdateInfo(String projectId, String versionId, String versionNumber, UpdateChannel updateChannel) {
		this.projectId = projectId;
		this.versionId = versionId;
		this.versionNumber = versionNumber;
		this.updateChannel = updateChannel;
	}

	@Override
	public boolean isUpdateAvailable() {
		return true;
	}

	@Override
	public @Nullable Text getUpdateMessage() {
		return Text.translatable("modmenu.updateText", VersionUtil.stripPrefix(this.versionNumber), MODRINTH_TEXT);
	}

	@Override
	public String getDownloadLink() {
		return "https://modrinth.com/project/%s/version/%s".formatted(projectId, versionId);
	}

	public String getProjectId() {
		return projectId;
	}

	public String getVersionId() {
		return versionId;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	@Override
	public UpdateChannel getUpdateChannel() {
		return this.updateChannel;
	}
}
