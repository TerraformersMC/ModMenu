package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.util.BadgeRenderer;
import io.github.prospector.modmenu.util.FabricHardcodedBsUtil;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ModListEntry extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> implements AutoCloseable {
	public static final Identifier UNKNOWN_ICON = new Identifier("textures/misc/unknown_pack.png");
	private static final Logger LOGGER = LogManager.getLogger();

	private static final Map<Identifier, NativeImageBackedTexture> MOD_ICONS = new HashMap<>();

	protected final MinecraftClient client;
	protected final ModContainer container;
	protected final ModMetadata metadata;
	protected final ModListWidget list;
	protected final Identifier iconLocation;
	protected final NativeImageBackedTexture icon;

	public ModListEntry(ModContainer container, ModListWidget list) {
		this.container = container;
		this.list = list;
		this.metadata = container.getMetadata();
		this.client = MinecraftClient.getInstance();
		this.iconLocation = new Identifier("modmenu", metadata.getId() + "_icon");
		this.icon = MOD_ICONS.computeIfAbsent(this.iconLocation, (id) -> this.createIcon());
	}

	@Override
	public void render(int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		x += getXOffset();
		rowWidth -= getXOffset();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.client.getTextureManager().bindTexture(this.icon != null ? this.iconLocation : UNKNOWN_ICON);
		GlStateManager.enableBlend();
		DrawableHelper.blit(x, y, 0.0F, 0.0F, 32, 32, 32, 32);
		GlStateManager.disableBlend();
		String name = metadata.getName();
		name = FabricHardcodedBsUtil.formatFabricModuleName(name);
		String trimmedName = name;
		int maxNameWidth = rowWidth - 32 - 3;
		TextRenderer font = this.client.textRenderer;
		if (font.getStringWidth(name) > maxNameWidth) {
			trimmedName = font.trimToWidth(name, maxNameWidth - font.getStringWidth("...")) + "...";
		}
		font.draw(trimmedName, x + 32 + 3, y + 1, 0xFFFFFF);
		new BadgeRenderer(x + 32 + 3 + font.getStringWidth(name) + 2, y, x + rowWidth, container, list.getParent()).draw(mouseX, mouseY);
		String description = metadata.getDescription();
		if (description.isEmpty() && FabricHardcodedBsUtil.getFabricDescriptions().containsKey(metadata.getId())) {
			description = FabricHardcodedBsUtil.getFabricModuleDescription(metadata.getId());
		}
		RenderUtils.drawWrappedString(description, (x + 32 + 3 + 4), (y + client.textRenderer.fontHeight + 2), rowWidth - 32 - 7, 2, 0x808080);
	}

	private NativeImageBackedTexture createIcon() {
		try {
			InputStream inputStream;
			try {
				inputStream = Files.newInputStream(container.getPath(metadata.getIconPath(64 * MinecraftClient.getInstance().options.guiScale).orElse("assets/" + metadata.getId() + "/icon.png")));
			} catch (NoSuchFileException e) {
				if (FabricHardcodedBsUtil.getFabricMods().contains(metadata.getId())) {
					inputStream = getClass().getClassLoader().getResourceAsStream("assets/" + ModMenu.MOD_ID + "/fabric_icon.png");
				} else {
					inputStream = getClass().getClassLoader().getResourceAsStream("assets/" + ModMenu.MOD_ID + "/grey_fabric_icon.png");
				}
			}
			Throwable var3 = null;
			NativeImageBackedTexture var6;
			try {
				NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
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

	@Override
	public void close() {
		if (this.icon != null) {
			this.icon.close();
		}
	}

	public ModMetadata getMetadata() {
		return metadata;
	}

	public Identifier getIconLocation() {
		return iconLocation;
	}

	public NativeImageBackedTexture getIcon() {
		return icon;
	}

	public int getXOffset() {
		return 0;
	}
}
