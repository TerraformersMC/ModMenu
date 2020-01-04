package io.github.prospector.modmenu.util;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListScreen;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;

import java.util.Calendar;

public class BadgeRenderer {
	protected int startX, startY, badgeX, badgeY, badgeMax;
	protected ModContainer container;
	protected ModMetadata metadata;
	protected MinecraftClient client;
	protected final ModListScreen screen;

	public BadgeRenderer(int startX, int startY, int endX, ModContainer container, ModListScreen screen) {
		this.startX = startX;
		this.startY = startY;
		this.badgeMax = endX;
		this.container = container;
		this.metadata = container.getMetadata();
		this.screen = screen;
		this.client = MinecraftClient.getInstance();
	}

	public void draw(int mouseX, int mouseY) {
		this.badgeX = startX;
		this.badgeY = startY;
		if (ModMenu.LIBRARY_MODS.contains(metadata.getId())) {
			drawBadge(I18n.translate("modmenu.library"), 0x8810d098, 0x88046146, mouseX, mouseY);
		}
		if (ModMenu.CLIENTSIDE_MODS.contains(metadata.getId())) {
			drawBadge(I18n.translate("modmenu.clientsideOnly"), 0x884383E3, 0x880E4699, mouseX, mouseY);
		}
		if (ModMenu.PATCHWORK_FORGE_MODS.contains(metadata.getId())) {
			drawBadge(I18n.translate("modmenu.forge"), 0x887C89A3, 0x88202C43, mouseX, mouseY);
		}
		if (metadata.getId().equals("minecraft")) {
			drawBadge(I18n.translate("modmenu.minecraft"), 0x88BCBCBC, 0x88535353, mouseX, mouseY);
		}
		//noinspection MagicConstant
		if (Calendar.getInstance().get(0b10) == 0b11 && Calendar.getInstance().get(0b101) == 0x1) {
			if (metadata.getId().equals(new String(new byte[]{109, 111, 100, 109, 101, 110, 117}))) {
				drawBadge(new String(new byte[]{-30, -100, -104, 32, 86, 105, 114, 117, 115, 32, 68, 101, 116, 101, 99, 116, 101, 100}), 0b10001000111111110010001000100010, 0b10001000011111110000100000001000, mouseX, mouseY);
			} else if (metadata.getId().contains(new String(new byte[]{116, 97, 116, 101, 114}))) {
				drawBadge(new String(new byte[]{116, 97, 116, 101, 114}), 0b10001000111010111011001100101011, 0b10001000100110010111000100010010, mouseX, mouseY);
			} else {
				drawBadge(new String(new byte[]{-30, -100, -108, 32, 98, 121, 32, 77, 99, 65, 102, 101, 101}), 0b10001000000111011111111101001000, 0b10001000000001110110100100001110, mouseX, mouseY);
			}
		}
	}

	public void drawBadge(String text, int outlineColor, int fillColor, int mouseX, int mouseY) {
		int width = client.textRenderer.getStringWidth(text) + 6;
		if (badgeX + width < badgeMax) {
			RenderUtils.drawBadge(badgeX, badgeY, width, text, outlineColor, fillColor, 0xCACACA);
			badgeX += width + 3;
		}
	}

	public ModMetadata getMetadata() {
		return metadata;
	}
}
