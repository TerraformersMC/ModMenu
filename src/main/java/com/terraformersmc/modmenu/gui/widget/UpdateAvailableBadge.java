package com.terraformersmc.modmenu.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class UpdateAvailableBadge {
	private static final Identifier UPDATE_ICON = Identifier.ofVanilla("icon/trial_available");

	public static void renderBadge(DrawContext context, int x, int y) {
		context.drawGuiTexture(RenderLayer::getGuiTextured, UPDATE_ICON, x, y, 8, 8);
	}
}
