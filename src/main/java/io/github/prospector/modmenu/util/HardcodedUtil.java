package io.github.prospector.modmenu.util;

import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.text.WordUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HardcodedUtil {
	private static final Pattern FABRIC_PATTERN = Pattern.compile("^fabric-.*(-v\\d+)$");
	private static final Set<String> FABRIC_MODS = new HashSet<>();
	private static final HashMap<String, String> HARDCODED_DESCRIPTIONS = new HashMap<>();
	private static final HashMap<Predicate<ModMetadata>, Path> HARDCODED_ICONS = new HashMap<>();

	public static void initializeHardcodings() {
		FABRIC_MODS.add("fabric");
		FABRIC_MODS.add("fabricloader");
		HARDCODED_DESCRIPTIONS.put("minecraft", "The base game.");
		HARDCODED_DESCRIPTIONS.put("java", "The Java Runtime Environment");
		ModContainer modMenu = FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID).orElseThrow(IllegalAccessError::new);
		HARDCODED_ICONS.put(mod -> getFabricMods().contains(mod.getId()), modMenu.getPath("assets/" + ModMenu.MOD_ID + "/fabric_icon.png"));
		HARDCODED_ICONS.put(mod -> mod.getId().equals("minecraft"), modMenu.getPath("assets/" + ModMenu.MOD_ID + "/mc_icon.png"));
		HARDCODED_ICONS.put(mod -> mod.getId().equals("java"), modMenu.getPath("assets/" + ModMenu.MOD_ID + "/duke_icon.png"));
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
		if (id.equals("java")) {
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

	public static boolean modHasHardcodedIcon(ModMetadata mod) {
		return HARDCODED_ICONS.keySet().stream().anyMatch(predicate -> predicate.test(mod));
	}

	public static Path getHardcodedIcon(ModMetadata mod) {
		AtomicReference<Path> result = new AtomicReference<>();
		HARDCODED_ICONS.forEach((predicate, path) -> {
			if (predicate.test(mod)) result.set(path);
		});
		return result.get();
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
