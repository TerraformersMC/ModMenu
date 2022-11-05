package com.terraformersmc.modmenu.util.mod.search.term;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.mod.Mod;

import net.minecraft.text.TextColor;

public class ConfigurableSearchTerm extends SearchTerm {
	private static final TextColor COLOR = TextColor.fromRgb(0xd4955e);

	private final ModsScreen screen;

	public ConfigurableSearchTerm(TermData data, ModsScreen screen) {
		super(data);
		this.screen = screen;
	}

	@Override
	public boolean matches(Mod mod) {
		return screen.getModHasConfigScreen().get(mod.getId());
	}

	@Override
	public TextColor getColor() {
		return COLOR;
	}
}
