package io.github.prospector.modmenu.util;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListScreen;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;

import java.util.Calendar;

public class BadgeRenderer {
	protected int startX, startY, badgeX, badgeY, badgeMax;
	protected ModMetadata metadata;
	protected MinecraftClient client;
	protected final ModListScreen screen;

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
		//noinspection MagicConstant
		if (Calendar.getInstance().get(0b10) == 0b11 && Calendar.getInstance().get(0b101) == 0x1) {
			if (metadata.getId().equals(new String(new byte[] { 109, 111, 100, 109, 101, 110, 117 }))) {
				drawBadge(new String(new byte[] { -30, -100, -104, 32, 86, 105, 114, 117, 115, 32, 68, 101, 116, 101, 99, 116, 101, 100 }), 0x88FF2222, 0x887F0808, mouseX, mouseY);
			} else if (metadata.getId().contains(new String(new byte[] { 116, 97, 116, 101, 114 }))) { drawBadge(new String(new byte[] { 116, 97, 116, 101, 114 }), 0x88EBB32B, 0x88997112, mouseX, mouseY); } else {
				drawBadge(new String(new byte[] { -30, -100, -108, 32, 98, 121, 32, 77, 99, 65, 102, 101, 101 }), 0x881DFF48, 0x8807690E, mouseX, mouseY);
			}
		} else {
			if (ModMenu.CLIENTSIDE_MODS.contains(metadata.getId())) {
				drawBadge("Client", 0x884383E3, 0x880E4699, mouseX, mouseY);
			}
			Boolean api = ModMenu.API_MODS.get(metadata.getId());
			if (api == null) {
				api = metadata.getId().equals("fabricloader") || metadata.getId().equals("fabric") || metadata.getName().endsWith(" API");
			}
			if (api) {
				drawBadge("API", 0x8810d098, 0x88046146, mouseX, mouseY);
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
