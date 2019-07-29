package io.github.prospector.modmenu.util;

import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.commons.lang3.text.WordUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FabricHardcodedBsUtil {
	private static final Pattern FABRIC_PATTERN = Pattern.compile("^fabric-.*(-v\\d+)$");
	private static final Set<String> FABRIC_MODS = new HashSet<>();
	private static final HashMap<String, String> FABRIC_DESCRIPTIONS = new HashMap<>();

	public static void initializeFabricHardcodedBs() {
		FABRIC_MODS.add("fabric");
		FABRIC_MODS.add("fabricloader");
		FABRIC_DESCRIPTIONS.put("fabric-api-base", "Contains the essentials for Fabric API modules.");
		FABRIC_DESCRIPTIONS.put("fabric-biomes-v1", "Hooks for adding biomes to the default world generator.");
		FABRIC_DESCRIPTIONS.put("fabric-commands-v0", "Adds command-related hooks.");
		FABRIC_DESCRIPTIONS.put("fabric-containers-v0", "Adds hooks for containers.");
		FABRIC_DESCRIPTIONS.put("fabric-content-registries-v0", "Adds registries for vanilla mechanics that are missing them.");
		FABRIC_DESCRIPTIONS.put("fabric-crash-report-info-v1", "Adds Fabric-related debug info to crash reports.");
		FABRIC_DESCRIPTIONS.put("fabric-events-interaction-v0", "Events for player interaction with blocks and entities.");
		FABRIC_DESCRIPTIONS.put("fabric-events-lifecycle-v0", "Events for the game's lifecycle.");
		FABRIC_DESCRIPTIONS.put("fabric-item-groups-v0", "An API for adding custom item groups.");
		FABRIC_DESCRIPTIONS.put("fabric-keybindings-v0", "Keybinding registry API.");
		FABRIC_DESCRIPTIONS.put("fabric-loot-tables-v1", "Hooks for manipulating loot tables.");
		FABRIC_DESCRIPTIONS.put("fabric-mining-levels-v0", "Block mining level tags for tools.");
		FABRIC_DESCRIPTIONS.put("fabric-models-v0", "Hooks for models and model loading.");
		FABRIC_DESCRIPTIONS.put("fabric-networking-blockentity-v0", "Networking hooks for block entities.");
		FABRIC_DESCRIPTIONS.put("fabric-networking-v0", "Networking packet hooks and registries.");
		FABRIC_DESCRIPTIONS.put("fabric-object-builders-v0", "Builders for objects vanilla has locked down.");
		FABRIC_DESCRIPTIONS.put("fabric-registry-sync-v0", "Syncs registry mappings.");
		FABRIC_DESCRIPTIONS.put("fabric-renderer-api-v1", "Defines rendering extensions for dynamic/fancy block and item models.");
		FABRIC_DESCRIPTIONS.put("fabric-renderer-indigo", "Default implementation of the Fabric Renderer API.");
		FABRIC_DESCRIPTIONS.put("fabric-rendering-data-attachment-v1", "Thread-safe hooks for BlockEntity data use during terrain rendering.");
		FABRIC_DESCRIPTIONS.put("fabric-rendering-fluids-v1", "Hooks for registering fluid renders.");
		FABRIC_DESCRIPTIONS.put("fabric-rendering-v0", "Hooks and registries for rendering-related things");
		FABRIC_DESCRIPTIONS.put("fabric-resource-loader-v0", "Asset and data resource loading.");
		FABRIC_DESCRIPTIONS.put("fabric-tag-extensions-v0", "Hooks for tags.");
		FABRIC_DESCRIPTIONS.put("fabric-textures-v0", "Hooks for texture loading and registration.");
	}

	public static void hardcodeModuleMetadata(ModContainer mod, ModMetadata metadata, String id) {
		Matcher matcher = FABRIC_PATTERN.matcher(id);
		if (matcher.matches() || id.equals("fabric-api-base") || id.equals("fabric-renderer-indigo")) {
			FABRIC_MODS.add(id);
			if (FabricLoader.getInstance().isModLoaded("fabric")) {
				Optional<ModContainer> parent = FabricLoader.getInstance().getModContainer("fabric");
				parent.ifPresent(modContainer -> ModMenu.PARENT_MAP.put(modContainer, mod));
			}
			if (!ModMenu.LIBRARY_MODS.containsKey(id)) {
				ModMenu.updateCacheLibraryValue(id, true);
			}
			if (id.equals("fabric-keybindings-v0") || id.equals("fabric-models-v0") || id.equals("fabric-renderer-api-v1") || id.equals("fabric-renderer-indigo") || id.equals("fabric-rendering-fluids-v1") || id.equals("fabric-rendering-v0") || id.equals("fabric-textures-v0")) {
				ModMenu.CLIENTSIDE_MODS.add(id);
			}
		}
		if (id.equals("fabricloader") || id.equals("fabric") || metadata.getName().endsWith(" API")) {
			if (!ModMenu.LIBRARY_MODS.containsKey(id)) {
				ModMenu.updateCacheLibraryValue(id, true);
			}
		}
	}

	public static String formatFabricModuleName(String name) {
		Matcher matcher = FABRIC_PATTERN.matcher(name);
		if (matcher.matches() || name.equals("fabric-renderer-indigo") || name.equals("fabric-api-base")) {
			if (matcher.matches()) {
				String v = matcher.group(1);
				name = WordUtils.capitalize(name.replace(v, "").replace("-", " "));
				name = name + " (" + v.replace("-", "") + ")";
			} else {
				name = WordUtils.capitalize(name.replace("-", " "));
			}
			name = name.replace("Api", "API");
			name = name.replace("Blockentity", "BlockEntity");
		}
		return name;
	}

	public static String getFabricModuleDescription(String id) {
		return FABRIC_DESCRIPTIONS.getOrDefault(id, "");
	}

	public static Set<String> getFabricMods() {
		return FABRIC_MODS;
	}

	public static HashMap<String, String> getFabricDescriptions() {
		return FABRIC_DESCRIPTIONS;
	}
}
