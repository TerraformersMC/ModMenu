package com.terraformersmc.modmenu.api;

import net.fabricmc.loader.api.FabricLoader;

class ModMenuApiImpl {
	static ModMenuGuiProvider guiProvider = null;
	static ModMenuUpdateHandlerPreferenceProvider updateHandlerPreferenceProvider = null;

	static ModMenuGuiProvider getGuiProvider() {
		if (guiProvider == null) {
			var entrypoints = FabricLoader.getInstance().getEntrypoints("modmenu-gui", ModMenuGuiProvider.class);
			if (!entrypoints.isEmpty()) {
				guiProvider = entrypoints.getFirst();
			}
		}
		if (guiProvider == null) {
			throw new RuntimeException("Mod Menu's GUI is not installed, download here: https://modrinth.com/mod/modmenu");
		}
		return guiProvider;
	}

	static ModMenuUpdateHandlerPreferenceProvider getUpdateHandlerPreferenceProvider() {
		if (updateHandlerPreferenceProvider == null) {
			var entrypoints = FabricLoader.getInstance()
				.getEntrypoints("modmenu-update-preferences", ModMenuUpdateHandlerPreferenceProvider.class);
			if (!entrypoints.isEmpty()) {
				updateHandlerPreferenceProvider = entrypoints.getFirst();
			}
		}
		if (updateHandlerPreferenceProvider == null) {
			throw new RuntimeException("Mod Menu's GUI is not installed, download here: https://modrinth.com/mod/modmenu");
		}
		return updateHandlerPreferenceProvider;
	}
}
