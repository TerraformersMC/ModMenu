package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.resource.language.I18n;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ModSearch {

	public static boolean validSearchQuery(String query) {
		return query != null && !query.isEmpty();
	}

	public static List<Mod> search(ModsScreen screen, String query, List<Mod> candidates) {
		if (!validSearchQuery(query)) {
			return candidates;
		}
		return candidates.stream()
				.filter(modContainer -> passesFilters(screen, modContainer, query.toLowerCase(Locale.ROOT)))
				.collect(Collectors.toList());
	}

	private static boolean passesFilters(ModsScreen screen, Mod mod, String query) {
		String modId = mod.getId();
		String modDescription = mod.getDescription();
		String modSummary = mod.getSummary();

		String library = I18n.translate("modmenu.searchTerms.library");
		String patchwork = I18n.translate("modmenu.searchTerms.patchwork");
		String deprecated = I18n.translate("modmenu.searchTerms.deprecated");
		String clientside = I18n.translate("modmenu.searchTerms.clientside");
		String configurable = I18n.translate("modmenu.searchTerms.configurable");

		// Some basic search, could do with something more advanced but this will do for now
		if (mod.getName().toLowerCase(Locale.ROOT).contains(query) // Search mod name
				|| modId.toLowerCase(Locale.ROOT).contains(query) // Search mod ID
				|| modDescription.toLowerCase(Locale.ROOT).contains(query) // Search mod description
				|| modSummary.toLowerCase(Locale.ROOT).contains(query) // Search mod summary
				|| authorMatches(mod, query) // Search via author
				|| library.contains(query) && mod.getBadges().contains(Mod.Badge.LIBRARY) // Search for lib mods
				|| patchwork.contains(query) && mod.getBadges().contains(Mod.Badge.PATCHWORK_FORGE) // Search for patchwork mods
				|| deprecated.contains(query) && mod.getBadges().contains(Mod.Badge.DEPRECATED) // Search for deprecated mods
				|| clientside.contains(query) && mod.getBadges().contains(Mod.Badge.CLIENT) // Search for clientside mods
				|| configurable.contains(query) && screen.getModHasConfigScreen().get(modId) // Search for mods that can be configured
		) {
			return true;
		}

		// Allow parent to pass filter if a child passes
		if (ModMenu.PARENT_MAP.keySet().contains(mod)) {
			for (Mod child : ModMenu.PARENT_MAP.get(mod)) {
				if (passesFilters(screen, child, query)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean authorMatches(Mod mod, String query) {
		return mod.getAuthors().stream()
				.map(s -> s.toLowerCase(Locale.ROOT))
				.anyMatch(s -> s.contains(query.toLowerCase(Locale.ROOT)));
	}

}
