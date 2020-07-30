package io.github.prospector.modmenu.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_5481;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Style;
import net.minecraft.util.Language;

import java.util.List;

@Environment(EnvType.CLIENT)
public class RenderUtils {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static void drawWrappedString(MatrixStack matrices, String string, int x, int y, int wrapWidth, int lines, int color) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<StringRenderable> strings = CLIENT.textRenderer.getTextHandler().wrapLines(new LiteralText(string), wrapWidth, Style.EMPTY);
		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			StringRenderable renderable = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				renderable = StringRenderable.concat(strings.get(i), StringRenderable.plain("..."));
			}
			class_5481 line = Language.getInstance().method_30934(renderable);
			int x1 = x;
			if (CLIENT.textRenderer.isRightToLeft()) {
				int width = CLIENT.textRenderer.method_30880(line);
				x1 += (float) (wrapWidth - width);
			}
			CLIENT.textRenderer.draw(matrices, line, x1, y + i * CLIENT.textRenderer.fontHeight, color);
		}
	}

	public static void drawBadge(MatrixStack matrices, int x, int y, int tagWidth, class_5481 text, int outlineColor, int fillColor, int textColor) {
		DrawableHelper.fill(matrices, x + 1, y - 1, x + tagWidth, y, outlineColor);
		DrawableHelper.fill(matrices, x, y, x + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		DrawableHelper.fill(matrices, x + 1, y + 1 + CLIENT.textRenderer.fontHeight - 1, x + tagWidth, y + CLIENT.textRenderer.fontHeight + 1, outlineColor);
		DrawableHelper.fill(matrices, x + tagWidth, y, x + tagWidth + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		DrawableHelper.fill(matrices, x + 1, y, x + tagWidth, y + CLIENT.textRenderer.fontHeight, fillColor);
		CLIENT.textRenderer.draw(matrices, text, (x + 1 + (tagWidth - CLIENT.textRenderer.method_30880(text)) / (float) 2), y + 1, textColor);
	}
}
