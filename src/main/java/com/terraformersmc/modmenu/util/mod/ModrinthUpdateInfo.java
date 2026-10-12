package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateInfo;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ModrinthUpdateInfo implements UpdateInfo {
	private final String projectId;
	private final String versionId;
	private final String versionNumber;
	private final UpdateChannel updateChannel;

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
	public Text getUpdateMessage() {
		return new TranslatableText("modmenu.updateText", versionNumber, new TranslatableText("modmenu.modrinth"));
	}

	@Override
	public String getDownloadLink() {
		return "https://modrinth.com/project/" + projectId + "/version/" + versionId;
	}

	@Override
	public UpdateChannel getUpdateChannel() {
		return updateChannel;
	}
}