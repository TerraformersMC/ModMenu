package io.github.prospector.modmenu.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.List;

@Environment(EnvType.CLIENT)
public class RenderUtils {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static void drawWrappedString(MatrixStack matrices, String string, int x, int y, int wrapWidth, int lines, int color) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<Text> strings = CLIENT.textRenderer.wrapStringToWidthAsList(new LiteralText(string), wrapWidth);
		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			String line = strings.get(i).getString();
			if (i == lines - 1 && strings.size() > lines) {
				line += "...";
			}
			int x1 = x;
			if (CLIENT.textRenderer.isRightToLeft()) {
				int width = CLIENT.textRenderer.getStringWidth(CLIENT.textRenderer.mirror(line));
				x1 += (float) (wrapWidth - width);
			}
			CLIENT.textRenderer.draw(matrices, line, x1, y + i * CLIENT.textRenderer.fontHeight, color);
		}
	}

	public static void drawBadge(MatrixStack matrices, int x, int y, int tagWidth, String text, int outlineColor, int fillColor, int textColor) {
		DrawableHelper.fill(matrices, x + 1, y - 1, x + tagWidth, y, outlineColor);
		DrawableHelper.fill(matrices, x, y, x + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		DrawableHelper.fill(matrices, x + 1, y + 1 + CLIENT.textRenderer.fontHeight - 1, x + tagWidth, y + CLIENT.textRenderer.fontHeight + 1, outlineColor);
		DrawableHelper.fill(matrices, x + tagWidth, y, x + tagWidth + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		DrawableHelper.fill(matrices, x + 1, y, x + tagWidth, y + CLIENT.textRenderer.fontHeight, fillColor);
		CLIENT.textRenderer.draw(matrices, text, (x + 1 + (tagWidth - CLIENT.textRenderer.getStringWidth(text)) / (float) 2), y + 1, textColor);
	}
}
