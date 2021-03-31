package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.gui.GuiComponent;

public class ChildEntry extends ModListEntry {
	private boolean bottomChild;
	private ParentEntry parent;

	public ChildEntry(Mod mod, ParentEntry parent, ModListWidget list, boolean bottomChild) {
		super(mod, list);
		this.bottomChild = bottomChild;
		this.parent = parent;
	}

	@Override
	public void render(PoseStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		super.render(matrices, index, y, x, rowWidth, rowHeight, mouseX, mouseY, isSelected, delta);
		x += 4;
		int color = 0xFFA0A0A0;
		GuiComponent.fill(matrices, x, y - 2, x + 1, y + (bottomChild ? rowHeight / 2 : rowHeight + 2), color);
		GuiComponent.fill(matrices, x, y + rowHeight / 2, x + 7, y + rowHeight / 2 + 1, color);
	}

	@Override
	public int getXOffset() {
		return 13;
	}
}
