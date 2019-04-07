package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.menu.AlwaysSelectedItemListWidget;
import net.minecraft.client.resource.language.I18n;
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
		if (list != null) {
			this.modContainerList = list.modContainerList;
		}
		this.filter(searchTerm, false);
		this.parent = parent;
	}

	@Override
	public void render(int i, int i1, float v) {
		super.render(i, i1, v);
		ModItem selected = getSelectedItem();
		if (selected != null) {
			ModMetadata metadata = selected.metadata;
			int x = width + 8;
			int y = this.top;
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.client.getTextureManager().bindTexture(selected.icon != null ? selected.iconLocation : ModItem.unknownIcon);
			GlStateManager.enableBlend();
			blit(x, y, 0.0F, 0.0F, 32, 32, 32, 32);
			GlStateManager.disableBlend();
			int lineSpacing = client.textRenderer.fontHeight + 1;
			int imageOffset = 36;
			this.client.textRenderer.draw(metadata.getName(), x + imageOffset, y, 0xFFFFFF);
			if (i > x && i1 > y && i1 < y + imageOffset && i < parent.width)
				parent.setTooltip(I18n.translate("modmenu.modIdToolTip", metadata.getId()));
			this.client.textRenderer.draw("v" + metadata.getVersion().getFriendlyString(), x + imageOffset, y + lineSpacing, 0xAAAAAA);
		}
	}

	@Override
	protected boolean isFocused() {
		return parent.getFocused() == this;
	}

	public void method_20157(ModItem modItem) {
		this.selectItem(modItem);
		if (modItem != null) {
			ModMetadata metadata = modItem.metadata;
			NarratorManager.INSTANCE.method_19788(new TranslatableTextComponent("narrator.select", metadata.getName()).getString());
		}

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

	public int getY() {
		return this.top;
	}

}
