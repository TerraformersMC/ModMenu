package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ChildEntry;
import com.terraformersmc.modmenu.gui.widget.entries.IndependentEntry;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.gui.widget.entries.ParentEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModSearch;
import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ModListWidget extends AlwaysSelectedEntryListWidget<ModListEntry> implements AutoCloseable {
	public static final boolean DEBUG = Boolean.getBoolean("modmenu.debug");
	private final ModsScreen parent;
	private List<Mod> mods = null;
	private final Set<Mod> addedMods = new HashSet<>();
	private String selectedModId = null;
	//private boolean scrolling;
	private final FabricIconHandler iconHandler = new FabricIconHandler();
	private Double restoreScrollY = null;

	public ModListWidget(
		MinecraftClient client,
		int width,
		int height,
		int y,
		int itemHeight,
		ModListWidget list,
		ModsScreen parent
	) {
		super(client, width, height, y, itemHeight);
		this.parent = parent;
		if (list != null) {
			this.mods = list.mods;
			this.restoreScrollY = list.getScrollY();
		}
	}

	@Override
	public void setScrollY(double amount) {
		super.setScrollY(amount);
		int denominator = Math.max(0, this.getContentsHeightWithPadding() - (this.getBottom() - this.getY() - 4));
		if (denominator == 0) {
			parent.updateScrollPercent(0);
		} else {
			parent.updateScrollPercent(getScrollY() / Math.max(0, this.getContentsHeightWithPadding() - (this.getBottom() - this.getY() - 4)));
		}
	}

	@Override
	public boolean isFocused() {
		return parent.getFocused() == this;
	}

	public void select(ModListEntry entry) {
		this.setSelected(entry);
		if (entry != null) {
			Mod mod = entry.getMod();
			this.client.getNarratorManager().narrate(Text.translatable("narrator.select", mod.getTranslatedName()));
		}
	}

	@Override
	public void setSelected(@Nullable ModListEntry entry) {
		super.setSelected(entry);
		if (entry == null) {
			selectedModId = null;
		} else {
			selectedModId = entry.getMod().getId();
		}

		parent.updateSelectedEntry(getSelectedOrNull());
	}

	protected boolean isSelectedEntry(int index) {
		ModListEntry selected = getSelectedOrNull();
		ModListEntry entry = this.getEntry(index);
		return selected != null && entry != null && selected.getMod().getId().equals(entry.getMod().getId());
	}

	@Override
	public int addEntry(ModListEntry entry) {
		if (addedMods.contains(entry.mod)) {
			return 0;
		}

		addedMods.add(entry.mod);
		int i = super.addEntry(entry);
		if (entry.getMod().getId().equals(selectedModId)) {
			setSelected(entry);
		}

		return i;
	}

	@Nullable
    public ModListEntry getEntry(int index) {
		if (this.children().size() > index) {
			return this.children().get(index);
		}

		return null;
    }

	@Override
	protected void removeEntry(ModListEntry entry) {
		addedMods.remove(entry.mod);
		super.removeEntry(entry);
	}

	@Override
    protected void clearEntries()
    {
		this.setSelected(null);
		addedMods.clear();
		super.clearEntries();
	}

	protected void remove(int index) {
		ModListEntry entry = this.children().get(index);
		addedMods.remove(entry.mod);
		super.removeEntry(entry);
	}

	public void finalizeInit() {
		reloadFilters();
		if (restoreScrollY != null) {
			setScrollY(restoreScrollY);
			restoreScrollY = null;
		}
	}

	public void reloadFilters() {
		filter(parent.getSearchInput(), true, false);
	}

	public void filter(String searchTerm, boolean refresh) {
		filter(searchTerm, refresh, true);
	}

	private boolean hasVisibleChildMods(Mod parent) {
		List<Mod> children = ModMenu.PARENT_MAP.get(parent);
		boolean hideLibraries = !ModMenuConfig.SHOW_LIBRARIES.getValue();
		return !children.stream().allMatch(child -> child.isHidden() || hideLibraries && child.getBadges().contains(Mod.Badge.LIBRARY));
	}

	public void filter(String searchTerm, boolean refresh, boolean reposition) {
		this.clearEntries();
		addedMods.clear();
		Collection<Mod> mods = ModMenu.MODS.values().stream().filter(mod -> {
			if (ModMenuConfig.CONFIG_MODE.getValue()) {
				return !parent.getModHasConfigScreen(mod.getId());
			} else {
				return !mod.isHidden();
			}
		}).collect(Collectors.toSet());

		if (DEBUG) {
			mods = new ArrayList<>(mods);
		}

		if (this.mods == null || refresh) {
			this.mods = new ArrayList<>();
			this.mods.addAll(mods);
			this.mods.sort(ModMenuConfig.SORTING.getValue().getComparator());
		}

		for (Mod mod : ModSearch.search(parent, searchTerm, this.mods)) {
			String modId = mod.getId();

			//Hide parent lib mods when the config is set to hide
			if (mod.getBadges().contains(Mod.Badge.LIBRARY) && !ModMenuConfig.SHOW_LIBRARIES.getValue()) {
				continue;
			}

			if (!ModMenu.PARENT_MAP.values().contains(mod)) {
				if (ModMenu.PARENT_MAP.keySet().contains(mod) && hasVisibleChildMods(mod)) {
					//Add parent mods when not searching
					List<Mod> children = ModMenu.PARENT_MAP.get(mod);
					children.sort(ModMenuConfig.SORTING.getValue().getComparator());
					ParentEntry parent = new ParentEntry(mod, children, this);
					this.addEntry(parent);
					//Add children if they are meant to be shown
					if (this.parent.showModChildren.contains(modId)) {
						List<Mod> validChildren = ModSearch.search(this.parent, searchTerm, children);
						for (Mod child : validChildren) {
							this.addEntry(new ChildEntry(child, parent, this, validChildren.indexOf(child) == validChildren.size() - 1));
						}
					}
				} else {
					//A mod with no children
					this.addEntry(new IndependentEntry(mod, this));
				}
			}
		}

        if (!reposition) {
            // This generally leaves the same mod selected, but no mod highlighted, and the scrolling is unmodified.
            return;
        }

		if (parent.getSelectedEntry() != null && !children().isEmpty() || this.getSelectedOrNull() != null && getSelectedOrNull().getMod() != parent.getSelectedEntry().getMod()) {
			for (ModListEntry entry : children()) {
				if (entry.getMod().equals(parent.getSelectedEntry().getMod())) {
					setSelected(entry);
				}
			}
		} else {
			if (getSelectedOrNull() == null && !children().isEmpty() && getEntry(0) != null) {
				setSelected(getEntry(0));
			}
		}

		if (getScrollY() > Math.max(0, this.getContentsHeightWithPadding() - (this.getBottom() - this.getY() - 4))) {
			setScrollY(Math.max(0, this.getContentsHeightWithPadding() - (this.getBottom() - this.getY() - 4)));
		}
	}

	@Override
	protected void renderList(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		int entryLeft = this.getRowLeft();
		int entryWidth = this.getRowWidth();
		int entryHeight = this.itemHeight - 4;
		int entryCount = this.getEntryCount();
		int x = this.getX();
		int y = this.getY();
		int yOffset = 2;
		for (int index = 0; index < entryCount; ++index) {
			int entryTop = this.getRowTop(index) + 2;
			int entryBottom = this.getRowBottom(index);
			if (entryBottom >= y && entryTop <= this.getBottom()) {
				ModListEntry entry = this.getEntry(index);
				if (entry == null) continue;
				if (this.isSelectedEntry(index)) {
					int entryContentLeft = entryLeft + entry.getXOffset() - 2;
					int entryContentWidth = entryWidth - entry.getXOffset() + 4;
					this.drawSelectionHighlight(
						drawContext,
						entryContentLeft,
						entryTop + yOffset,
						entryContentWidth,
						entryHeight,
						this.isFocused() ? Colors.WHITE : Colors.GRAY, Colors.BLACK
					);
				}

				entry.setYOffset(yOffset);
				entry.render(
					drawContext,
					mouseX,
					mouseY,
					this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry),
					delta
				);
			}
		}
	}

	/**
	 * Version of {@link #drawSelectionHighlight(DrawContext, EntryListWidget.Entry, int)} with unconstrained positioning and sizing.
	 */
	protected void drawSelectionHighlight(DrawContext context, int x, int y, int width, int height, int borderColor, int fillColor) {
		context.fill(x, y - 2, x + width, y + height + 2, borderColor);
		context.fill(x + 1, y - 1, x + width - 1, y + height + 1, fillColor);
	}

	public void ensureVisible(ModListEntry entry) {
//		super.ensureVisible(entry);
		int i = this.getRowTop(this.children().indexOf(entry));
		int j = i - this.getY() - 4 - this.itemHeight;
		if (j < 0) {
			this.setScrollY(this.getScrollY() + j);
		}

		int k = this.getBottom() - i - (this.itemHeight * 2);
		if (k < 0) {
			this.setScrollY(this.getScrollY() - k);
		}
	}

	public boolean keyPressed(KeyInput input) {
		if (input.isUp() || input.isDown()) {
			return super.keyPressed(input);
		}

		if (getSelectedOrNull() != null) {
			return getSelectedOrNull().keyPressed(input);
		}

		return false;
	}

	public final ModListEntry getEntryAtPos(double x, double y) {
		int int_5 = MathHelper.floor(y - (double) this.getY()) + (int) this.getScrollY() - 4;
		int index = int_5 / this.itemHeight;
		return x < (double) this.getScrollbarX() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getEntryCount() ? this.children().get(index) : null;
	}

	@Override
	protected int getScrollbarX() {
		return this.width - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - (Math.max(0, this.getContentsHeightWithPadding() - (this.getBottom() - this.getY() - 4)) > 0 ? 18 : 12);
	}

	@Override
	public int getRowLeft() {
		return this.getX() + 6;
	}

	public ModsScreen getParent() {
		return parent;
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return super.getContentsHeightWithPadding() + 4;
	}

	public int getDisplayedCountFor(Set<String> set) {
		int count = 0;
		for (ModListEntry c : children()) {
			if (set.contains(c.getMod().getId())) {
				count++;
			}
		}

		return count;
	}

	@Override
	public void close() {
		iconHandler.close();
	}

	public FabricIconHandler getFabricIconHandler() {
		return iconHandler;
	}
}
