package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.menu.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.TranslatableTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class ModListWidget extends AlwaysSelectedEntryListWidget<ModListEntry> {
	private static final Logger LOGGER = LogManager.getLogger();
	private List<ModContainer> modContainerList = null;
	private final ModListScreen parent;

	public ModListWidget(MinecraftClient client, int width, int height, int y1, int y2, int entryHeight, Supplier<String> searchTerm, ModListWidget list, ModListScreen parent) {
		super(client, width, height, y1, y2, entryHeight);
		this.parent = parent;
		if (list != null) {
			this.modContainerList = list.modContainerList;
		}
		this.filter(searchTerm, false);
		setScrollAmount(parent.getScrollPercent() * Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
	}

	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		parent.updateScrollPercent(getScrollAmount() / Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
	}

	@Override
	protected boolean isFocused() {
		return parent.getFocused() == this;
	}

	public void select(ModListEntry entry) {
		this.setSelected(entry);
		if (entry != null) {
			ModMetadata metadata = entry.getMetadata();
			NarratorManager.INSTANCE.method_19788(new TranslatableTextComponent("narrator.select", metadata.getName()).getString());
		}
	}

	@Override
	public void setSelected(ModListEntry entry) {
		super.setSelected(entry);
		parent.updateSelectedEntry(getSelected());
	}

	@Override
	public int getRowWidth() {
		return this.width - 6 - 4;
	}

	public void filter(Supplier<String> searchTerm, boolean var2) {
		this.clearEntries();
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
				this.addEntry(new ModListEntry(container, this));
			}
		}

		if (parent.getSelectedEntry() != null && !children().isEmpty() || this.getSelected() != null && getSelected().getMetadata() != parent.getSelectedEntry().getMetadata()) {
			for (ModListEntry entry : children()) {
				if (entry.getMetadata().equals(parent.getSelectedEntry().getMetadata())) {
					setSelected(entry);
				}
			}
		} else {
			if (getSelected() == null && getEntry(0) != null) {
				setSelected(getEntry(0));
			}
		}

		if (getScrollAmount() > Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4))) {
			setScrollAmount(Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
		}
	}

	@Override
	protected void renderList(int x, int y, int mouseX, int mouseY, float delta) {
		int itemCount = this.getItemCount();
		Tessellator tessellator_1 = Tessellator.getInstance();
		BufferBuilder buffer = tessellator_1.getBufferBuilder();

		for (int index = 0; index < itemCount; ++index) {
			int entryTop = this.getRowTop(index);
			int entryBottom = this.getRowTop(index) + this.itemHeight;
			if (entryBottom >= this.top && entryTop <= this.bottom) {
				int int_9 = y + index * this.itemHeight + this.headerHeight;
				int entryHeight = this.itemHeight - 4;
				ModListEntry entry = this.getEntry(index);
				int rowWidth = this.getRowWidth() - 14;
				int entryLeft;
				if (this.renderSelection && this.isSelectedItem(index)) {
					entryLeft = getRowLeft() - 2;
					int entryRight = this.left + this.width / 2 + rowWidth / 2;
					GlStateManager.disableTexture();
					float float_2 = this.isFocused() ? 1.0F : 0.5F;
					GlStateManager.color4f(float_2, float_2, float_2, 1.0F);
					buffer.begin(7, VertexFormats.POSITION);
					buffer.vertex((double) entryLeft, (double) (int_9 + entryHeight + 2), 0.0D).next();
					buffer.vertex((double) entryRight, (double) (int_9 + entryHeight + 2), 0.0D).next();
					buffer.vertex((double) entryRight, (double) (int_9 - 2), 0.0D).next();
					buffer.vertex((double) entryLeft, (double) (int_9 - 2), 0.0D).next();
					tessellator_1.draw();
					GlStateManager.color4f(0.0F, 0.0F, 0.0F, 1.0F);
					buffer.begin(7, VertexFormats.POSITION);
					buffer.vertex((double) (entryLeft + 1), (double) (int_9 + entryHeight + 1), 0.0D).next();
					buffer.vertex((double) (entryRight - 1), (double) (int_9 + entryHeight + 1), 0.0D).next();
					buffer.vertex((double) (entryRight - 1), (double) (int_9 - 1), 0.0D).next();
					buffer.vertex((double) (entryLeft + 1), (double) (int_9 - 1), 0.0D).next();
					tessellator_1.draw();
					GlStateManager.enableTexture();
				}

				entryLeft = this.getRowLeft();
				entry.render(index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, this.isMouseOver((double) mouseX, (double) mouseY) && Objects.equals(this.getEntryAtPosition((double) mouseX, (double) mouseY), entry), delta);
			}
		}

	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6;
	}

	@Override
	protected int getRowLeft() {
		return left + 4;
	}

	public int getWidth() {
		return width;
	}

	public int getTop() {
		return this.top;
	}

	public ModListScreen getParent() {
		return parent;
	}
}
