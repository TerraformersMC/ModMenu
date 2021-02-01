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
import com.terraformersmc.modmenu.util.ModMenuApiMarker;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricDummyParentMod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();

	private static ImmutableMap<String, ConfigScreenFactory<?>> configScreenFactories = ImmutableMap.of();

	private static int cachedDisplayedModCount = -1;

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		ConfigScreenFactory<?> factory = configScreenFactories.get(modid);
		return factory != null ? factory.create(menuScreen) : null;
	}

	@Override
	public void onInitializeClient() {
		ModMenuConfigManager.initializeConfig();
		Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();
		FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApiMarker.class).forEach(entrypoint -> {
			ModMenuApiMarker marker = entrypoint.getEntrypoint();
			if (marker instanceof ModMenuApi) {
				/* Current API */
				ModMenuApi api = (ModMenuApi) marker;
				factories.put(entrypoint.getProvider().getMetadata().getId(), api.getModConfigScreenFactory());
				api.getProvidedConfigScreenFactories().forEach(factories::putIfAbsent);
			} else if (marker instanceof io.github.prospector.modmenu.api.ModMenuApi) {
				/* Legacy API */
				io.github.prospector.modmenu.api.ModMenuApi api = (io.github.prospector.modmenu.api.ModMenuApi) entrypoint.getEntrypoint();
				factories.put(entrypoint.getProvider().getMetadata().getId(), screen -> api.getModConfigScreenFactory().create(screen));
				api.getProvidedConfigScreenFactories().forEach((id, legacyFactory) -> factories.put(id, legacyFactory::create));
			} else {
				throw new RuntimeException(entrypoint.getProvider().getMetadata().getId() + " is providing an invalid ModMenuApi implementation");
			}
		});
		configScreenFactories = new ImmutableMap.Builder<String, ConfigScreenFactory<?>>().putAll(factories).build();


		// Fill mods map
		for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
			if (!ModMenuConfig.HIDDEN_MODS.getValue().contains(modContainer.getMetadata().getId())) {
				FabricMod mod = new FabricMod(modContainer);
				MODS.put(mod.getId(), mod);
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
		TranslatableText modsText = new TranslatableText("modmenu.title");
		if (ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnModsButton() && ModMenuConfig.MODS_BUTTON_STYLE.getValue() != ModMenuConfig.ModsButtonStyle.ICON) {
			if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.SHRINK) {
				modsText.append(new LiteralText(" ")).append(new TranslatableText("modmenu.loaded.short", ModMenu.getDisplayedModCount()));
			} else {
				modsText.append(new LiteralText(" ")).append(new TranslatableText("modmenu.loaded", ModMenu.getDisplayedModCount()));
			}
		}
		return modsText;
	}
}
