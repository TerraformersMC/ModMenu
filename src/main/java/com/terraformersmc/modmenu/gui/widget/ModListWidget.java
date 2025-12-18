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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;

public class ModListWidget extends ObjectSelectionList<ModListEntry> implements AutoCloseable {
	public static final boolean DEBUG = Boolean.getBoolean("modmenu.debug");
	private final ModsScreen parent;
	private List<Mod> mods = null;
	private final Set<Mod> addedMods = new HashSet<>();
	private String selectedModId = null;
	//private boolean scrolling;
	private final FabricIconHandler iconHandler = new FabricIconHandler();
	private Double restoreScrollY = null;

	public ModListWidget(
		Minecraft client,
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
			this.restoreScrollY = list.scrollAmount();
		}
	}

	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		int denominator = Math.max(0, this.contentHeight() - (this.getBottom() - this.getY() - 4));
		if (denominator == 0) {
			parent.updateScrollPercent(0);
		} else {
			parent.updateScrollPercent(scrollAmount() / Math.max(0, this.contentHeight() - (this.getBottom() - this.getY() - 4)));
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
			this.minecraft.getNarrator().saySystemChatQueued(Component.translatable("narrator.select", mod.getTranslatedName()));
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

		parent.updateSelectedEntry(getSelected());
	}

	protected boolean isSelectedEntry(int index) {
		ModListEntry selected = getSelected();
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
			setScrollAmount(restoreScrollY);
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

		if (parent.getSelectedEntry() != null && !children().isEmpty() || this.getSelected() != null && getSelected().getMod() != parent.getSelectedEntry().getMod()) {
			for (ModListEntry entry : children()) {
				if (entry.getMod().equals(parent.getSelectedEntry().getMod())) {
					setSelected(entry);
				}
			}
		} else {
			if (getSelected() == null && !children().isEmpty() && getEntry(0) != null) {
				setSelected(getEntry(0));
			}
		}

		if (scrollAmount() > Math.max(0, this.contentHeight() - (this.getBottom() - this.getY() - 4))) {
			setScrollAmount(Math.max(0, this.contentHeight() - (this.getBottom() - this.getY() - 4)));
		}
	}

	@Override
	protected void renderListItems(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
		int entryLeft = this.getRowLeft();
		int entryWidth = this.getRowWidth();
		int entryHeight = this.defaultEntryHeight - 4;
		int entryCount = this.getItemCount();
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
						this.isFocused() ? CommonColors.WHITE : CommonColors.GRAY, CommonColors.BLACK
					);
				}

				entry.setYOffset(yOffset);
				entry.renderContent(
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
	 * Version of {@link #renderSelection(GuiGraphics, AbstractSelectionList.Entry, int)} with unconstrained positioning and sizing.
	 */
	protected void drawSelectionHighlight(GuiGraphics context, int x, int y, int width, int height, int borderColor, int fillColor) {
		context.fill(x, y - 2, x + width, y + height + 2, borderColor);
		context.fill(x + 1, y - 1, x + width - 1, y + height + 1, fillColor);
	}

	public void ensureVisible(ModListEntry entry) {
//		super.ensureVisible(entry);
		int i = this.getRowTop(this.children().indexOf(entry));
		int j = i - this.getY() - 4 - this.defaultEntryHeight;
		if (j < 0) {
			this.setScrollAmount(this.scrollAmount() + j);
		}

		int k = this.getBottom() - i - (this.defaultEntryHeight * 2);
		if (k < 0) {
			this.setScrollAmount(this.scrollAmount() - k);
		}
	}

	public boolean keyPressed(KeyEvent input) {
		if (input.isUp() || input.isDown()) {
			return super.keyPressed(input);
		}

		if (getSelected() != null) {
			return getSelected().keyPressed(input);
		}

		return false;
	}

	public final ModListEntry getEntryAtPos(double x, double y) {
		int int_5 = Mth.floor(y - (double) this.getY()) + (int) this.scrollAmount() - 4;
		int index = int_5 / this.defaultEntryHeight;
		return x < (double) this.scrollBarX() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getItemCount() ? this.children().get(index) : null;
	}

	@Override
	protected int scrollBarX() {
		return this.width - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - (Math.max(0, this.contentHeight() - (this.getBottom() - this.getY() - 4)) > 0 ? 18 : 12);
	}

	@Override
	public int getRowLeft() {
		return this.getX() + 6;
	}

	public ModsScreen getParent() {
		return parent;
	}

	@Override
	protected int contentHeight() {
		return super.contentHeight() + 4;
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
