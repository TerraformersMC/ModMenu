package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
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
import net.minecraft.client.gl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

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
			this.client.getNarratorManager().narrate(Text.translatable("narrator.select", mod.getTranslatedName()).getString());
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

	@Override
	protected boolean isSelectedEntry(int index) {
		ModListEntry selected = getSelectedOrNull();
		return selected != null && selected.getMod().getId().equals(getEntry(index).getMod().getId());
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

	@Override
	protected boolean removeEntry(ModListEntry entry) {
		addedMods.remove(entry.mod);
		return super.removeEntry(entry);
	}

	@Override
	protected ModListEntry remove(int index) {
		addedMods.remove(getEntry(index).mod);
		return super.remove(index);
	}

	public void finalizeInit() {
		reloadFilters();
		if(restoreScrollY != null) {
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

	private void filter(String searchTerm, boolean refresh, boolean search) {
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
		int entryCount = this.getEntryCount();
		for (int index = 0; index < entryCount; ++index) {
			int entryTop = this.getRowTop(index) + 2;
			int entryBottom = this.getRowTop(index) + this.itemHeight;
			if (entryBottom >= this.getY() && entryTop <= this.getBottom()) {
				int entryHeight = this.itemHeight - 4;
				ModListEntry entry = this.getEntry(index);
				int rowWidth = this.getRowWidth();
				int entryLeft;
				if (this.isSelectedEntry(index)) {
					Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
					entryLeft = getRowLeft() - 2 + entry.getXOffset();
					int selectionRight = this.getRowLeft() + rowWidth + 2;
					float float_2 = this.isFocused() ? 1.0F : 0.5F;
					final int topColor = ColorHelper.fromFloats(1.0F, float_2, float_2, float_2);
					final int bottomColor = ColorHelper.fromFloats(1.0F, 0.0F, 0.0F, 0.0F);
                    RenderPipeline pipeline = RenderPipelines.GUI;
                    try (BufferAllocator alloc = new BufferAllocator(pipeline.getVertexFormat().getVertexSize() * 4)) {
                        BufferBuilder bufferBuilder = new BufferBuilder(alloc, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
                        bufferBuilder.vertex(matrix, entryLeft, entryTop + entryHeight + 2, 0.0F).color(topColor);
                        bufferBuilder.vertex(matrix, selectionRight, entryTop + entryHeight + 2, 0.0F).color(topColor);
                        bufferBuilder.vertex(matrix, selectionRight, entryTop - 2, 0.0F).color(topColor);
                        bufferBuilder.vertex(matrix, entryLeft, entryTop - 2, 0.0F).color(topColor);
                        bufferBuilder.vertex(matrix, entryLeft + 1, entryTop + entryHeight + 1, 0.0F).color(bottomColor);
                        bufferBuilder.vertex(matrix, selectionRight - 1, entryTop + entryHeight + 1, 0.0F).color(bottomColor);
                        bufferBuilder.vertex(matrix, selectionRight - 1, entryTop - 1, 0.0F).color(bottomColor);
                        bufferBuilder.vertex(matrix, entryLeft + 1, entryTop - 1, 0.0F).color(bottomColor);
                        try (BuiltBuffer builtBuffer = bufferBuilder.endNullable()) {
                            if (builtBuffer == null) {
                                alloc.close();
                                return;
                            }
                            Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
                            RenderSystem.ShapeIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
                            VertexFormat.IndexType indexType = autoStorageIndexBuffer.getIndexType();
                            GpuBuffer indexBuffer = autoStorageIndexBuffer.getIndexBuffer(builtBuffer.getDrawParameters().indexCount());
                            GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Mod List", BufferType.VERTICES, BufferUsage.DYNAMIC_WRITE, builtBuffer.getBuffer().remaining());
                            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(vertexBuffer, builtBuffer.getBuffer(), 0);
                            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(framebuffer.getColorAttachment(), OptionalInt.empty(), framebuffer.getDepthAttachment(), OptionalDouble.empty())) {
                                renderPass.setPipeline(pipeline);
                                renderPass.setVertexBuffer(0, vertexBuffer);
                                renderPass.setIndexBuffer(indexBuffer, indexType);
                                renderPass.drawIndexed(0, builtBuffer.getDrawParameters().indexCount());
                            }
                        }
                    }
				}

				entryLeft = this.getRowLeft();
				entry.render(
					drawContext,
					index,
					entryTop,
					entryLeft,
					rowWidth,
					entryHeight,
					mouseX,
					mouseY,
					this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry),
					delta
				);
			}
		}
	}

	public void ensureVisible(ModListEntry entry) {
		super.ensureVisible(entry);
	}

// FIXME --> Was removed from super class (updateScrollingState / mouseClicked)
	/*
	@Override
	protected void updateScrollingState(double double_1, double double_2, int int_1) {
		super.updateScrollingState(double_1, double_2, int_1);
		this.scrolling = int_1 == 0 && double_1 >= (double) this.getScrollbarX() && double_1 < (double) (this.getScrollbarX() + 6);
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
			} else if (int_1 == 0 && this.clickedHeader((int) (double_1 - (double) (this.getX() + this.width / 2 - this.getRowWidth() / 2)),
				(int) (double_2 - (double) this.getY()) + (int) this.getScrollY() - 4
			)) {
				return true;
			}

			return this.scrolling;
		}
	}
	 */

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}

		if (getSelectedOrNull() != null) {
			return getSelectedOrNull().keyPressed(keyCode, scanCode, modifiers);
		}

		return false;
	}

	public final ModListEntry getEntryAtPos(double x, double y) {
		int int_5 = MathHelper.floor(y - (double) this.getY()) - this.headerHeight + (int) this.getScrollY() - 4;
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

	public int getTop() {
		return this.getY();
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
