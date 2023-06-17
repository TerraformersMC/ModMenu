package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModSearch;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ParentEntry extends ModListEntry {
	private static final Identifier PARENT_MOD_TEXTURE = new Identifier(ModMenu.MOD_ID, "textures/gui/parent_mod.png");
	protected List<Mod> children;
	protected ModListWidget list;
	protected boolean hoveringIcon = false;

	public ParentEntry(Mod parent, List<Mod> children, ModListWidget list) {
		super(parent, list);
		this.children = children;
		this.list = list;
	}

	@Override
	public void render(DrawContext DrawContext, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		super.render(DrawContext, index, y, x, rowWidth, rowHeight, mouseX, mouseY, isSelected, delta);
		TextRenderer font = client.textRenderer;
		int childrenBadgeHeight = font.fontHeight;
		int childrenBadgeWidth = font.fontHeight;
		int shownChildren = ModSearch.search(list.getParent(), list.getParent().getSearchInput(), getChildren()).size();
		Text str = shownChildren == children.size() ? Text.literal(String.valueOf(shownChildren)) : Text.literal(shownChildren + "/" + children.size());
		int childrenWidth = font.getWidth(str) - 1;
		if (childrenBadgeWidth < childrenWidth + 4) {
			childrenBadgeWidth = childrenWidth + 4;
		}
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		int childrenBadgeX = x + iconSize - childrenBadgeWidth;
		int childrenBadgeY = y + iconSize - childrenBadgeHeight;
		int childrenOutlineColor = 0xff107454;
		int childrenFillColor = 0xff093929;
		DrawContext.fill(childrenBadgeX + 1, childrenBadgeY, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, childrenOutlineColor);
		DrawContext.fill( childrenBadgeX, childrenBadgeY + 1, childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, childrenOutlineColor);
		DrawContext.fill( childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth, childrenBadgeY + childrenBadgeHeight - 1, childrenOutlineColor);
		DrawContext.fill( childrenBadgeX + 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight - 1, childrenFillColor);
		DrawContext.fill( childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight, childrenOutlineColor);
		DrawContext.drawText(font, str.asOrderedText(), (int) (childrenBadgeX + (float) childrenBadgeWidth / 2 - (float) childrenWidth / 2), childrenBadgeY + 1, 0xCACACA, false);
		this.hoveringIcon = mouseX >= x - 1 && mouseX <= x - 1 + iconSize && mouseY >= y - 1 && mouseY <= y - 1 + iconSize;
		if (isMouseOver(mouseX, mouseY)) {
			DrawContext.fill(x, y, x + iconSize, y + iconSize, 0xA0909090);
			int xOffset = list.getParent().showModChildren.contains(getMod().getId()) ? iconSize : 0;
			int yOffset = hoveringIcon ? iconSize : 0;
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			DrawContext.drawTexture(PARENT_MOD_TEXTURE, x, y, xOffset, yOffset, iconSize + xOffset, iconSize + yOffset, ModMenuConfig.COMPACT_LIST.getValue() ? (int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) : 256, ModMenuConfig.COMPACT_LIST.getValue() ? (int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) : 256);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int i) {
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		boolean quickConfigure = ModMenuConfig.QUICK_CONFIGURE.getValue();
		if (mouseX - list.getRowLeft() <= iconSize) {
			this.toggleChildren();
			return true;
		} else if (!quickConfigure && Util.getMeasuringTimeMs() - this.sinceLastClick < 250) {
			this.toggleChildren();
			return true;
		} else {
			return super.mouseClicked(mouseX, mouseY, i);
		}
	}

	private void toggleChildren() {
		String id = getMod().getId();
		if (list.getParent().showModChildren.contains(id)) {
			list.getParent().showModChildren.remove(id);
		} else {
			list.getParent().showModChildren.add(id);
		}
		list.filter(list.getParent().getSearchInput(), false);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		String modId = getMod().getId();
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
			if (list.getParent().showModChildren.contains(modId)) {
				list.getParent().showModChildren.remove(modId);
			} else {
				list.getParent().showModChildren.add(modId);
			}
			list.filter(list.getParent().getSearchInput(), false);
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_LEFT) {
			if (list.getParent().showModChildren.contains(modId)) {
				list.getParent().showModChildren.remove(modId);
				list.filter(list.getParent().getSearchInput(), false);
			}
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
			if (!list.getParent().showModChildren.contains(modId)) {
				list.getParent().showModChildren.add(modId);
				list.filter(list.getParent().getSearchInput(), false);
			} else {
				return list.keyPressed(GLFW.GLFW_KEY_DOWN, 0, 0);
			}
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public void setChildren(List<Mod> children) {
		this.children = children;
	}

	public void addChildren(List<Mod> children) {
		this.children.addAll(children);
	}

	public void addChildren(Mod... children) {
		this.children.addAll(Arrays.asList(children));
	}

	public List<Mod> getChildren() {
		return children;
	}

	@Override
	public boolean isMouseOver(double double_1, double double_2) {
		return Objects.equals(this.list.getEntryAtPos(double_1, double_2), this);
	}
}
