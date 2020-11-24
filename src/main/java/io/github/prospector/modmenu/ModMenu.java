package io.github.prospector.modmenu;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.function.Function;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

	private static final Map<String, Runnable> LEGACY_CONFIG_SCREEN_TASKS = new HashMap<>();
	public static final List<String> LIBRARY_MODS = new ArrayList<>();
	public static final Set<String> CLIENTSIDE_MODS = new HashSet<>();
	public static final Set<String> PATCHWORK_FORGE_MODS = new HashSet<>();
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

	public static void addLibraryMod(String modid) {
		if(LIBRARY_MODS.contains(modid)) return;

		LIBRARY_MODS.add(modid);
	}

	@Override
	public void onInitializeClient() {
		ModMenuConfigManager.initializeConfig();
		ImmutableMap.Builder<String, Function<Screen, ? extends Screen>> factories = ImmutableMap.builder();
		FabricLoader.getInstance().getEntrypoints("modmenu", ModMenuApi.class).forEach(api -> factories.put(api.getModId(), api.getConfigScreenFactory()));
		configScreenFactories = factories.build();
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
		HardcodedUtil.initializeHardcodings();
		for (ModContainer mod : mods) {
			ModMetadata metadata = mod.getMetadata();
			String id = metadata.getId();
			if (metadata.containsCustomValue("modmenu:api") && metadata.getCustomValue("modmenu:api").getAsBoolean()) {
				addLibraryMod(id);
			}
			if (metadata.containsCustomValue("modmenu:clientsideOnly") && metadata.getCustomValue("modmenu:clientsideOnly").getAsBoolean()) {
				CLIENTSIDE_MODS.add(id);
			}
			if (metadata.containsCustomValue("patchwork:patcherMeta")) {
				PATCHWORK_FORGE_MODS.add(id);
			}
			if (metadata.containsCustomValue("modmenu:parent")) {
				String parentId = metadata.getCustomValue("modmenu:parent").getAsString();
				if (parentId != null) {
					Optional<ModContainer> parent = FabricLoader.getInstance().getModContainer(parentId);
					parent.ifPresent(modContainer -> PARENT_MAP.put(modContainer, mod));
				}
			} else {
				HardcodedUtil.hardcodeModuleMetadata(mod, metadata, id);
			}
		}
		libraryCount = LIBRARY_MODS.size();
	}

	public static String getFormattedModCount() {
		return NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size());
	}
}
