package com.terraformersmc.modmenu.updates;

public class AvailableUpdate {
	private String version;
	private String url;
	private String changeLog;
	private String provider;

	public AvailableUpdate(String version, String url, String changeLog, String provider) {
		this.version = version;
		this.url = url;
		this.changeLog = changeLog;
		this.provider = provider;
	}

	public String getVersion() {
		return version;
	}

	public String getUrl() {
		return url;
	}

	public String getChangeLog() {
		return changeLog;
	}

	public String getProvider() {
		return provider;
	}
}
