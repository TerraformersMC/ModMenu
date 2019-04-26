package io.github.prospector.modmenu;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.Screen;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

	private static final Map<String, Runnable> LEGACY_CONFIG_SCREEN_TASKS = new HashMap<>();
	public static final Map<String, Boolean> MOD_API = new HashMap<>();
	public static final Map<String, Boolean> MOD_CLIENTSIDE = new HashMap<>();

	public static boolean noFabric;

	private static ImmutableMap<String, Function<Screen, Screen>> configScreenFactories = ImmutableMap.of();

	public static boolean hasFactory(String modid) {
		return configScreenFactories.containsKey(modid);
	}

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		Function<Screen, Screen> factory = configScreenFactories.get(modid);
		return factory != null ? factory.apply(menuScreen) : null;
	}

	public static void openConfigScreen(String modid) {
		Runnable opener = LEGACY_CONFIG_SCREEN_TASKS.get(modid);
		if (opener != null) opener.run();
	}

	public static void addLegacyConfigScreenTask(String modid, Runnable task) {
		LEGACY_CONFIG_SCREEN_TASKS.putIfAbsent(modid, task);
	}
	
	public static boolean hasLegacyConfigScreenTask(String modid) {
		return LEGACY_CONFIG_SCREEN_TASKS.containsKey(modid);
	}

	public static void updateCacheApiValue(String modid, boolean value) {
		MOD_API.put(modid, value);
	}

	public static void updateCacheClientsideOnlyValue(String modid, boolean value) {
		MOD_CLIENTSIDE.put(modid, value);
	}

	@Override
	public void onInitializeClient() {
		noFabric = !FabricLoader.getInstance().isModLoaded("fabric");
		ImmutableMap.Builder<String, Function<Screen, Screen>> factories = ImmutableMap.builder();
		FabricLoader.getInstance().getEntrypoints("modmenu", ModMenuApi.class).forEach(api -> factories.put(api.getModId(), api.getConfigScreenFactory()));
		configScreenFactories = factories.build();
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			ModMetadata metadata = mod.getMetadata();
			try {
				if (metadata.containsCustomElement("modmenu:api")) {
					ModMenu.updateCacheApiValue(metadata.getId(), metadata.getCustomElement("modmenu:api").getAsBoolean());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (metadata.containsCustomElement("modmenu:clientsideOnly")) {
					ModMenu.updateCacheClientsideOnlyValue(metadata.getId(), metadata.getCustomElement("modmenu:clientsideOnly").getAsBoolean());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
