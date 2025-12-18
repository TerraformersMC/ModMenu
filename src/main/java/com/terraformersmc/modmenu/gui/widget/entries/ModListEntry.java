package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateAvailableBadge;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;

public class ModListEntry extends ObjectSelectionList.Entry<ModListEntry> {
	public static final Identifier UNKNOWN_ICON = Identifier.withDefaultNamespace("textures/misc/unknown_pack.png");
	private static final Identifier MOD_CONFIGURATION_ICON = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "textures/gui/mod_configuration.png");
	private static final Identifier ERROR_ICON = Identifier.withDefaultNamespace("world_list/error");
	private static final Identifier ERROR_HIGHLIGHTED_ICON = Identifier.withDefaultNamespace("world_list/error_highlighted");

	protected final Minecraft client;
	public final Mod mod;
	protected final ModListWidget list;
	protected Identifier iconLocation;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;
	protected long sinceLastClick;
	protected int yOffset = 0;

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = Minecraft.getInstance();
	}

	@Override
	public Component getNarration() {
		return Component.literal(mod.getTranslatedName());
	}

	@Override
	public void renderContent(
		GuiGraphics drawContext,
		int mouseX,
		int mouseY,
		boolean hovered,
		float delta
	) {
        int x = this.getX() + this.getXOffset();
        int y = this.getContentY() + this.getYOffset();
        int rowWidth = this.getContentWidth();
//        int rowHeight = this.getContentHeight();
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		String modId = mod.getId();
		if ("java".equals(modId)) {
			DrawingUtil.drawRandomVersionBackground(mod, drawContext, x, y, iconSize, iconSize);
		}

		drawContext.blit(
			RenderPipelines.GUI_TEXTURED,
			this.getIconTexture(),
			x,
			y,
			0.0F,
			0.0F,
			iconSize,
			iconSize,
			iconSize,
			iconSize,
			ARGB.white(1.0F)
		);

		Component name = Component.literal(mod.getTranslatedName());
		FormattedText trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		Font font = this.client.font;
		if (font.width(name) > maxNameWidth) {
			FormattedText ellipsis = FormattedText.of("...");
			trimmedName = FormattedText.composite(font.substrByWidth(name, maxNameWidth - font.width(ellipsis)), ellipsis);
		}

		drawContext.drawString(font,
			Language.getInstance().getVisualOrder(trimmedName),
			x + iconSize + 3,
			y + 1,
			0xFFFFFFFF
		);

		var updateBadgeXOffset = 0;
		if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue()
			.contains(modId) && (mod.hasUpdate() || mod.getChildHasUpdate())) {
			UpdateAvailableBadge.renderBadge(drawContext, x + iconSize + 3 + font.width(name) + 2, y);
			updateBadgeXOffset = 11;
		}

		if (!ModMenuConfig.HIDE_BADGES.getValue()) {
			new ModBadgeRenderer(
				x + iconSize + 3 + font.width(name) + 2 + updateBadgeXOffset,
				y,
				x + rowWidth,
				mod,
				list.getParent()
			).draw(drawContext, mouseX, mouseY);
		}

		if (!ModMenuConfig.COMPACT_LIST.getValue()) {
			String summary = mod.getSummary();
			DrawingUtil.drawWrappedString(
				drawContext,
				summary,
				(x + iconSize + 3 + 4),
				(y + client.font.lineHeight + 2),
				rowWidth - iconSize - 7,
				2,
				0xFF808080
			);
		} else {
			DrawingUtil.drawWrappedString(
				drawContext,
				mod.getPrefixedVersion(),
				(x + iconSize + 3),
				(y + client.font.lineHeight + 2),
				rowWidth - iconSize - 7,
				2,
				0xFF808080
			);
		}

		if (!(this instanceof ParentEntry) && ModMenuConfig.QUICK_CONFIGURE.getValue() && (this.list.getParent().getModHasConfigScreen(modId) || this.list.getParent().modScreenErrors.containsKey(modId))) {
			final int textureSize = ModMenuConfig.COMPACT_LIST.getValue() ? (int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) : 256;
			if (this.client.options.touchscreen().get() || hovered) {
				drawContext.fill(x, y, x + iconSize, y + iconSize, -1601138544);
				boolean hoveringIcon = mouseX - x < iconSize;
				if (this.list.getParent().modScreenErrors.containsKey(modId)) {
					drawContext.blitSprite(
						RenderPipelines.GUI_TEXTURED,
						hoveringIcon ? ERROR_HIGHLIGHTED_ICON : ERROR_ICON,
						x,
						y,
						iconSize,
						iconSize
					);
//					if (hoveringIcon) {
//						Throwable e = this.list.getParent().modScreenErrors.get(modId);
//						this.list.getParent().setTooltip(this.client.textRenderer.wrapLines(ModMenuScreenTexts.configureError(modId, e), 175));
//					}
				} else {
					int v = hoveringIcon ? iconSize : 0;
					drawContext.blit(
						RenderPipelines.GUI_TEXTURED,
						MOD_CONFIGURATION_ICON,
						x,
						y,
						0.0F,
						(float) v,
						iconSize,
						iconSize,
						textureSize,
						textureSize,
						ARGB.white(1.0F)
					);
				}
				if (hoveringIcon) {
					drawContext.requestCursor(this.shouldTakeFocusAfterInteraction() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
		list.select(this);
		if (ModMenuConfig.QUICK_CONFIGURE.getValue() && this.list.getParent().getModHasConfigScreen(this.mod.getId())) {
			int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
			if (click.x() - list.getRowLeft() <= iconSize) {
				this.openConfig();
			} else if (Util.getMillis() - this.sinceLastClick < 250) {
				this.openConfig();
			}
		}

		this.sinceLastClick = Util.getMillis();
		return true;
	}

	public void openConfig() {
		this.list.getParent().safelyOpenConfigScreen(mod.getId());
	}

	public Mod getMod() {
		return mod;
	}

	public Identifier getIconTexture() {
		if (this.iconLocation == null) {
			this.iconLocation = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, mod.getId() + "_icon");
			DynamicTexture icon = mod.getIcon(list.getFabricIconHandler(), 64 * this.client.options.guiScale().get());
			this.client.getTextureManager().register(this.iconLocation, icon);
		}

		return iconLocation;
	}

	public int getXOffset() {
		return 0;
	}

	public void setYOffset(int offset) {
		this.yOffset = offset;
	}

	public int getYOffset() {
		return this.yOffset;
	}
}
