package io.github.prospector.modmenu;

import com.google.common.collect.ImmutableMap;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SettingsScreen;

import java.util.Map;

public class VanillaModMenuCompat implements ModMenuApi {
	@Override
	public String getModId() {
		return ModMenu.MOD_ID;
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return ImmutableMap.of("minecraft", parent -> new SettingsScreen(parent, MinecraftClient.getInstance().options));
	}
}
