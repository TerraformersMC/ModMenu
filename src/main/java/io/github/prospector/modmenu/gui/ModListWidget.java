package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class ModListWidget extends EntryListWidget {
	private static final Logger LOGGER = LogManager.getLogger();

	private List<ModContainer> modInfoList = null;
	public ModEntryWidget selected;
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
		this.selected = null;
		this.searchFilter(searchTerm, false);
		this.screen = screen;
	}

	@Override
	public void draw(int i, int i1, float v) {
		super.draw(i, i1, v);
		if (selected != null) {
			ModMetadata metadata = selected.info;
			int x = width + 8;
			int y = y1;
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.client.getTextureManager().bindTexture(selected.nativeImageBackedTexture != null ? selected.iconLocation : ModEntryWidget.unknownIcon);
			GlStateManager.enableBlend();
			DrawableHelper.drawTexturedRect(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
			GlStateManager.disableBlend();
			int lineSpacing = client.textRenderer.fontHeight + 1;
			int imageOffset = 36;
			this.client.textRenderer.draw(metadata.getName(), x + imageOffset, y, 0xFFFFFF);
			this.client.textRenderer.draw(" (ID: " + metadata.getId() + ")", x + imageOffset + client.textRenderer.getStringWidth(metadata.getName()), y, 0xAAAAAA);
			this.client.textRenderer.draw("v" + metadata.getVersion().getFriendlyString(), x + imageOffset, y + lineSpacing, 0xAAAAAA);
			//			if (metadata.getLinks().getHomepage() != null && !metadata.getLinks().getHomepage().isEmpty()) {
			//				this.client.textRenderer.draw(TextFormat.BLUE + "" + TextFormat.UNDERLINE + metadata.getLinks().getHomepage(), x + imageOffset, y + lineSpacing * 2, 0);
			//			}
			y = y1 + imageOffset + 24;
			if (metadata.getDescription() != null && !metadata.getDescription().isEmpty()) {
				RenderUtils.drawWrappedString(metadata.getDescription(), x, y, screen.screenWidth - this.width - 20, 5, 0xAAAAAA);
			}
		}
	}

	@Override
	public int getEntryWidth() {
		return this.width - 8;
	}

	public void searchFilter(Supplier<String> searchTerm, boolean var2) {
		this.clearEntries();
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

			this.addEntry(new ModEntryWidget(container, this));
		}
	}

	@Override
	protected int getScrollbarPosition() {
		return width - 6;
	}

	public int getWidth() {
		return width;
	}

	public int getY1() {
		return this.y1;
	}

	public int getY2() {
		return this.y2;
	}
}
