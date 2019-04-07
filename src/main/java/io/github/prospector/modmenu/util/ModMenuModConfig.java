package io.github.prospector.modmenu.util;

public class ModMenuModConfig {
	public boolean clientsideOnly;
	public boolean api;

	public ModMenuModConfig() {
	}

	public ModMenuModConfig setClientsideOnly(boolean clientsideOnly) {
		this.clientsideOnly = clientsideOnly;
		return this;
	}

	public ModMenuModConfig setApi(boolean api) {
		this.api = api;
		return this;
	}

	public boolean isModClientsideOnly() {
		return clientsideOnly;
	}

	public boolean isModApi() {
		return api;
	}
}
