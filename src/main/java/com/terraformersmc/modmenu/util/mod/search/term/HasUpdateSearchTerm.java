package com.terraformersmc.modmenu.util.mod.search.term;

import com.terraformersmc.modmenu.util.mod.Mod;

import net.minecraft.text.TextColor;

public class HasUpdateSearchTerm extends SearchTerm {
	private static final TextColor COLOR = TextColor.fromRgb(0xaad865);

	public HasUpdateSearchTerm(TermData data) {
		super(data);
	}

	@Override
	public boolean matches(Mod mod) {
		return mod.getModrinthData() != null;
	}

	@Override
	public TextColor getColor() {
		return COLOR;
	}
}
