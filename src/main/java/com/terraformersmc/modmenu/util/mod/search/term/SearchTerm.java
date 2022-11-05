package com.terraformersmc.modmenu.util.mod.search.term;

import org.jetbrains.annotations.Nullable;

import com.terraformersmc.modmenu.util.mod.Mod;

import net.minecraft.text.Style;
import net.minecraft.text.TextColor;

public abstract class SearchTerm {
	private final TermData data;

	public SearchTerm(TermData data) {
		this.data = data;
	}

	public TermData getData() {
		return this.data;
	}

	public abstract boolean matches(Mod mod);

	@Nullable
	public abstract TextColor getColor();

	public final Style getStyle() {
		return Style.EMPTY.withColor(this.getColor());
	}
}
