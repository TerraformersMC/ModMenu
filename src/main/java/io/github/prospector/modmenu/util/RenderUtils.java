package io.github.prospector.modmenu.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import java.util.List;

@Environment(EnvType.CLIENT)
public class RenderUtils {
	public static void drawWrappedString(String string, int x, int y, int wrapWidth, int lines, int color) {
		MinecraftClient client = MinecraftClient.getInstance();
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<String> strings = client.fontRenderer.wrapStringToWidthAsList(string, wrapWidth);

		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			String line = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				line += "...";
			}
			int x1 = x;
			if (client.fontRenderer.isRightToLeft()) {
				int width = client.fontRenderer.getStringWidth(client.fontRenderer.mirror(line));
				x1 += (float) (wrapWidth - width);
			}
			client.fontRenderer.drawWithShadow(line, x1, y + i * client.fontRenderer.FONT_HEIGHT, color);
		}
	}
}
