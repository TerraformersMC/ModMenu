package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.menu.options.LanguageOptionsScreen;
import net.minecraft.client.gui.widget.ItemListWidget;
import net.minecraft.client.resource.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class ModListWidget extends ItemListWidget<ModItemWidget> {
	private static final Logger LOGGER = LogManager.getLogger();

	private List<ModContainer> modInfoList = null;
	public ModListScreen screen;

	public ModListWidget(MinecraftClient client,
	                     int width,
	                     int height,
	                     int y1,
	                     int y2,
	                     int entryHeight,
	                     Supplier<String> searchTerm, ModListWidget list, ModListScreen screen) {
		super(client, width, height, y1, y2, entryHeight);
		if (list != null) {
			this.modInfoList = list.modInfoList;
		}
		this.searchFilter(searchTerm, false);
		this.screen = screen;
	}

	@Override
	public void render(int i, int i1, float v) {
		super.render(i, i1, v);
		ModItemWidget selected = getSelectedItem();
		if (selected != null) {
			ModMetadata metadata = selected.info;
			int x = width + 8;
			int y = this.top;
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.client.getTextureManager().bindTexture(selected.nativeImageBackedTexture != null ? selected.iconLocation : ModItemWidget.unknownIcon);
			GlStateManager.enableBlend();
			blit(x, y, 0.0F, 0.0F, 32, 32, 32, 32);
			GlStateManager.disableBlend();
			int lineSpacing = client.textRenderer.fontHeight + 1;
			int imageOffset = 36;
			this.client.textRenderer.draw(metadata.getName(), x + imageOffset, y, 0xFFFFFF);
			if (i > x && i1 > y && i1 < y + imageOffset && i < screen.width)
				screen.setTooltip(I18n.translate("modmenu.modIdToolTip", metadata.getId()));
			this.client.textRenderer.draw("v" + metadata.getVersion().getFriendlyString(), x + imageOffset, y + lineSpacing, 0xAAAAAA);
			//			if (metadata.getLinks().getHomepage() != null && !metadata.getLinks().getHomepage().isEmpty()) {
			//				this.client.textRenderer.draw(TextFormat.BLUE + "" + TextFormat.UNDERLINE + metadata.getLinks().getHomepage(), x + imageOffset, y + lineSpacing * 2, 0);
			//			}
			y = this.top + imageOffset + 24;
		}
	}

	@Override
	protected boolean isFocused() {
		return screen.getFocused() == this;
	}

	@Override
	public int getItemWidth() {
		return this.width - 8;
	}

	public void searchFilter(Supplier<String> searchTerm, boolean var2) {
		this.clearItems();
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
		if (this.modInfoList == null || var2) {
			this.modInfoList = new ArrayList<>();
			modInfoList.addAll(mods);
			this.modInfoList.sort(Comparator.comparing(modContainer -> modContainer.getMetadata().getName()));
		}

		String term = searchTerm.get().toLowerCase(Locale.ROOT);
		Iterator<ModContainer> iter = this.modInfoList.iterator();

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

			this.addItem(new ModItemWidget(container, this));
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
