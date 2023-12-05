package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.api.UpdateInfo;

public class ModrinthUpdateInfo implements UpdateInfo {

	protected String projectId;
	protected String versionId;
	protected String versionNumber;

	public ModrinthUpdateInfo(String projectId, String versionId, String versionNumber) {
		this.projectId = projectId;
		this.versionId = versionId;
		this.versionNumber = versionNumber;
	}

	@Override
	public boolean isUpdateAvailable() {
		return true;
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

}
