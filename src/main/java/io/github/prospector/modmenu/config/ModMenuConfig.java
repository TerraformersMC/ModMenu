package io.github.prospector.modmenu.config;

public class ModMenuConfig {
	private boolean showLibraries = false;

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public boolean showLibraries() {
		return showLibraries;
	}
}
