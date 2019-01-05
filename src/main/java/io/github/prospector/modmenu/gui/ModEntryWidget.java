package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.fabric.impl.resources.ModResourcePackUtil;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.ModInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

public class ModEntryWidget extends EntryListWidget.Entry {
	private static final Logger LOGGER = LogManager.getLogger();
	private final MinecraftClient client;
	public ModContainer container;
	public ModInfo info;
	public ModListWidget list;
	public final Identifier iconLocation;
	public final NativeImageBackedTexture nativeImageBackedTexture;
	public static final Identifier unknownIcon = new Identifier("textures/misc/unknown_pack.png");

	public ModEntryWidget(ModContainer container, ModListWidget list) {
		this.container = container;
		this.list = list;
		this.info = container.getInfo();
		this.client = MinecraftClient.getInstance();
		this.iconLocation = new Identifier("modmenu", info.getId() + "_icon");
		this.nativeImageBackedTexture = this.getNativeImageBackedTexture();
	}

	@Override
	public void draw(int width, int height, int var3, int var4, boolean var5, float var6) {
		int y = this.getY();
		int x = this.getX();
		if (this.equals(list.selected)) {
			Drawable.drawRect(x - 2, y - 2, x - 2 + width - 15, y - 2 + 36, 0xFF808080);
			Drawable.drawRect(x - 1, y - 1, x - 3 + width - 15, y - 3 + 36, 0xFF000000);
		}
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.client.getTextureManager().bindTexture(this.nativeImageBackedTexture != null ? this.iconLocation : unknownIcon);
		GlStateManager.enableBlend();
		Drawable.drawTexturedRect(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
		GlStateManager.disableBlend();
		this.client.fontRenderer.draw(info.getName(), (float) (x + 32 + 3), (float) (y + 1), 0xFFFFFF);
		RenderUtils.drawWrappedString(info.getDescription(), (x + 32 + 3 + 4), (y + 11), width - 32 - 3 - 25 - 4, 2, 0x808080);
	}

	private NativeImageBackedTexture getNativeImageBackedTexture() {
		try {
			InputStream inputStream = ModResourcePackUtil.class.getClassLoader().getResourceAsStream("assets/" + info.getId() + "/icon.png");
			if (inputStream == null) {
				if (info.getId().equals("fabricloader") || info.getId().equals("fabric")) {
					inputStream = ModResourcePackUtil.class.getClassLoader().getResourceAsStream("assets/" + ModMenu.MOD_ID + "/fabric_icon.png");
				} else {
					inputStream = ModResourcePackUtil.class.getClassLoader().getResourceAsStream("assets/" + ModMenu.MOD_ID + "/grey_fabric_icon.png");
				}
			}
			Throwable var3 = null;

			NativeImageBackedTexture var6;
			try {
				NativeImage var4 = NativeImage.fromInputStream(inputStream);
				Validate.validState(var4.getHeight() == var4.getWidth(), "Must be square icon");
				NativeImageBackedTexture var5 = new NativeImageBackedTexture(var4);
				this.client.getTextureManager().registerTexture(this.iconLocation, var5);
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

	@Override
	public boolean mouseClicked(double v, double v1, int i) {
		list.selected = this;
		return true;
	}
}
