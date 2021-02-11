package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Language;

import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class DrawingUtil {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static void drawRandomVersionBackground(Mod mod, MatrixStack matrices, int x, int y, int width, int height){
		int seed = mod.getName().hashCode() + mod.getVersion().hashCode();
		DrawableHelper.fill(matrices, x, y, x + width, y + height, 0xFF000000 + new Random(seed).nextInt(0xFFFFFF));
	}

	public static void drawWrappedString(MatrixStack matrices, String string, int x, int y, int wrapWidth, int lines, int color) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<StringVisitable> strings = CLIENT.textRenderer.getTextHandler().wrapLines(new LiteralText(string), wrapWidth, Style.EMPTY);
		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			StringVisitable renderable = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				renderable = StringVisitable.concat(strings.get(i), StringVisitable.plain("..."));
			}
			OrderedText line = Language.getInstance().reorder(renderable);
			int x1 = x;
			if (CLIENT.textRenderer.isRightToLeft()) {
				int width = CLIENT.textRenderer.getWidth(line);
				x1 += (float) (wrapWidth - width);
			}
			CLIENT.textRenderer.draw(matrices, line, x1, y + i * CLIENT.textRenderer.fontHeight, color);
		}
	}

	public static void drawBadge(MatrixStack matrices, int x, int y, int tagWidth, OrderedText text, int outlineColor, int fillColor, int textColor) {
		DrawableHelper.fill(matrices, x + 1, y - 1, x + tagWidth, y, outlineColor);
		DrawableHelper.fill(matrices, x, y, x + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		DrawableHelper.fill(matrices, x + 1, y + 1 + CLIENT.textRenderer.fontHeight - 1, x + tagWidth, y + CLIENT.textRenderer.fontHeight + 1, outlineColor);
		DrawableHelper.fill(matrices, x + tagWidth, y, x + tagWidth + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		DrawableHelper.fill(matrices, x + 1, y, x + tagWidth, y + CLIENT.textRenderer.fontHeight, fillColor);
		CLIENT.textRenderer.draw(matrices, text, (x + 1 + (tagWidth - CLIENT.textRenderer.getWidth(text)) / (float) 2), y + 1, textColor);
	}
}
