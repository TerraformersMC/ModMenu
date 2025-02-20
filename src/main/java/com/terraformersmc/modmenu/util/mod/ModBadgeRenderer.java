package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.DrawingUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;

import java.util.Set;

public class ModBadgeRenderer {
	protected int startX, startY, badgeX, badgeY, badgeMax;
	protected Mod mod;
	protected MinecraftClient client;
	protected final ModsScreen screen;

	public ModBadgeRenderer(int startX, int startY, int endX, Mod mod, ModsScreen screen) {
		this.startX = startX;
		this.startY = startY;
		this.badgeMax = endX;
		this.mod = mod;
		this.screen = screen;
		this.client = MinecraftClient.getInstance();
	}

	public void draw(DrawContext drawContext, int mouseX, int mouseY) {
		this.badgeX = startX;
		this.badgeY = startY;
		Set<Mod.Badge> badges = mod.getBadges();
		badges.forEach(badge -> drawBadge(drawContext, badge, mouseX, mouseY));
	}

	public void drawBadge(DrawContext drawContext, Mod.Badge badge, int mouseX, int mouseY) {
		this.drawBadge(
			drawContext,
			badge.getText().asOrderedText(),
			badge.getOutlineColor(),
			badge.getFillColor()
		);
	}

	public void drawBadge(
		DrawContext drawContext,
		OrderedText text,
		int outlineColor,
		int fillColor
	) {
		int width = client.textRenderer.getWidth(text) + 6;
		if (badgeX + width < badgeMax) {
			DrawingUtil.drawBadge(drawContext, badgeX, badgeY, width, text, outlineColor, fillColor, 0xCACACA);
			badgeX += width + 3;
		}
	}

	public Mod getMod() {
		return mod;
	}
}
