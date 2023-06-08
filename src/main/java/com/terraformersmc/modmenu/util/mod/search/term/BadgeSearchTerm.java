package com.terraformersmc.modmenu.util.mod.search.term;

import com.terraformersmc.modmenu.util.mod.Mod;

import net.minecraft.text.TextColor;

public class BadgeSearchTerm extends SearchTerm {
	private final Mod.Badge badge;

	public BadgeSearchTerm(TermData data, Mod.Badge badge) {
		super(data);
		this.badge = badge;
	}

	@Override
	public boolean matches(Mod mod) {
		return mod.getBadges().contains(this.badge);
	}

	@Override
	public TextColor getColor() {
		return this.badge.getSearchColor();
	}
}
