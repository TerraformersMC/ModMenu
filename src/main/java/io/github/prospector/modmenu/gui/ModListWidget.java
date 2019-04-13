package io.github.prospector.modmenu.gui;

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

public class ModListWidget extends AlwaysSelectedItemListWidget<ModItem> {
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
			for (ModItem item : children()) {
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

	public void select(ModItem modItem) {
		this.selectItem(modItem);
		if (modItem != null) {
			ModMetadata metadata = modItem.metadata;
			NarratorManager.INSTANCE.method_19788(new TranslatableTextComponent("narrator.select", metadata.getName()).getString());
		}

	}

	@Override
	public void selectItem(ModItem itemListWidget$Item_1) {
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
		Iterator<ModContainer> iter = this.modContainerList.iterator();

		while (true) {
			ModContainer container;
			ModMetadata metadata;

			do {
				if (!iter.hasNext()) {
					return;
				}
				container = iter.next();
				metadata = container.getMetadata();
			} while (!metadata.getName().toLowerCase(Locale.ROOT).contains(term) && !metadata.getId().toLowerCase(Locale.ROOT).contains(term));
			this.addItem(new ModItem(container, this));
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
