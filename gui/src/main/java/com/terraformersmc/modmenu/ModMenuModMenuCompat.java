package com.terraformersmc.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.gui.ModMenuOptionsScreen;
import com.terraformersmc.modmenu.util.mod.fabric.FabricLoaderUpdateChecker;
import com.terraformersmc.modmenu.util.mod.quilt.QuiltLoaderUpdateChecker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.OptionsScreen;

import java.util.Map;

public class ModMenuModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ModMenuOptionsScreen::new;
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return Map.of("minecraft", parent -> new OptionsScreen(parent, MinecraftClient.getInstance().options));
	}

	@Override
	public Map<String, UpdateChecker> getProvidedUpdateCheckers() {
		if (ModMenu.RUNNING_QUILT) {
			return Map.of("quilt_loader", new QuiltLoaderUpdateChecker());
		} else {
			return Map.of("fabricloader", new FabricLoaderUpdateChecker());
		}
	}
}
