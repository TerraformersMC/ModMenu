package io.github.prospector.modmenu.util;

import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.text.WordUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HardcodedUtil {
	private static final Pattern FABRIC_PATTERN = Pattern.compile("^fabric-.*(-v\\d+)$");
	private static final Set<String> FABRIC_MODS = new HashSet<>();
	private static final HashMap<String, String> HARDCODED_DESCRIPTIONS = new HashMap<>();

	public static void initializeHardcodings() {
		FABRIC_MODS.add("fabric");
		FABRIC_MODS.add("fabricloader");
		HARDCODED_DESCRIPTIONS.put("fabric-api-base", "Contains the essentials for Fabric API modules.");
		HARDCODED_DESCRIPTIONS.put("fabric-biomes-v1", "Hooks for adding biomes to the default world generator.");
		HARDCODED_DESCRIPTIONS.put("fabric-commands-v0", "Adds command-related hooks.");
		HARDCODED_DESCRIPTIONS.put("fabric-containers-v0", "Adds hooks for containers.");
		HARDCODED_DESCRIPTIONS.put("fabric-content-registries-v0", "Adds registries for vanilla mechanics that are missing them.");
		HARDCODED_DESCRIPTIONS.put("fabric-crash-report-info-v1", "Adds Fabric-related debug info to crash reports.");
		HARDCODED_DESCRIPTIONS.put("fabric-events-interaction-v0", "Events for player interaction with blocks and entities.");
		HARDCODED_DESCRIPTIONS.put("fabric-events-lifecycle-v0", "Events for the game's lifecycle.");
		HARDCODED_DESCRIPTIONS.put("fabric-item-groups-v0", "An API for adding custom item groups.");
		HARDCODED_DESCRIPTIONS.put("fabric-keybindings-v0", "Keybinding registry API.");
		HARDCODED_DESCRIPTIONS.put("fabric-loot-tables-v1", "Hooks for manipulating loot tables.");
		HARDCODED_DESCRIPTIONS.put("fabric-mining-levels-v0", "Block mining level tags for tools.");
		HARDCODED_DESCRIPTIONS.put("fabric-models-v0", "Hooks for models and model loading.");
		HARDCODED_DESCRIPTIONS.put("fabric-networking-blockentity-v0", "Networking hooks for block entities.");
		HARDCODED_DESCRIPTIONS.put("fabric-networking-v0", "Networking packet hooks and registries.");
		HARDCODED_DESCRIPTIONS.put("fabric-object-builders-v0", "Builders for objects vanilla has locked down.");
		HARDCODED_DESCRIPTIONS.put("fabric-registry-sync-v0", "Syncs registry mappings.");
		HARDCODED_DESCRIPTIONS.put("fabric-renderer-api-v1", "Defines rendering extensions for dynamic/fancy block and item models.");
		HARDCODED_DESCRIPTIONS.put("fabric-renderer-indigo", "Default implementation of the Fabric Renderer API.");
		HARDCODED_DESCRIPTIONS.put("fabric-rendering-data-attachment-v1", "Thread-safe hooks for BlockEntity data use during terrain rendering.");
		HARDCODED_DESCRIPTIONS.put("fabric-rendering-fluids-v1", "Hooks for registering fluid renders.");
		HARDCODED_DESCRIPTIONS.put("fabric-rendering-v0", "Hooks and registries for rendering-related things");
		HARDCODED_DESCRIPTIONS.put("fabric-resource-loader-v0", "Asset and data resource loading.");
		HARDCODED_DESCRIPTIONS.put("fabric-tag-extensions-v0", "Hooks for tags.");
		HARDCODED_DESCRIPTIONS.put("fabric-textures-v0", "Hooks for texture loading and registration.");
		HARDCODED_DESCRIPTIONS.put("minecraft", "The base game.");
	}

	public static void hardcodeModuleMetadata(ModContainer mod, ModMetadata metadata, String id) {
		Matcher matcher = FABRIC_PATTERN.matcher(id);
		if (matcher.matches() || id.equals("fabric-api-base") || id.equals("fabric-renderer-indigo")) {
			FABRIC_MODS.add(id);
			if (FabricLoader.getInstance().isModLoaded("fabric")) {
				Optional<ModContainer> parent = FabricLoader.getInstance().getModContainer("fabric");
				parent.ifPresent(modContainer -> ModMenu.PARENT_MAP.put(modContainer, mod));
			}
			ModMenu.addLibraryMod(id);
			if (id.equals("fabric-keybindings-v0") || id.equals("fabric-models-v0") || id.equals("fabric-renderer-api-v1") || id.equals("fabric-renderer-indigo") || id.equals("fabric-rendering-fluids-v1") || id.equals("fabric-rendering-v0") || id.equals("fabric-textures-v0")) {
				ModMenu.CLIENTSIDE_MODS.add(id);
			}
		}
		if (id.equals("fabricloader") || id.equals("fabric") || metadata.getName().endsWith(" API")) {
			ModMenu.addLibraryMod(id);
		}
	}

	public static Text formatFabricModuleName(String name) {
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
		return new LiteralText(name);
	}

	public static String getHardcodedDescription(String id) {
		return HARDCODED_DESCRIPTIONS.getOrDefault(id, "");
	}

	public static Set<String> getFabricMods() {
		return FABRIC_MODS;
	}

	public static HashMap<String, String> getHardcodedDescriptions() {
		return HARDCODED_DESCRIPTIONS;
	}
}
