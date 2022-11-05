package com.terraformersmc.modmenu.util.mod.search.term;

import java.util.Locale;

import com.terraformersmc.modmenu.util.mod.Mod;

import net.minecraft.text.TextColor;

public class ContentSearchTerm extends SearchTerm {
	private final String content;

	public ContentSearchTerm(TermData data) {
		super(data);
		this.content = data.getContent().toLowerCase(Locale.ROOT);
	}

	@Override
	public boolean matches(Mod mod) {
		String modName = mod.getName().toLowerCase(Locale.ROOT);
		String modId = mod.getId().toLowerCase(Locale.ROOT);
		String modDescription = mod.getDescription().toLowerCase(Locale.ROOT);
		String modSummary = mod.getSummary().toLowerCase(Locale.ROOT);

		return modName.contains(this.content) // Search mod name
			|| modId.contains(this.content) // Search mod ID
			|| modDescription.contains(this.content) // Search mod description
			|| modSummary.contains(this.content) // Search mod summary
			|| ContentSearchTerm.authorMatches(mod, this.content); // Search via author
	}

	@Override
	public TextColor getColor() {
		return null;
	}

	private static boolean authorMatches(Mod mod, String query) {
		return mod.getAuthors().stream()
				.map(s -> s.toLowerCase(Locale.ROOT))
				.anyMatch(s -> s.contains(query));
	}
}
