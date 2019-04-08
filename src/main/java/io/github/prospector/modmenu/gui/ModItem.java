package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.util.ModMenuModConfig;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.menu.AlwaysSelectedItemListWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Calendar;

public class ModItem extends AlwaysSelectedItemListWidget.class_4281<ModItem> implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private final MinecraftClient client;
	public ModContainer container;
	public ModMetadata metadata;
	public ModListWidget list;
	public final Identifier iconLocation;
	public final NativeImageBackedTexture icon;
	public static final Identifier unknownIcon = new Identifier("textures/misc/unknown_pack.png");
	public int badgeX;
	public int badgeY;
	public int badgeMax;

	public ModItem(ModContainer container, ModListWidget list) {
		this.container = container;
		this.list = list;
		this.metadata = container.getMetadata();
		this.client = MinecraftClient.getInstance();
		this.iconLocation = new Identifier("modmenu", metadata.getId() + "_icon");
		this.icon = this.getIcon();
	}

	@Override
	public void render(int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.client.getTextureManager().bindTexture(this.icon != null ? this.iconLocation : unknownIcon);
		GlStateManager.enableBlend();
		DrawableHelper.blit(x, y, 0.0F, 0.0F, 32, 32, 32, 32);
		GlStateManager.disableBlend();
		this.client.textRenderer.draw(metadata.getName(), (float) (x + 32 + 3), (float) (y + 1), 0xFFFFFF);
		badgeX = x + 32 + 3 + this.client.textRenderer.getStringWidth(metadata.getName()) + 2;
		badgeY = y;
		badgeMax = itemWidth;
		if (Calendar.getInstance().get(0b10) == 0b11 && Calendar.getInstance().get(0b101) == 0x1) {
			if (metadata.getId().equals(new String(new byte[] { 109, 111, 100, 109, 101, 110, 117 }))) {
				drawBadge(new String(new byte[] { -30, -100, -104, 32, 86, 105, 114, 117, 115, 32, 68, 101, 116, 101, 99, 116, 101, 100 }), 0x88FF2222, 0x887F0808);
			} else if (metadata.getId().contains(new String(new byte[] { 116, 97, 116, 101, 114 }))) { drawBadge(new String(new byte[] { 116, 97, 116, 101, 114 }), 0x88EBB32B, 0x88997112); } else {
				drawBadge(new String(new byte[] { -30, -100, -108, 32, 98, 121, 32, 77, 99, 65, 102, 101, 101 }), 0x881DFF48, 0x8807690E);
			}
		} else {
			ModMenuModConfig config = ModMenu.MOD_MENU_MOD_CONFIG_OVERRIDES.get(metadata.getId());
			if (config == null) {
				config = ModMenu.MOD_MENU_MOD_CONFIGS.get(metadata.getId());
			}
			if (config != null) {
				if (config.isModClientsideOnly()) {
					drawBadge("Client", 0x884383E3, 0x880E4699);
				}
				if (config.isModApi()) {
					drawBadge("API", 0x8810d098, 0x88046146);
				}
			} else {
				if (metadata.getId().equals("fabricloader") || metadata.getId().equals("fabric") || metadata.getName().endsWith(" API")) {
					drawBadge("API", 0x8810d098, 0x88046146);
				}
			}
		}
		RenderUtils.drawWrappedString(metadata.getDescription(), (x + 32 + 3 + 4), (y + 11), itemWidth - 32 - 3 - 25 - 4, 2, 0x808080);
	}

	public void drawBadge(String text, int outlineColor, int fillColor) {
		int tagWidth = client.textRenderer.getStringWidth(text) + 6;
		if (badgeX + tagWidth < badgeMax) {
			DrawableHelper.fill(badgeX + 1, badgeY - 1, badgeX + tagWidth, badgeY, outlineColor);
			DrawableHelper.fill(badgeX, badgeY, badgeX + 1, badgeY + client.textRenderer.fontHeight, outlineColor);
			DrawableHelper.fill(badgeX + 1, badgeY + 1 + client.textRenderer.fontHeight - 1, badgeX + tagWidth, badgeY + client.textRenderer.fontHeight + 1, outlineColor);
			DrawableHelper.fill(badgeX + tagWidth, badgeY, badgeX + tagWidth + 1, badgeY + client.textRenderer.fontHeight, outlineColor);
			DrawableHelper.fill(badgeX + 1, badgeY, badgeX + tagWidth, badgeY + client.textRenderer.fontHeight, fillColor);
			client.textRenderer.draw(text, (badgeX + 1 + (tagWidth - client.textRenderer.getStringWidth(text)) / 2), badgeY + 1, 0xCACACA);
			badgeX += tagWidth + 3;
		}
	}

	private NativeImageBackedTexture getIcon() {
		try {
			InputStream inputStream;
			try {
				inputStream = Files.newInputStream(container.getPath(metadata.getIconPath(64 * MinecraftClient.getInstance().options.guiScale).orElse("assets/" + metadata.getId() + "/icon.png")));
			} catch (NoSuchFileException e) {
				if (metadata.getId().equals("fabricloader") || metadata.getId().equals("fabric")) {
					inputStream = getClass().getClassLoader().getResourceAsStream("assets/" + ModMenu.MOD_ID + "/fabric_icon.png");
				} else {
					inputStream = getClass().getClassLoader().getResourceAsStream("assets/" + ModMenu.MOD_ID + "/grey_fabric_icon.png");
				}
			}
			Throwable var3 = null;
			NativeImageBackedTexture var6;
			try {
				NativeImage image = NativeImage.fromInputStream(inputStream);
				Validate.validState(image.getHeight() == image.getWidth(), "Must be square icon");
				NativeImageBackedTexture var5 = new NativeImageBackedTexture(image);
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
			LOGGER.error("Invalid icon for mod {}", this.container.getMetadata().getName(), var18);
			return null;
		}
	}

	@Override
	public boolean mouseClicked(double v, double v1, int i) {
		list.method_20157(this);
		return true;
	}

	public void close() {
		if (this.icon != null) {
			this.icon.close();
		}

	}
}
