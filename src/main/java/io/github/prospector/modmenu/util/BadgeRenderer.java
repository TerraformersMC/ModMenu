package io.github.prospector.modmenu.util;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListScreen;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;

import java.util.Calendar;

public class BadgeRenderer {
	protected int startX;
	protected int startY;
	protected int badgeX;
	protected int badgeY;
	protected int badgeMax;
	public ModMetadata metadata;
	private MinecraftClient client;
	protected ModListScreen screen;

	public BadgeRenderer(int startX, int startY, int endX, ModMetadata metadata, ModListScreen screen) {
		this.startX = startX;
		this.startY = startY;
		this.badgeMax = endX;
		this.metadata = metadata;
		this.screen = screen;
		this.client = MinecraftClient.getInstance();
	}

	public void draw(int mouseX, int mouseY) {
		this.badgeX = startX;
		this.badgeY = startY;
		if (Calendar.getInstance().get(0b10) == 0b11 && Calendar.getInstance().get(0b101) == 0x1) {
			if (metadata.getId().equals(new String(new byte[] { 109, 111, 100, 109, 101, 110, 117 }))) {
				drawBadge(new String(new byte[] { -30, -100, -104, 32, 86, 105, 114, 117, 115, 32, 68, 101, 116, 101, 99, 116, 101, 100 }), 0x88FF2222, 0x887F0808, mouseX, mouseY);
			} else if (metadata.getId().contains(new String(new byte[] { 116, 97, 116, 101, 114 }))) { drawBadge(new String(new byte[] { 116, 97, 116, 101, 114 }), 0x88EBB32B, 0x88997112, mouseX, mouseY); } else {
				drawBadge(new String(new byte[] { -30, -100, -108, 32, 98, 121, 32, 77, 99, 65, 102, 101, 101 }), 0x881DFF48, 0x8807690E, mouseX, mouseY);
			}
		} else {
			ModMenuModConfig config = ModMenu.MOD_MENU_MOD_CONFIG_OVERRIDES.get(metadata.getId());
			if (config == null) {
				config = ModMenu.MOD_MENU_MOD_CONFIGS.get(metadata.getId());
			}
			if (config != null) {
				if (config.isModClientsideOnly()) {
					drawBadge("Client", 0x884383E3, 0x880E4699, mouseX, mouseY);
				}
				if (config.isModApi()) {
					drawBadge("API", 0x8810d098, 0x88046146, mouseX, mouseY);
				}
			} else {
				if (metadata.getId().equals("fabricloader") || metadata.getId().equals("fabric") || metadata.getName().endsWith(" API")) {
					drawBadge("API", 0x8810d098, 0x88046146, mouseX, mouseY);
				}
			}
		}
	}

	public void drawBadge(String text, int outlineColor, int fillColor, int mouseX, int mouseY) {
		int width = client.textRenderer.getStringWidth(text) + 6;
		if (badgeX + width < badgeMax) {
			RenderUtils.drawBadge(badgeX, badgeY, width, text, outlineColor, fillColor, 0xCACACA);
			if (mouseX > badgeX && mouseY > badgeY && mouseY < badgeY + client.textRenderer.fontHeight + 4 && mouseX < badgeX + width) {
				//do nothing hahaha i made computer do stupid check for no reason
			}
			badgeX += width + 3;
		}
	}
}
