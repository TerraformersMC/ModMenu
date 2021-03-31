package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModListEntry extends ObjectSelectionList.Entry<ModListEntry> {
	public static final ResourceLocation UNKNOWN_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
	private static final Logger LOGGER = LogManager.getLogger();

	protected final Minecraft client;
	public final Mod mod;
	protected final ModListWidget list;
	protected ResourceLocation iconLocation;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = Minecraft.getInstance();
	}

	@Override
	public void render(PoseStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		x += getXOffset();
		rowWidth -= getXOffset();
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		if ("java".equals(mod.getId())) {
			DrawingUtil.drawRandomVersionBackground(mod, matrices, x, y, iconSize, iconSize);
		}
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.bindIconTexture();
		RenderSystem.enableBlend();
		GuiComponent.blit(matrices, x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
		RenderSystem.disableBlend();
		Component name = new TextComponent(mod.getName());
		FormattedText trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		Font font = this.client.font;
		if (font.width(name) > maxNameWidth) {
			FormattedText ellipsis = FormattedText.of("...");
			trimmedName = FormattedText.composite(font.substrByWidth(name, maxNameWidth - font.width(ellipsis)), ellipsis);
		}
		font.draw(matrices, Language.getInstance().getVisualOrder(trimmedName), x + iconSize + 3, y + 1, 0xFFFFFF);
		if (!ModMenuConfig.HIDE_BADGES.getValue()) {
			new ModBadgeRenderer(x + iconSize + 3 + font.width(name) + 2, y, x + rowWidth, mod, list.getParent()).draw(matrices, mouseX, mouseY);
		}
		if (!ModMenuConfig.COMPACT_LIST.getValue()) {
			String summary = mod.getSummary();
			String translatableSummaryKey = "modmenu.summaryTranslation." + mod.getId();
			String translatableDescriptionKey = "modmenu.descriptionTranslation." + mod.getId();
			if (I18n.exists(translatableSummaryKey)) {
				summary = I18n.get(translatableSummaryKey);
			} else if (I18n.exists(translatableDescriptionKey)) {
				summary = I18n.get(translatableDescriptionKey);
			}
			DrawingUtil.drawWrappedString(matrices, summary, (x + iconSize + 3 + 4), (y + client.font.lineHeight + 2), rowWidth - iconSize - 7, 2, 0x808080);
		} else {
			DrawingUtil.drawWrappedString(matrices, "v" + mod.getVersion(), (x + iconSize + 3), (y + client.font.lineHeight + 2), rowWidth - iconSize - 7, 2, 0x808080);
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
			this.iconLocation = new ResourceLocation(ModMenu.MOD_ID, mod.getId() + "_icon");
			DynamicTexture icon = mod.getIcon(list.getIconHandler(), 64 * Minecraft.getInstance().options.guiScale);
			if (icon != null) {
				this.client.getTextureManager().register(this.iconLocation, icon);
			} else {
				this.iconLocation = UNKNOWN_ICON;
			}
		}
		this.client.getTextureManager().bind(this.iconLocation);
	}

	public int getXOffset() {
		return 0;
	}
}
