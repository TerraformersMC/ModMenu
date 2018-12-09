package prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.fabric.resources.impl.ModResourcePackUtil;
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
import prospector.modmenu.util.RenderUtils;

import java.io.InputStream;

public class ModEntryWidget extends EntryListWidget.Entry {
	private static final Logger LOGGER = LogManager.getLogger();
	private final MinecraftClient client;
	public ModContainer container;
	public ModInfo info;
	public ModListWidget list;
	private final Identifier iconLocation;
	private final NativeImageBackedTexture nativeImageBackedTexture;
	private static final Identifier unknownIcon = new Identifier("textures/misc/unknown_pack.png");

	public ModEntryWidget(ModContainer container, ModListWidget list) {
		this.container = container;
		this.list = list;
		this.info = container.getInfo();
		this.client = MinecraftClient.getInstance();
		this.iconLocation = new Identifier("modmenu", "modicon");
		this.nativeImageBackedTexture = this.getNativeImageBackedTexture();
	}

	@Override
	public void drawEntry(int width, int height, int i2, int i3, boolean b, float v) {
		int y = this.method_1906();
		int x = this.method_1907();
		if (container.equals(list.selected)) {
			Drawable.drawRect(x - 2, y - 2, x - 2 + width - 15, y - 2 + 36, 0xFF808080);
			Drawable.drawRect(x - 1, y - 1, x - 3 + width - 15, y - 3 + 36, 0xFF000000);
		}
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.client.getTextureManager().bindTexture(this.nativeImageBackedTexture != null ? this.iconLocation : unknownIcon);
		GlStateManager.enableBlend();
		Drawable.drawTexturedRect(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
		GlStateManager.disableBlend();
		this.client.fontRenderer.drawWithShadow(info.getName(), (float) (x + 32 + 3), (float) (y + 1), 0xFFFFFF);
		this.client.fontRenderer.drawWithShadow(" (" + info.getId() + ")", (float) (x + 32 + 3) + client.fontRenderer.getStringWidth(info.getName()), (float) (y + 1), 0xAAAAAA);
		RenderUtils.drawWrappedString(info.getDescription(), (x + 32 + 3 + 4), (y + 11), width - 32 - 3 - 25 - 4, 2, 0x808080);
	}

	private NativeImageBackedTexture getNativeImageBackedTexture() {
		try {
			InputStream inputStream = ModResourcePackUtil.openDefault(container.getInfo(), "pack.png");
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
		list.selected = container;
		return true;
	}
}
