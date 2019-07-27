package io.github.prospector.modmenu;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.screen.Screen;

import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

	private static final Map<String, Runnable> LEGACY_CONFIG_SCREEN_TASKS = new HashMap<>();
	public static final Map<String, Boolean> LIBRARY_MODS = new HashMap<>();
	public static final Set<String> CLIENTSIDE_MODS = new HashSet<>();
	public static final Set<String> FABRIC_MODS = new HashSet<>();
	public static final LinkedListMultimap<ModContainer, ModContainer> PARENT_MAP = LinkedListMultimap.create();
	private static ImmutableMap<String, Function<Screen, ? extends Screen>> configScreenFactories = ImmutableMap.of();
	private static int libraryCount = 0;

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

	public static void updateCacheLibraryValue(String modid, boolean value) {
		LIBRARY_MODS.put(modid, value);
	}

	@Override
	public void onInitializeClient() {
		ModMenuConfigManager.initializeConfig();
		ImmutableMap.Builder<String, Function<Screen, ? extends Screen>> factories = ImmutableMap.builder();
		FabricLoader.getInstance().getEntrypoints("modmenu", ModMenuApi.class).forEach(api -> factories.put(api.getModId(), api.getConfigScreenFactory()));
		configScreenFactories = factories.build();
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
		FABRIC_MODS.add("fabric");
		FABRIC_MODS.add("fabricloader");
		for (ModContainer mod : mods) {
			ModMetadata metadata = mod.getMetadata();
			String id = metadata.getId();
			try {
				if (metadata.containsCustomElement("modmenu:api")) {
					updateCacheLibraryValue(id, metadata.getCustomElement("modmenu:api").getAsBoolean());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (metadata.containsCustomElement("modmenu:clientsideOnly") && metadata.getCustomElement("modmenu:clientsideOnly").getAsBoolean()) {
					CLIENTSIDE_MODS.add(id);
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
				} else {
					if (id.startsWith("fabric-") && Character.isDigit(id.charAt(id.length() - 1)) || id.equals("fabric-api-base") || id.equals("fabric-renderer-indigo")) {
						if (FabricLoader.getInstance().isModLoaded("fabric")) {
							FABRIC_MODS.add(id);
							Optional<ModContainer> parent = FabricLoader.getInstance().getModContainer("fabric");
							parent.ifPresent(modContainer -> PARENT_MAP.put(modContainer, mod));
							if (!LIBRARY_MODS.containsKey(id)) {
								updateCacheLibraryValue(id, true);
							}
							if (id.equals("fabric-keybindings-v0") || id.equals("fabric-models-v0") || id.equals("fabric-renderer-api-v1") || id.equals("fabric-renderer-indigo") || id.equals("fabric-rendering-fluids-v1") || id.equals("fabric-rendering-v0") || id.equals("fabric-textures-v0")) {
								CLIENTSIDE_MODS.add(id);
							}
						}
					}
					if (id.equals("fabricloader") || id.equals("fabric") || metadata.getName().endsWith(" API")) {
						if (!LIBRARY_MODS.containsKey(id)) {
							updateCacheLibraryValue(id, true);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		LIBRARY_MODS.forEach((id, value) -> {
			if (value != null && value) {
				libraryCount++;
			}
		});
	}

	public static void toggleLibrariesShown() {
		ModMenuConfigManager.getConfig().toggleShowLibraries();
	}

	public static String getFormattedModCount() {
		return NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size());
	}
}
