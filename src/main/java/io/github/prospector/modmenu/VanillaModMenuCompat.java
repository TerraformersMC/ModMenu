package io.github.prospector.modmenu;

import com.google.common.collect.ImmutableMap;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.OptionsScreen;

import java.util.Map;

public class VanillaModMenuCompat implements ModMenuApi {
	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return ImmutableMap.of("minecraft", parent -> new OptionsScreen(parent, MinecraftClient.getInstance().options));
	}
}
