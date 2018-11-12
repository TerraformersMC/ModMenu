package prospector.modmenu;

import com.mojang.blaze3d.platform.GlStateManager;
import com.sun.istack.internal.Nullable;
import net.fabricmc.fabric.resources.ModResourcePackUtil;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.ModInfo;
import net.minecraft.client.MinecraftGame;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.WidgetListMulti;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.List;

public class WidgetModEntry extends WidgetListMulti.class_351 {
	private static final Logger LOGGER = LogManager.getLogger();
	private final MinecraftGame game;
	public ModContainer container;
	public ModInfo info;
	private final Identifier iconLocation;
	private final NativeImageBackedTexture nativeImageBackedTexture;
	private static final Identifier unknownIcon = new Identifier("textures/misc/unknown_pack.png");

	public WidgetModEntry(ModContainer container) {
		this.container = container;
		this.info = container.getInfo();
		this.game = MinecraftGame.getInstance();
		this.iconLocation = new Identifier("modmenu", "modicon");
		this.nativeImageBackedTexture = this.getNativeImageBackedTexture();
	}

	@Override
	public void drawEntry(int i, int i1, int i2, int i3, boolean b, float v) {
		int var7 = this.method_1906();
		int var8 = this.method_1907();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.game.getTextureManager().bindTexture(this.nativeImageBackedTexture != null ? this.iconLocation : unknownIcon);
		GlStateManager.enableBlend();
		Drawable.drawTexturedRect(var8, var7, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
		GlStateManager.disableBlend();
		this.game.fontRenderer.drawWithShadow(info.getName(), (float) (var8 + 32 + 3), (float) (var7 + 1), 0xFFFFFF);
		this.game.fontRenderer.drawWithShadow(" (" + info.getId() + ")", (float) (var8 + 32 + 3) + game.fontRenderer.method_1727(info.getName()), (float) (var7 + 1), 0xAAAAAA);
		drawWrappedString(info.getDescription(), (var8 + 32 + 3), (var7 + 11), i - 40, 2, 0xAAAAAA);
	}

	public void drawWrappedString(String string, int x, int y, int wrapWidth, int lines, int color) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<String> strings = game.fontRenderer.wrapStringToWidthAsList(string, wrapWidth);

		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			String line = strings.get(i);
			if (i == lines - 1) {
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

	@Nullable
	private NativeImageBackedTexture getNativeImageBackedTexture() {
		try {
			InputStream inputStream = ModResourcePackUtil.openDefault(container.getInfo(), "pack.png");
			Throwable var3 = null;

			NativeImageBackedTexture var6;
			try {
				NativeImage var4 = NativeImage.fromInputStream(inputStream);
				Validate.validState(var4.getHeight() == var4.getWidth(), "Must be square icon");
				NativeImageBackedTexture var5 = new NativeImageBackedTexture(var4);
				this.game.getTextureManager().registerTexture(this.iconLocation, var5);
				var6 = var5;
			} catch (Throwable var16) {
				var3 = var16;
				throw var16;
			} finally {
				if (inputStream != null) {
					if (var3 != null) {
						try {
							inputStream.close();
						} catch (Throwable var15) {
							var3.addSuppressed(var15);
						}
					} else {
						inputStream.close();
					}
				}

			}

			return var6;
		} catch (Throwable var18) {
			LOGGER.error("Invalid icon for mod {}", this.container.getInfo().getName(), var18);
			return null;
		}
	}
}
