package com.terraformersmc.modmenu.gui.widget.entries;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;

public class ChildEntry extends ModListEntry {
	private final boolean bottomChild;
	private final ParentEntry parent;

	public ChildEntry(Mod mod, ParentEntry parent, ModListWidget list, boolean bottomChild) {
		super(mod, list);
		this.bottomChild = bottomChild;
		this.parent = parent;
	}

	@Override
	public void render(
		DrawContext drawContext,
		int mouseX,
		int mouseY,
		boolean isSelected,
		float delta
	) {
		super.render(drawContext, mouseX, mouseY, isSelected, delta);
		int x = this.getContentX() - 2;
		int y = this.getContentY() + this.getYOffset();
//		int rowWidth = this.getContentWidth();
		int rowHeight = this.getContentHeight();
		int color = 0xFFA0A0A0;
		drawContext.fill(x, y - 2, x + 1, y + (bottomChild ? rowHeight / 2 : rowHeight + 2), color);
		drawContext.fill(x, y + rowHeight / 2, x + 7, y + rowHeight / 2 + 1, color);
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (input.isLeft()) {
			list.setSelected(parent);
			list.ensureVisible(parent);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getXOffset() {
		return 13;
	}
}
