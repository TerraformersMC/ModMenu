package prospector.modmenu.util;

import net.fabricmc.api.Side;
import net.fabricmc.api.Sided;
import net.minecraft.client.MinecraftGame;

import java.util.List;

@Sided(Side.CLIENT)
public class RenderUtils {
	public static void drawWrappedString(String string, int x, int y, int wrapWidth, int lines, int color) {
		MinecraftGame game = MinecraftGame.getInstance();
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<String> strings = game.fontRenderer.wrapStringToWidthAsList(string, wrapWidth);

		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			String line = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				line += "...";
			}
			int x1 = x;
			if (game.fontRenderer.isRightToLeft()) {
				int width = game.fontRenderer.method_1727(game.fontRenderer.mirror(line));
				x1 += (float) (wrapWidth - width);
			}
			game.fontRenderer.drawWithShadow(line, x1, y + i * game.fontRenderer.FONT_HEIGHT, color);
		}
	}
}
