package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

public class ModListEntry extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> {
	public static final Identifier UNKNOWN_ICON = new Identifier("textures/misc/unknown_pack.png");

	protected final MinecraftClient client;
	public final Mod mod;
	protected final ModListWidget list;
	protected Identifier iconLocation;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = MinecraftClient.getInstance();
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		x += getXOffset();
		rowWidth -= getXOffset();
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		if ("java".equals(mod.getId())) {
			DrawingUtil.drawRandomVersionBackground(mod, matrices, x, y, iconSize, iconSize);
		}
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.bindIconTexture();
		RenderSystem.enableBlend();
		DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
		RenderSystem.disableBlend();
		Text name = new LiteralText(mod.getName());
		StringVisitable trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		TextRenderer font = this.client.textRenderer;
		if (font.getWidth(name) > maxNameWidth) {
			StringVisitable ellipsis = StringVisitable.plain("...");
			trimmedName = StringVisitable.concat(font.trimToWidth(name, maxNameWidth - font.getWidth(ellipsis)), ellipsis);
		}
		font.draw(matrices, Language.getInstance().reorder(trimmedName), x + iconSize + 3, y + 1, 0xFFFFFF);
		if (!ModMenuConfig.HIDE_BADGES.getValue()) {
			new ModBadgeRenderer(x + iconSize + 3 + font.getWidth(name) + 2, y, x + rowWidth, mod, list.getParent()).draw(matrices, mouseX, mouseY);
		}
		if (!ModMenuConfig.COMPACT_LIST.getValue()) {
			String summary = mod.getSummary();
			String translatableSummaryKey = "modmenu.summaryTranslation." + mod.getId();
			String translatableDescriptionKey = "modmenu.descriptionTranslation." + mod.getId();
			if (I18n.hasTranslation(translatableSummaryKey)) {
				summary = I18n.translate(translatableSummaryKey);
			} else if (I18n.hasTranslation(translatableDescriptionKey)) {
				summary = I18n.translate(translatableDescriptionKey);
			}
			DrawingUtil.drawWrappedString(matrices, summary, (x + iconSize + 3 + 4), (y + client.textRenderer.fontHeight + 2), rowWidth - iconSize - 7, 2, 0x808080);
		} else {
			DrawingUtil.drawWrappedString(matrices, mod.getPrefixedVersion(), (x + iconSize + 3), (y + client.textRenderer.fontHeight + 2), rowWidth - iconSize - 7, 2, 0x808080);
		}
	}

	@Override
	public boolean mouseClicked(double v, double v1, int i) {
		list.select(this);
		return true;
	}

	public Mod getMod() {
		return mod;
	}

	public void bindIconTexture() {
		if (this.iconLocation == null) {
			this.iconLocation = new Identifier(ModMenu.MOD_ID, mod.getId() + "_icon");
			NativeImageBackedTexture icon = mod.getIcon(list.getIconHandler(), 64 * this.client.options.guiScale);
			if (icon != null) {
				this.client.getTextureManager().registerTexture(this.iconLocation, icon);
			} else {
				this.iconLocation = UNKNOWN_ICON;
			}
		}
		this.client.getTextureManager().bindTexture(this.iconLocation);
	}

	public int getXOffset() {
		return 0;
	}
}
