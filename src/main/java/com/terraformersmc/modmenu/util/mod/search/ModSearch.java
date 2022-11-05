package com.terraformersmc.modmenu.util.mod.search;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;

public class ModSearch {
	private final ModsScreen screen;
	private final ModListWidget modList;

	private SearchQuery query;

	public ModSearch(ModsScreen screen, ModListWidget modList, TextFieldWidget searchBox) {
		this.screen = screen;
		this.modList = modList;

		this.query = SearchQuery.parse("", this.screen);

		searchBox.setChangedListener(this::updateSearch);
		searchBox.setRenderTextProvider(this::provideRenderText);
	}

	private void updateSearch(String string) {
		this.query = SearchQuery.parse(string, this.screen);
		this.modList.refreshEntries();
	}

	private OrderedText provideRenderText(String original, int firstCharacterIndex) {
		return this.query.provideRenderText(firstCharacterIndex, original.length());
	}

	private boolean matches(Mod mod) {
		if (this.query.matches(mod)) {
			return true;
		}

		// Allow parent to pass filter if a child passes
		if (ModMenu.PARENT_MAP.keySet().contains(mod)) {
			for (Mod child : ModMenu.PARENT_MAP.get(mod)) {
				if (this.query.matches(child)) {
					return true;
				}
			}
		}

		return false;
	}

	public List<Mod> getResults(Collection<Mod> mods) {
		return mods.stream().filter(this::matches).collect(Collectors.toList());
	}
}
