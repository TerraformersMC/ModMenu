package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class UpdateAvailableBadge {
	private static final Identifier UPDATE_ICON = new Identifier("realms", "textures/gui/realms/trial_icon.png");
	public static void renderBadge(MatrixStack matrices, int x, int y) {
		RenderSystem.setShaderTexture(0, UPDATE_ICON);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		int animOffset = 0;
		if((Util.getMeasuringTimeMs() / 800L & 1L) == 1L) {
			animOffset = 8;
		}
		DrawableHelper.drawTexture(matrices, x, y, 0f, animOffset, 8, 8, 8, 16);
	}
}
