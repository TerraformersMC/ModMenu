package com.terraformersmc.modmenu.gui.widget;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class UpdateAvailableBadge {
	private static final Identifier UPDATE_ICON = Identifier.ofVanilla("icon/trial_available");

	public static void renderBadge(DrawContext drawContext, int x, int y) {
		drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, UPDATE_ICON, x, y, 8, 8, 0xFFFFFFFF);
	}
}
