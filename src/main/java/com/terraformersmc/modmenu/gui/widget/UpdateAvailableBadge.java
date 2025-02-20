package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class UpdateAvailableBadge {
	private static final Identifier UPDATE_ICON = Identifier.ofVanilla("icon/trial_available");

	public static void renderBadge(DrawContext drawContext, int x, int y) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		drawContext.drawGuiTexture(RenderLayer::getGuiTextured, UPDATE_ICON, x, y, 8, 8);
	}
}
