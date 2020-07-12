package io.github.prospector.modmenu.util;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModsScreen;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.Calendar;

public class BadgeRenderer {
	protected int startX, startY, badgeX, badgeY, badgeMax;
	protected ModContainer container;
	protected ModMetadata metadata;
	protected MinecraftClient client;
	protected final ModsScreen screen;

	public BadgeRenderer(int startX, int startY, int endX, ModContainer container, ModsScreen screen) {
		this.startX = startX;
		this.startY = startY;
		this.badgeMax = endX;
		this.container = container;
		this.metadata = container.getMetadata();
		this.screen = screen;
		this.client = MinecraftClient.getInstance();
	}

	public void draw(MatrixStack matrices, int mouseX, int mouseY) {
		this.badgeX = startX;
		this.badgeY = startY;
		if (ModMenu.LIBRARY_MODS.contains(metadata.getId())) {
			drawBadge(matrices, BadgeType.LIBRARY, mouseX, mouseY);
		}
		if (ModMenu.CLIENTSIDE_MODS.contains(metadata.getId())) {
			drawBadge(matrices, BadgeType.CLIENTSIDE, mouseX, mouseY);
		}
		if (ModMenu.DEPRECATED_MODS.contains(metadata.getId())) {
			drawBadge(matrices, BadgeType.DEPRECATED, mouseX, mouseY);
		}
		if (ModMenu.PATCHWORK_FORGE_MODS.contains(metadata.getId())) {
			drawBadge(matrices, BadgeType.PATCHWORK_FORGE, mouseX, mouseY);
		}
		if (metadata.getId().equals("minecraft")) {
			drawBadge(matrices, BadgeType.MINECRAFT, mouseX, mouseY);
		}
		//noinspection MagicConstant
		if (Calendar.getInstance().get(0b10) == 0b11 && Calendar.getInstance().get(0b101) == 0x1) {
			if (metadata.getId().equals(new String(new byte[]{109, 111, 100, 109, 101, 110, 117}))) {
				drawBadge(matrices, new LiteralText(new String(new byte[]{-30, -100, -104, 32, 86, 105, 114, 117, 115, 32, 68, 101, 116, 101, 99, 116, 101, 100})), 0b10001000111111110010001000100010, 0b10001000011111110000100000001000, mouseX, mouseY);
			} else if (metadata.getId().contains(new String(new byte[]{116, 97, 116, 101, 114}))) {
				drawBadge(matrices, new LiteralText(new String(new byte[]{116, 97, 116, 101, 114})), 0b10001000111010111011001100101011, 0b10001000100110010111000100010010, mouseX, mouseY);
			} else {
				drawBadge(matrices, new LiteralText(new String(new byte[]{-30, -100, -108, 32, 98, 121, 32, 77, 99, 65, 102, 101, 101})), 0b10001000000111011111111101001000, 0b10001000000001110110100100001110, mouseX, mouseY);
			}
		}
	}
	
	public void drawBadge(MatrixStack matrices, BadgeType badgeType, int mouseX, int mouseY) {
		this.drawBadge(matrices, badgeType.getText(), badgeType.getOutlineColor(), badgeType.getFillColor(), mouseX, mouseY);
	}

	public void drawBadge(MatrixStack matrices, Text text, int outlineColor, int fillColor, int mouseX, int mouseY) {
		int width = client.textRenderer.getWidth(text) + 6;
		if (badgeX + width < badgeMax) {
			RenderUtils.drawBadge(matrices, badgeX, badgeY, width, text, outlineColor, fillColor, 0xCACACA);
			badgeX += width + 3;
		}
	}

	public ModMetadata getMetadata() {
		return metadata;
	}
}
