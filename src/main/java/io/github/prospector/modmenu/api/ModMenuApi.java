package io.github.prospector.modmenu.api;

import io.github.prospector.modmenu.ModMenu;

public class ModMenuApi {
	public static void addConfigOverride(String modid, Runnable action) {
		ModMenu.CONFIG_OVERRIDES.put(modid, action);
	}
}
