package io.github.prospector.modmenu.gui;

import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.menu.AlwaysSelectedItemListWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.TranslatableTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class ModListWidget extends AlwaysSelectedItemListWidget<ModListItem> {
	private static final Logger LOGGER = LogManager.getLogger();

	private List<ModContainer> modContainerList = null;
	public ModListScreen parent;

	public ModListWidget(MinecraftClient client,
	                     int width,
	                     int height,
	                     int y1,
	                     int y2,
	                     int entryHeight,
	                     Supplier<String> searchTerm, ModListWidget list, ModListScreen parent) {
		super(client, width, height, y1, y2, entryHeight);
		this.parent = parent;
		if (list != null) {
			this.modContainerList = list.modContainerList;
		}
		this.filter(searchTerm, false);
		if (parent.selected != null && !children().isEmpty()) {
			for (ModListItem item : children()) {
				if (item.metadata.equals(parent.selected.metadata)) {
					selectItem(item);
				}
			}
		} else {
			if (getSelectedItem() == null && getItem(0) != null) {
				selectItem(getItem(0));
			}
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
	}

	@Override
	protected boolean isFocused() {
		return parent.getFocused() == this;
	}

	public void select(ModListItem modItem) {
		this.selectItem(modItem);
		if (modItem != null) {
			ModMetadata metadata = modItem.metadata;
			NarratorManager.INSTANCE.method_19788(new TranslatableTextComponent("narrator.select", metadata.getName()).getString());
		}

	}

	@Override
	public void selectItem(ModListItem itemListWidget$Item_1) {
		super.selectItem(itemListWidget$Item_1);
		parent.selected = getSelectedItem();
	}

	@Override
	public int getItemWidth() {
		return this.width - 8;
	}

	public void filter(Supplier<String> searchTerm, boolean var2) {
		this.clearItems();
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
		if (this.modContainerList == null || var2) {
			this.modContainerList = new ArrayList<>();
			modContainerList.addAll(mods);
			this.modContainerList.sort(Comparator.comparing(modContainer -> modContainer.getMetadata().getName()));
		}

		String term = searchTerm.get().toLowerCase(Locale.ROOT);
		for (ModContainer container : this.modContainerList) {
			ModMetadata metadata = container.getMetadata();
			Boolean api = ModMenu.MOD_API.get(metadata.getId());
			if (api == null) {
				api = metadata.getId().equals("fabricloader") || metadata.getId().equals("fabric") || metadata.getName().endsWith(" API");
			}
			if (metadata.getName().toLowerCase(Locale.ROOT).contains(term) || metadata.getId().toLowerCase(Locale.ROOT).contains(term) || metadata.getAuthors().stream().anyMatch(person -> person.getName().toLowerCase(Locale.ROOT).contains(term)) || (api && "api".contains(term)) || ("clientside".contains(term) && ModMenu.MOD_CLIENTSIDE.get(metadata.getId()) != null && ModMenu.MOD_CLIENTSIDE.get(metadata.getId()))) {
				this.addItem(new ModListItem(container, this));
			}
		}
	}

	@Override
	protected int getScrollbarPosition() {
		return width - 6;
	}

	public int getWidth() {
		return width;
	}

	public int getTop() {
		return this.top;
	}

}
