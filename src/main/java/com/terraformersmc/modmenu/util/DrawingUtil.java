package com.terraformersmc.modmenu.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class DrawingUtil {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static void drawRandomVersionBackground(Mod mod, PoseStack matrices, int x, int y, int width, int height){
		int seed = mod.getName().hashCode() + mod.getVersion().hashCode();
		GuiComponent.fill(matrices, x, y, x + width, y + height, 0xFF000000 + new Random(seed).nextInt(0xFFFFFF));
	}

	public static void drawWrappedString(PoseStack matrices, String string, int x, int y, int wrapWidth, int lines, int color) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<FormattedText> strings = CLIENT.font.getSplitter().splitLines(new TextComponent(string), wrapWidth, Style.EMPTY);
		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			FormattedText renderable = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				renderable = FormattedText.composite(strings.get(i), FormattedText.of("..."));
			}
			FormattedCharSequence line = Language.getInstance().getVisualOrder(renderable);
			int x1 = x;
			if (CLIENT.font.isBidirectional()) {
				int width = CLIENT.font.width(line);
				x1 += (float) (wrapWidth - width);
			}
			CLIENT.font.draw(matrices, line, x1, y + i * CLIENT.font.lineHeight, color);
		}
	}

	public static void drawBadge(PoseStack matrices, int x, int y, int tagWidth, FormattedCharSequence text, int outlineColor, int fillColor, int textColor) {
		GuiComponent.fill(matrices, x + 1, y - 1, x + tagWidth, y, outlineColor);
		GuiComponent.fill(matrices, x, y, x + 1, y + CLIENT.font.lineHeight, outlineColor);
		GuiComponent.fill(matrices, x + 1, y + 1 + CLIENT.font.lineHeight - 1, x + tagWidth, y + CLIENT.font.lineHeight + 1, outlineColor);
		GuiComponent.fill(matrices, x + tagWidth, y, x + tagWidth + 1, y + CLIENT.font.lineHeight, outlineColor);
		GuiComponent.fill(matrices, x + 1, y, x + tagWidth, y + CLIENT.font.lineHeight, fillColor);
		CLIENT.font.draw(matrices, text, (x + 1 + (tagWidth - CLIENT.font.width(text)) / (float) 2), y + 1, textColor);
	}
}
