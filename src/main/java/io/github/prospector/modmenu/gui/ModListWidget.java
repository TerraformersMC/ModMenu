package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.gui.entries.ChildEntry;
import io.github.prospector.modmenu.gui.entries.IndependentEntry;
import io.github.prospector.modmenu.gui.entries.ParentEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class ModListWidget extends AlwaysSelectedEntryListWidget<ModListEntry> {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ModListScreen parent;
	private List<ModContainer> modContainerList = null;
	private String selectedModId = null;
	private boolean scrolling;

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
		int denominator = Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
		if (denominator <= 0) {
			parent.updateScrollPercent(0);
		} else {
			parent.updateScrollPercent(getScrollAmount() / Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
		}
	}

	@Override
	protected boolean isFocused() {
		return parent.getFocused() == this;
	}

	public void select(ModListEntry entry) {
		this.setSelected(entry);
		if (entry != null) {
			ModMetadata metadata = entry.getMetadata();
			NarratorManager.INSTANCE.narrate(new TranslatableText("narrator.select", metadata.getName()).getString());
		}
	}

	@Override
	public void setSelected(ModListEntry entry) {
		super.setSelected(entry);
		selectedModId = entry.getMetadata().getId();
		parent.updateSelectedEntry(getSelected());
	}

	@Override
	protected boolean isSelectedItem(int index) {
		ModListEntry selected = getSelected();
		return selected != null && selected.getMetadata().getId().equals(getEntry(index).getMetadata().getId());
	}

	@Override
	public int addEntry(ModListEntry entry) {
		int i = super.addEntry(entry);
		if (entry.getMetadata().getId().equals(selectedModId)) {
			setSelected(entry);
		}
		return i;
	}

	public void reloadFilter() {
		filter(parent.getSearchInput(), false);
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
			String id = metadata.getId();
			if (passesFilter(container, term)) {
				if (!ModMenu.PARENT_MAP.values().contains(container)) {
					if (ModMenu.PARENT_MAP.keySet().contains(container)) {
						List<ModContainer> children = ModMenu.PARENT_MAP.get(container);
						ParentEntry parent = new ParentEntry(container, children, this);
						this.addEntry(parent);
						if (this.parent.showModChildren.contains(id)) {
							List<ModContainer> passed = new ArrayList<>();
							for (ModContainer child : children) {
								if (passesFilter(child, term)) {
									passed.add(child);
								}
							}
							for (ModContainer child : passed) {
								this.addEntry(new ChildEntry(child, parent, this, passed.indexOf(child) == passed.size() - 1));
							}
						}
					} else {
						this.addEntry(new IndependentEntry(container, this));
					}
				}
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

	public boolean passesFilter(ModContainer container, String term) {
		ModMetadata metadata = container.getMetadata();
		String id = metadata.getId();
		boolean library = false;
		if (ModMenu.LIBRARY_MODS.get(id) != null) {
			library = ModMenu.LIBRARY_MODS.get(id);
		}
		boolean childPasses = ModMenu.PARENT_MAP.containsKey(container) && ModMenu.PARENT_MAP.get(container).stream().anyMatch(modContainer -> passesFilter(modContainer, term));
		if (childPasses) {
			return true;
		}
		if (library && !ModMenuConfigManager.getConfig().showLibraries()) {
			return false;
		}
		boolean clientside = ModMenu.CLIENTSIDE_MODS.contains(id);
		return metadata.getName().toLowerCase(Locale.ROOT).contains(term) || id.toLowerCase(Locale.ROOT).contains(term) || metadata.getAuthors().stream().anyMatch(person -> person.getName().toLowerCase(Locale.ROOT).contains(term)) || (library && "api library".contains(term)) || ("clientside".contains(term) && clientside) || ("configurations configs configures configurable".contains(term) && ModMenu.hasFactory(id));
	}

	@Override
	protected void renderList(int x, int y, int mouseX, int mouseY, float delta) {
		int itemCount = this.getItemCount();
		Tessellator tessellator_1 = Tessellator.getInstance();
		BufferBuilder buffer = tessellator_1.getBufferBuilder();

		for (int index = 0; index < itemCount; ++index) {
			int entryTop = this.getRowTop(index) + 2;
			int entryBottom = this.getRowTop(index) + this.itemHeight;
			if (entryBottom >= this.top && entryTop <= this.bottom) {
				int entryHeight = this.itemHeight - 4;
				ModListEntry entry = this.getEntry(index);
				int rowWidth = this.getRowWidth();
				int entryLeft;
				if (this.renderSelection && this.isSelectedItem(index)) {
					entryLeft = getRowLeft() - 2 + entry.getXOffset();
					int selectionRight = x + rowWidth + 2;
					GlStateManager.disableTexture();
					float float_2 = this.isFocused() ? 1.0F : 0.5F;
					GlStateManager.color4f(float_2, float_2, float_2, 1.0F);
					buffer.begin(7, VertexFormats.POSITION);
					buffer.vertex((double) entryLeft, (double) (entryTop + entryHeight + 2), 0.0D).next();
					buffer.vertex((double) selectionRight, (double) (entryTop + entryHeight + 2), 0.0D).next();
					buffer.vertex((double) selectionRight, (double) (entryTop - 2), 0.0D).next();
					buffer.vertex((double) entryLeft, (double) (entryTop - 2), 0.0D).next();
					tessellator_1.draw();
					GlStateManager.color4f(0.0F, 0.0F, 0.0F, 1.0F);
					buffer.begin(7, VertexFormats.POSITION);
					buffer.vertex((double) (entryLeft + 1), (double) (entryTop + entryHeight + 1), 0.0D).next();
					buffer.vertex((double) (selectionRight - 1), (double) (entryTop + entryHeight + 1), 0.0D).next();
					buffer.vertex((double) (selectionRight - 1), (double) (entryTop - 1), 0.0D).next();
					buffer.vertex((double) (entryLeft + 1), (double) (entryTop - 1), 0.0D).next();
					tessellator_1.draw();
					GlStateManager.enableTexture();
				}

				entryLeft = this.getRowLeft();
				entry.render(index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, this.isMouseOver((double) mouseX, (double) mouseY) && Objects.equals(this.getEntryAtPos((double) mouseX, (double) mouseY), entry), delta);
			}
		}

	}

	@Override
	protected void updateScrollingState(double double_1, double double_2, int int_1) {
		super.updateScrollingState(double_1, double_2, int_1);
		this.scrolling = int_1 == 0 && double_1 >= (double) this.getScrollbarPosition() && double_1 < (double) (this.getScrollbarPosition() + 6);
	}

	@Override
	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		this.updateScrollingState(double_1, double_2, int_1);
		if (!this.isMouseOver(double_1, double_2)) {
			return false;
		} else {
			ModListEntry entry = this.getEntryAtPos(double_1, double_2);
			if (entry != null) {
				if (entry.mouseClicked(double_1, double_2, int_1)) {
					this.setFocused(entry);
					this.setDragging(true);
					return true;
				}
			} else if (int_1 == 0) {
				this.clickedHeader((int) (double_1 - (double) (this.left + this.width / 2 - this.getRowWidth() / 2)), (int) (double_2 - (double) this.top) + (int) this.getScrollAmount() - 4);
				return true;
			}

			return this.scrolling;
		}
	}

	public final ModListEntry getEntryAtPos(double x, double y) {
		int int_5 = MathHelper.floor(y - (double) this.top) - this.headerHeight + (int) this.getScrollAmount() - 4;
		int index = int_5 / this.itemHeight;
		return x < (double) this.getScrollbarPosition() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getItemCount() ? this.children().get(index) : null;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - (Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)) > 0 ? 18 : 12);
	}

	@Override
	protected int getRowLeft() {
		return left + 6;
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

	@Override
	protected int getMaxPosition() {
		return super.getMaxPosition() + 4;
	}

	public int getDisplayedCount() {
		int count = 0;
		for (Entry entry : children()) {
			count++;
			if (entry instanceof ParentEntry) {
				count += ((ParentEntry) entry).getChildren().size();
			}
		}
		return count;
	}
}
