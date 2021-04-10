package io.github.prospector.modmenu;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.HardcodedUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.screen.Screen;

import java.text.NumberFormat;
import java.util.*;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

	private static final Map<String, Runnable> LEGACY_CONFIG_SCREEN_TASKS = new HashMap<>();
	public static final Set<String> LIBRARY_MODS = new HashSet<>();
	public static final Set<String> ROOT_LIBRARIES = new HashSet<>();
	public static final Set<String> CHILD_LIBRARIES = new HashSet<>();
	public static final Set<String> ALL_NONLIB_MODS = new HashSet<>();
	public static final Set<String> ROOT_NONLIB_MODS = new HashSet<>();
	public static final Set<String> CHILD_NONLIB_MODS = new HashSet<>();
	public static final Set<String> CLIENTSIDE_MODS = new HashSet<>();
	public static final Set<String> PATCHWORK_FORGE_MODS = new HashSet<>();
	public static final LinkedListMultimap<ModContainer, ModContainer> PARENT_MAP = LinkedListMultimap.create();
	private static ImmutableMap<String, ConfigScreenFactory<?>> configScreenFactories = ImmutableMap.of();

	public static boolean hasConfigScreenFactory(String modid) {
		return configScreenFactories.containsKey(modid);
	}

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		ConfigScreenFactory<?> factory = configScreenFactories.get(modid);
		return factory != null ? factory.create(menuScreen) : null;
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

	public static void addLibraryMod(String modid) {
		LIBRARY_MODS.add(modid);
	}

	@Override
	public void onInitializeClient() {
		ModMenuConfigManager.initializeConfig();
		Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();
		FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApi.class).forEach(provider -> {
			ModMenuApi entrypoint = provider.getEntrypoint();
			factories.put(provider.getProvider().getMetadata().getId(), entrypoint.getModConfigScreenFactory());
			entrypoint.getProvidedConfigScreenFactories().forEach(factories::putIfAbsent);
		});
		configScreenFactories = new ImmutableMap.Builder<String, ConfigScreenFactory<?>>().putAll(factories).build();
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
		HardcodedUtil.initializeHardcodings();
		for (ModContainer mod : mods) {
			ModMetadata metadata = mod.getMetadata();
			String id = metadata.getId();
			if ("minecraft".equals(id)) {
				continue;
			}
			boolean isLibrary = metadata.containsCustomValue("modmenu:api") && metadata.getCustomValue("modmenu:api").getAsBoolean();
			if (isLibrary) {
				addLibraryMod(id);
			}
			if (metadata.containsCustomValue("modmenu:clientsideOnly") && metadata.getCustomValue("modmenu:clientsideOnly").getAsBoolean()) {
				CLIENTSIDE_MODS.add(id);
			}
			if (metadata.containsCustomValue("patchwork:patcherMeta")) {
				PATCHWORK_FORGE_MODS.add(id);
			}
			boolean hasParent = false;
			if (metadata.containsCustomValue("modmenu:parent")) {
				String parentId = metadata.getCustomValue("modmenu:parent").getAsString();
				if (parentId != null) {
					Optional<ModContainer> parent = FabricLoader.getInstance().getModContainer(parentId);
					if (parent.isPresent()) {
						hasParent = true;
						PARENT_MAP.put(parent.get(), mod);
						if (isLibrary) {
							CHILD_LIBRARIES.add(id);
						} else {
							CHILD_NONLIB_MODS.add(id);
							ALL_NONLIB_MODS.add(id);
						}
					}
				}
			} else {
				HardcodedUtil.hardcodeModuleMetadata(mod, metadata, id);
				isLibrary = LIBRARY_MODS.contains(id);
				hasParent = PARENT_MAP.containsValue(mod);
			}

			if (isLibrary) {
				(hasParent ? CHILD_LIBRARIES : ROOT_LIBRARIES).add(id);
			} else {
				(hasParent ? CHILD_NONLIB_MODS : ROOT_NONLIB_MODS).add(id);
				ALL_NONLIB_MODS.add(id);
			}
		}
	}

	public static String getDisplayedModCount() {
		return NumberFormat.getInstance().format(ROOT_NONLIB_MODS.size());
	}
}
