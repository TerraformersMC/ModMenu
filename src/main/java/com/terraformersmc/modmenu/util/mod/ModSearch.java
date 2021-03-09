package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;

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

		// Some basic search, could do with something more advanced but this will do for now
		if (mod.getName().toLowerCase(Locale.ROOT).contains(query) // Search mod name
				|| modId.toLowerCase(Locale.ROOT).contains(query) // Search mod name
				|| authorMatches(mod, query) // Search via author
				|| (mod.getBadges().contains(Mod.Badge.LIBRARY) && "api library".contains(query)) // Search for lib mods
				|| ("patchwork forge".contains(query) && mod.getBadges().contains(Mod.Badge.PATCHWORK_FORGE)) // Search for patchwork mods
				|| ("deprecated".contains(query) && mod.getBadges().contains(Mod.Badge.DEPRECATED)) // Search for deprecated mods
				|| ("clientside client-side".contains(query) && mod.getBadges().contains(Mod.Badge.CLIENT)) // Search for clientside mods
				|| ("configurations configs configures configurable".contains(query) && screen.getConfigScreenCache().get(modId) != null) // Search for mods that can be configured
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
