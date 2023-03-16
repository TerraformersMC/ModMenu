package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.DrawingUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
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

	public void draw(DrawContext DrawContext, int mouseX, int mouseY) {
		this.badgeX = startX;
		this.badgeY = startY;
		Set<Mod.Badge> badges = mod.getBadges();
		badges.forEach(badge -> drawBadge(DrawContext, badge, mouseX, mouseY));
		if (ModMenuConfig.EASTER_EGGS.getValue()) {
			//noinspection MagicConstant
			if (Calendar.getInstance().get(0b10) == 0b11 && Calendar.getInstance().get(0b101) == 0x1) {
				if (mod.getId().equals(new String(new byte[]{109, 111, 100, 109, 101, 110, 117}, StandardCharsets.UTF_8))) {
					drawBadge(DrawContext, Text.literal(new String(new byte[]{-30, -100, -104, 32, 86, 105, 114, 117, 115, 32, 68, 101, 116, 101, 99, 116, 101, 100}, StandardCharsets.UTF_8)).asOrderedText(), 0b10001000111111110010001000100010, 0b10001000011111110000100000001000, mouseX, mouseY);
				} else if (mod.getId().contains(new String(new byte[]{116, 97, 116, 101, 114}, StandardCharsets.UTF_8))) {
					drawBadge(DrawContext, Text.literal(new String(new byte[]{116, 97, 116, 101, 114}, StandardCharsets.UTF_8)).asOrderedText(), 0b10001000111010111011001100101011, 0b10001000100110010111000100010010, mouseX, mouseY);
				} else {
					drawBadge(DrawContext, Text.literal(new String(new byte[]{-30, -100, -108, 32, 98, 121, 32, 77, 99, 65, 102, 101, 101}, StandardCharsets.UTF_8)).asOrderedText(), 0b10001000000111011111111101001000, 0b10001000000001110110100100001110, mouseX, mouseY);
				}
			}
		}
	}

	public void drawBadge(DrawContext DrawContext, Mod.Badge badge, int mouseX, int mouseY) {
		this.drawBadge(DrawContext, badge.getText().asOrderedText(), badge.getOutlineColor(), badge.getFillColor(), mouseX, mouseY);
	}

	public void drawBadge(DrawContext DrawContext, OrderedText text, int outlineColor, int fillColor, int mouseX, int mouseY) {
		int width = client.textRenderer.getWidth(text) + 6;
		if (badgeX + width < badgeMax) {
			DrawingUtil.drawBadge(DrawContext, badgeX, badgeY, width, text, outlineColor, fillColor, 0xCACACA);
			badgeX += width + 3;
		}
	}

	public Mod getMod() {
		return mod;
	}
}
