package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class DrawingUtil {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static void drawRandomVersionBackground(
		Mod mod,
		DrawContext drawContext,
		int x,
		int y,
		int width,
		int height
	) {
		int seed = mod.getName().hashCode() + mod.getVersion().hashCode();

		Random random = new Random(seed);
		int color = 0xFF000000 | MathHelper.hsvToRgb(random.nextFloat(1f), random.nextFloat(0.7f, 0.8f), 0.9f);
		if (!ModMenuConfig.RANDOM_JAVA_COLORS.getValue()) {
			color = 0xFFDD5656;
		}

		drawContext.fill(x, y, x + width, y + height, color);
	}

	public static void drawWrappedString(
		DrawContext drawContext,
		String string,
		int x,
		int y,
		int wrapWidth,
		int lines,
		int color
	) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}

		List<StringVisitable> strings = CLIENT.textRenderer.getTextHandler().wrapLines(Text.literal(string), wrapWidth, Style.EMPTY);
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
				x1 += wrapWidth - CLIENT.textRenderer.getWidth(line);
			}

			drawContext.drawTextWithShadow(CLIENT.textRenderer, line, x1, y + i * CLIENT.textRenderer.fontHeight, color);
		}
	}

	public static void drawBadge(
		DrawContext drawContext,
		int x,
		int y,
		int tagWidth,
		OrderedText text,
		int outlineColor,
		int fillColor,
		int textColor
	) {
		drawContext.fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
		drawContext.fill(x, y, x + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		drawContext.fill(x + 1,
			y + 1 + CLIENT.textRenderer.fontHeight - 1,
			x + tagWidth,
			y + CLIENT.textRenderer.fontHeight + 1,
			outlineColor
		);
		drawContext.fill(x + tagWidth, y, x + tagWidth + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		drawContext.fill(x + 1, y, x + tagWidth, y + CLIENT.textRenderer.fontHeight, fillColor);
		drawContext.drawText(CLIENT.textRenderer,
			text,
			(int) (x + 1 + (tagWidth - CLIENT.textRenderer.getWidth(text)) / (float) 2),
			y + 1,
			textColor,
			false
		);
	}
}
