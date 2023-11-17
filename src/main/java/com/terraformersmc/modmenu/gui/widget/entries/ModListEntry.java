package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateAvailableBadge;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.ModMenuScreenTexts;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Util;

public class ModListEntry extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> {
	public static final Identifier UNKNOWN_ICON = new Identifier("textures/misc/unknown_pack.png");
	private static final Identifier MOD_CONFIGURATION_ICON = new Identifier("modmenu", "textures/gui/mod_configuration.png");
	private static final Identifier ERROR_ICON = new Identifier("minecraft", "textures/gui/world_selection.png");

	protected final MinecraftClient client;
	public final Mod mod;
	protected final ModListWidget list;
	protected Identifier iconLocation;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;
	protected long sinceLastClick;

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = MinecraftClient.getInstance();
	}

	@Override
	public Text getNarration() {
		return Text.literal(mod.getTranslatedName());
	}

	@Override
	public void render(DrawContext DrawContext, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		x += getXOffset();
		rowWidth -= getXOffset();
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		String modId = mod.getId();
		if ("java".equals(modId)) {
			DrawingUtil.drawRandomVersionBackground(mod, DrawContext, x, y, iconSize, iconSize);
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		DrawContext.drawTexture(this.getIconTexture(), x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
		RenderSystem.disableBlend();
		Text name = Text.literal(mod.getTranslatedName());
		StringVisitable trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		TextRenderer font = this.client.textRenderer;
		if (font.getWidth(name) > maxNameWidth) {
			StringVisitable ellipsis = StringVisitable.plain("...");
			trimmedName = StringVisitable.concat(font.trimToWidth(name, maxNameWidth - font.getWidth(ellipsis)), ellipsis);
		}
		DrawContext.drawText(font, Language.getInstance().reorder(trimmedName), x + iconSize + 3, y + 1, 0xFFFFFF, false);
		var updateBadgeXOffset = 0;
		if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue().contains(modId) && (mod.getModrinthData() != null || mod.getChildHasUpdate())) {
			UpdateAvailableBadge.renderBadge(DrawContext, x + iconSize + 3 + font.getWidth(name) + 2, y);
			updateBadgeXOffset = 11;
		}
		if (!ModMenuConfig.HIDE_BADGES.getValue()) {
			new ModBadgeRenderer(x + iconSize + 3 + font.getWidth(name) + 2 + updateBadgeXOffset, y, x + rowWidth, mod, list.getParent()).draw(DrawContext, mouseX, mouseY);
		}
		if (!ModMenuConfig.COMPACT_LIST.getValue()) {
			String summary = mod.getSummary();
			DrawingUtil.drawWrappedString(DrawContext, summary, (x + iconSize + 3 + 4), (y + client.textRenderer.fontHeight + 2), rowWidth - iconSize - 7, 2, 0x808080);
		} else {
			DrawingUtil.drawWrappedString(DrawContext, mod.getPrefixedVersion(), (x + iconSize + 3), (y + client.textRenderer.fontHeight + 2), rowWidth - iconSize - 7, 2, 0x808080);
		}

		if (!(this instanceof ParentEntry) && ModMenuConfig.QUICK_CONFIGURE.getValue() && (this.list.getParent().getModHasConfigScreen().get(modId) || this.list.getParent().modScreenErrors.containsKey(modId))) {
			final int textureSize = ModMenuConfig.COMPACT_LIST.getValue() ? (int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) : 256;
			if (this.client.options.getTouchscreen().getValue() || hovered) {
				DrawContext.fill(x, y, x + iconSize, y + iconSize, -1601138544);
				boolean hoveringIcon = mouseX - x < iconSize;
				int v = hoveringIcon ? iconSize : 0;
				if (this.list.getParent().modScreenErrors.containsKey(modId)) {
					DrawContext.drawTexture(ERROR_ICON, x, y, 96.0F, (float) v, iconSize, iconSize, textureSize, textureSize);
					if (hoveringIcon) {
						Throwable e = this.list.getParent().modScreenErrors.get(modId);
						this.list.getParent().setTooltip(this.client.textRenderer.wrapLines(ModMenuScreenTexts.configureError(modId, e), 175));
					}
				} else {
					DrawContext.drawTexture(MOD_CONFIGURATION_ICON, x, y, 0.0F, (float) v, iconSize, iconSize, textureSize, textureSize);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int delta) {
		list.select(this);
		if (ModMenuConfig.QUICK_CONFIGURE.getValue() && this.list.getParent().getModHasConfigScreen().get(this.mod.getId())) {
			int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
			if (mouseX - list.getRowLeft() <= iconSize) {
				this.openConfig();
			} else if (Util.getMeasuringTimeMs() - this.sinceLastClick < 250) {
				this.openConfig();
			}
		}
		this.sinceLastClick = Util.getMeasuringTimeMs();
		return true;
	}

	public void openConfig() {
		MinecraftClient.getInstance().setScreen(ModMenu.getConfigScreen(mod.getId(), list.getParent()));
	}

	public Mod getMod() {
		return mod;
	}

	public Identifier getIconTexture() {
		if (this.iconLocation == null) {
			this.iconLocation = new Identifier(ModMenu.MOD_ID, mod.getId() + "_icon");
			NativeImageBackedTexture icon = mod.getIcon(list.getFabricIconHandler(), 64 * this.client.options.getGuiScale().getValue());
			if (icon != null) {
				this.client.getTextureManager().registerTexture(this.iconLocation, icon);
			} else {
				this.iconLocation = UNKNOWN_ICON;
			}
		}
		return iconLocation;
	}

	public int getXOffset() {
		return 0;
	}
}
