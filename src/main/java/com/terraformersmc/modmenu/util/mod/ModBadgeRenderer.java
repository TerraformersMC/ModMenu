package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.DrawingUtil;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;

public class ModBadgeRenderer {
	protected int startX, startY, badgeX, badgeY, badgeMax;
	protected Mod mod;
	protected Minecraft client;
	protected final ModsScreen screen;

	public ModBadgeRenderer(int startX, int startY, int endX, Mod mod, ModsScreen screen) {
		this.startX = startX;
		this.startY = startY;
		this.badgeMax = endX;
		this.mod = mod;
		this.screen = screen;
		this.client = Minecraft.getInstance();
	}

	public void draw(GuiGraphics drawContext, int mouseX, int mouseY) {
		this.badgeX = startX;
		this.badgeY = startY;
		Set<Mod.Badge> badges = mod.getBadges();
		badges.forEach(badge -> drawBadge(drawContext, badge, mouseX, mouseY));
	}

	public void drawBadge(GuiGraphics drawContext, Mod.Badge badge, int mouseX, int mouseY) {
		this.drawBadge(
			drawContext,
			badge.getText().getVisualOrderText(),
			badge.getOutlineColor(),
			badge.getFillColor(),
			mouseX,
			mouseY
		);
	}

	public void drawBadge(
		GuiGraphics drawContext,
		FormattedCharSequence text,
		int outlineColor,
		int fillColor,
		int mouseX,
		int mouseY
	) {
		int width = client.font.width(text) + 6;
		if (badgeX + width < badgeMax) {
			DrawingUtil.drawBadge(drawContext, badgeX, badgeY, width, text, outlineColor, fillColor, 0xFFCACACA);
			badgeX += width + 3;
		}
	}

	public Mod getMod() {
		return mod;
	}
}
