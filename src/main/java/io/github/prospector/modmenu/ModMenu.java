package io.github.prospector.modmenu;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.screen.Screen;

import java.util.*;
import java.util.function.Function;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

	private static final Map<String, Runnable> LEGACY_CONFIG_SCREEN_TASKS = new HashMap<>();
	public static final Map<String, Boolean> API_MODS = new HashMap<>();
	public static final Set<String> CLIENTSIDE_MODS = new HashSet<>();
	public static final LinkedListMultimap<ModContainer, ModContainer> PARENT_MAP = LinkedListMultimap.create();
	private static ImmutableMap<String, Function<Screen, ? extends Screen>> configScreenFactories = ImmutableMap.of();

	public static boolean hasFactory(String modid) {
		return configScreenFactories.containsKey(modid);
	}

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		Function<Screen, ? extends Screen> factory = configScreenFactories.get(modid);
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
		API_MODS.put(modid, value);
	}

	@Override
	public void onInitializeClient() {
		ImmutableMap.Builder<String, Function<Screen, ? extends Screen>> factories = ImmutableMap.builder();
		FabricLoader.getInstance().getEntrypoints("modmenu", ModMenuApi.class).forEach(api -> factories.put(api.getModId(), api.getConfigScreenFactory()));
		configScreenFactories = factories.build();
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
		for (ModContainer mod : mods) {
			ModMetadata metadata = mod.getMetadata();
			try {
				if (metadata.containsCustomElement("modmenu:api")) {
					ModMenu.updateCacheApiValue(metadata.getId(), metadata.getCustomElement("modmenu:api").getAsBoolean());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (metadata.containsCustomElement("modmenu:clientsideOnly") && metadata.getCustomElement("modmenu:clientsideOnly").getAsBoolean()) {
					ModMenu.CLIENTSIDE_MODS.add(metadata.getId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (metadata.containsCustomElement("modmenu:parent")) {
					String parentId = metadata.getCustomElement("modmenu:parent").getAsString();
					if (parentId != null) {
						Optional<ModContainer> parent = FabricLoader.getInstance().getModContainer(parentId);
						parent.ifPresent(modContainer -> PARENT_MAP.put(modContainer, mod));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
