package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.util.BadgeRenderer;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.menu.AlwaysSelectedEntryListWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

public class ModListEntry extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private final MinecraftClient client;
	public ModContainer container;
	public ModMetadata metadata;
	public ModListWidget list;
	public final Identifier iconLocation;
	public final NativeImageBackedTexture icon;
	public static final Identifier unknownIcon = new Identifier("textures/misc/unknown_pack.png");

	public ModListEntry(ModContainer container, ModListWidget list) {
		this.container = container;
		this.list = list;
		this.metadata = container.getMetadata();
		this.client = MinecraftClient.getInstance();
		this.iconLocation = new Identifier("modmenu", metadata.getId() + "_icon");
		this.icon = this.getIcon();
	}

	@Override
	public void render(int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.client.getTextureManager().bindTexture(this.icon != null ? this.iconLocation : unknownIcon);
		GlStateManager.enableBlend();
		DrawableHelper.blit(x, y, 0.0F, 0.0F, 32, 32, 32, 32);
		GlStateManager.disableBlend();
		this.client.textRenderer.draw(metadata.getName(), (float) (x + 32 + 3), (float) (y + 1), 0xFFFFFF);
		new BadgeRenderer(x + 32 + 3 + this.client.textRenderer.getStringWidth(metadata.getName()) + 2, y, rowWidth, metadata, list.parent).draw(mouseX, mouseY);
		RenderUtils.drawWrappedString(metadata.getDescription(), (x + 32 + 3 + 4), (y + client.textRenderer.fontHeight + 2), rowWidth - 32 - 3 - 25 - 4, 2, 0x808080);
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
		list.select(this);
		return true;
	}

	public void close() {
		if (this.icon != null) {
			this.icon.close();
		}

	}
}
