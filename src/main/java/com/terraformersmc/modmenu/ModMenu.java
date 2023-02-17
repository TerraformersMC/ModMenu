package com.terraformersmc.modmenu;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.util.ModrinthUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricDummyParentMod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import com.terraformersmc.modmenu.util.mod.quilt.QuiltMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final String GITHUB_REF = "TerraformersMC/ModMenu";
	public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu");
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();
	public static boolean MOD_UPDATE_AVAILABLE = false;

	private static ImmutableMap<String, ConfigScreenFactory<?>> configScreenFactories = ImmutableMap.of();
	private static List<Supplier<Map<String, ConfigScreenFactory<?>>>> dynamicScreenFactories = new ArrayList<>();

	private static int cachedDisplayedModCount = -1;

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		if (ModMenuConfig.HIDDEN_CONFIGS.getValue().contains(modid)) {
			return null;
		}
		ConfigScreenFactory<?> factory = configScreenFactories.get(modid);
		if (factory != null) {
			return factory.create(menuScreen);
		}
		for (Supplier<Map<String, ConfigScreenFactory<?>>> dynamicFactoriesSupplier : dynamicScreenFactories) {
			factory = dynamicFactoriesSupplier.get().get(modid);
			if (factory != null) {
				return factory.create(menuScreen);
			}
		}
		return null;
	}

	@Override
	public void onInitializeClient() {
		ModMenuConfigManager.initializeConfig();
		Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();
		Set<String> modpackMods = new HashSet<>();
		FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApi.class).forEach(entrypoint -> {
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			String modId = metadata.getId();
			try {
				ModMenuApi api = entrypoint.getEntrypoint();
				factories.put(modId, api.getModConfigScreenFactory());
				dynamicScreenFactories.add(api::getProvidedConfigScreenFactories);
				api.attachModpackBadges(modpackMods::add);
			} catch (Throwable e) {
				LOGGER.error("Mod {} provides a broken implementation of ModMenuApi", modId, e);
			}
		});
		configScreenFactories = ImmutableMap.copyOf(factories);


		// Fill mods map
		for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
			if (!ModMenuConfig.HIDDEN_MODS.getValue().contains(modContainer.getMetadata().getId())) {
				if (FabricLoader.getInstance().isModLoaded("quilt_loader")) {
					QuiltMod mod = new QuiltMod(modContainer, modpackMods);
					MODS.put(mod.getId(), mod);
					ModrinthUtil.checkForUpdates(mod);
				} else {
					FabricMod mod = new FabricMod(modContainer, modpackMods);
					MODS.put(mod.getId(), mod);
					ModrinthUtil.checkForUpdates(mod);
				}
			}
		}

		Map<String, Mod> dummyParents = new HashMap<>();

		// Initialize parent map
		for (Mod mod : MODS.values()) {
			String parentId = mod.getParent();
			if (parentId != null) {
				Mod parent = MODS.getOrDefault(parentId, dummyParents.get(parentId));
				if (parent == null) {
					if (mod instanceof FabricMod) {
						parent = new FabricDummyParentMod((FabricMod) mod, parentId);
						dummyParents.put(parentId, parent);
					}
				}
				PARENT_MAP.put(parent, mod);
			} else {
				ROOT_MODS.put(mod.getId(), mod);
			}
		}
		MODS.putAll(dummyParents);
		Util.getMainWorkerExecutor().execute(() -> {
			// since update checks are run all on the main worker executor we need to run this on it too to make sure the update checks run first.
			MOD_UPDATE_AVAILABLE = MODS.values().stream().anyMatch(m -> m.getModrinthData() != null);
		});
		ModMenuEventHandler.register();
	}

	public static void clearModCountCache() {
		cachedDisplayedModCount = -1;
	}

	public static String getDisplayedModCount() {
		if (cachedDisplayedModCount == -1) {
			// listen, if you have >= 2^32 mods then that's on you
			cachedDisplayedModCount = Math.toIntExact(MODS.values().stream().filter(mod ->
					(ModMenuConfig.COUNT_CHILDREN.getValue() || mod.getParent() == null) &&
							(ModMenuConfig.COUNT_LIBRARIES.getValue() || !mod.getBadges().contains(Mod.Badge.LIBRARY)) &&
							(ModMenuConfig.COUNT_HIDDEN_MODS.getValue() || !ModMenuConfig.HIDDEN_MODS.getValue().contains(mod.getId()))
			).count());
		}
		return NumberFormat.getInstance().format(cachedDisplayedModCount);
	}

	public static Text createModsButtonText() {
		MutableText modsText = Text.translatable("modmenu.title");
		if (ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnModsButton() && ModMenuConfig.MODS_BUTTON_STYLE.getValue() != ModMenuConfig.ModsButtonStyle.ICON) {
			String count = ModMenu.getDisplayedModCount();
			if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.SHRINK) {
				modsText.append(Text.literal(" ")).append(Text.translatable("modmenu.loaded.short", count));
			} else {
				String specificKey = "modmenu.loaded." + count;
				String key = I18n.hasTranslation(specificKey) ? specificKey : "modmenu.loaded";
				if (ModMenuConfig.EASTER_EGGS.getValue() && I18n.hasTranslation(specificKey + ".secret")) {
					key = specificKey + ".secret";
				}
				modsText.append(Text.literal(" ")).append(Text.translatable(key, count));
			}
		}
		return modsText;
	}
}
