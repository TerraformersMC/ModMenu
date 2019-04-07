package io.github.prospector.modmenu.api;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.util.ModMenuModConfig;

public class ModMenuApi {

	public static void addConfigOverride(String modid, Runnable action) {
		ModMenu.CONFIG_OVERRIDES.put(modid, action);
	}

	private static void addApiBadgeOverride(String modid, boolean value) {
		ModMenuModConfig config = ModMenu.MOD_MENU_MOD_CONFIG_OVERRIDES.containsKey(modid) ? ModMenu.MOD_MENU_MOD_CONFIG_OVERRIDES.get(modid) : new ModMenuModConfig();
		ModMenu.MOD_MENU_MOD_CONFIG_OVERRIDES.put(modid, config.setApi(value));
	}

	private static void addClientsideOnlyBadgeOverride(String modid, boolean value) {
		ModMenuModConfig config = ModMenu.MOD_MENU_MOD_CONFIG_OVERRIDES.containsKey(modid) ? ModMenu.MOD_MENU_MOD_CONFIG_OVERRIDES.get(modid) : new ModMenuModConfig();
		ModMenu.MOD_MENU_MOD_CONFIG_OVERRIDES.put(modid, config.setClientsideOnly(value));
	}
}
