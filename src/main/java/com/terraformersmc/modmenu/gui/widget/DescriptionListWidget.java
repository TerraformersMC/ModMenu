package com.terraformersmc.modmenu.gui.widget;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DescriptionListWidget extends AbstractSelectionList<DescriptionListWidget.DescriptionEntry> {

	private final ModsScreen parent;
	private final Font textRenderer;
	private ModListEntry lastSelected = null;

	public DescriptionListWidget(Minecraft client, int width, int height, int top, int bottom, int entryHeight, ModsScreen parent) {
		super(client, width, height, top, bottom, entryHeight);
		this.parent = parent;
		this.textRenderer = client.font;
	}

	@Override
	public DescriptionEntry getSelected() {
		return null;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6 + x0;
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clearEntries();
			setScrollAmount(-Double.MAX_VALUE);
			if (lastSelected != null) {
				Mod mod = lastSelected.getMod();
				String description = mod.getDescription();
				String translatableDescriptionKey = "modmenu.descriptionTranslation." + mod.getId();
				if (I18n.exists(translatableDescriptionKey)) {
					description = I18n.get(translatableDescriptionKey);
				}
				if (!description.isEmpty()) {
					for (FormattedCharSequence line : textRenderer.split(new TextComponent(description.replaceAll("\n", "\n\n")), getRowWidth() - 5)) {
						children().add(new DescriptionEntry(line, this));
					}
				}

				Map<String, String> links = mod.getLinks();
				String sourceLink = mod.getSource();
				if ((!links.isEmpty() || sourceLink != null) && !ModMenuConfig.HIDE_MOD_LINKS.getValue()) {
					children().add(new DescriptionEntry(TextComponent.EMPTY.getVisualOrderText(), this));
					children().add(new DescriptionEntry(new TranslatableComponent("modmenu.links").getVisualOrderText(), this));

					if (sourceLink != null) {
						children().add(new LinkEntry(new TextComponent("  ").append(new TranslatableComponent("modmenu.source").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE)).getVisualOrderText(), sourceLink, this));
					}

					links.forEach((key, value) -> {
						children().add(new LinkEntry(new TextComponent("  ").append(new TranslatableComponent(key).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE)).getVisualOrderText(), value, this));
					});
				}

				Set<String> licenses = mod.getLicense();
				if (!ModMenuConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty()) {
					children().add(new DescriptionEntry(TextComponent.EMPTY.getVisualOrderText(), this));
					children().add(new DescriptionEntry(new TranslatableComponent("modmenu.license").getVisualOrderText(), this));

					for (String license : licenses) {
						children().add(new DescriptionEntry(new TextComponent("  " + license).getVisualOrderText(), this));
					}
				}

				if (!ModMenuConfig.HIDE_MOD_CREDITS.getValue()) {
					if ("minecraft".equals(mod.getId())) {
						children().add(new DescriptionEntry(TextComponent.EMPTY.getVisualOrderText(), this));
						children().add(new MojangCreditsEntry(new TranslatableComponent("modmenu.viewCredits").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE).getVisualOrderText(), this));
					} else {
						List<String> authors = mod.getAuthors();
						List<String> contributors = mod.getContributors();
						if (!authors.isEmpty() || !contributors.isEmpty()) {
							children().add(new DescriptionEntry(TextComponent.EMPTY.getVisualOrderText(), this));
							children().add(new DescriptionEntry(new TranslatableComponent("modmenu.credits").getVisualOrderText(), this));
							for (String author : authors) {
								children().add(new DescriptionEntry(new TextComponent("  " + author).getVisualOrderText(), this));
							}
							for (String contributor : contributors) {
								children().add(new DescriptionEntry(new TextComponent("  " + contributor).getVisualOrderText(), this));
							}
						}
					}
				}
			}
		}

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();

		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		RenderSystem.disableAlphaTest();
		RenderSystem.shadeModel(7425);
		RenderSystem.disableTexture();

		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(this.x0, (this.y0 + 4), 0.0D).

				uv(0.0F, 1.0F).

				color(0, 0, 0, 0).

				endVertex();
		bufferBuilder.vertex(this.x1, (this.y0 + 4), 0.0D).

				uv(1.0F, 1.0F).

				color(0, 0, 0, 0).

				endVertex();
		bufferBuilder.vertex(this.x1, this.y0, 0.0D).

				uv(1.0F, 0.0F).

				color(0, 0, 0, 255).

				endVertex();
		bufferBuilder.vertex(this.x0, this.y0, 0.0D).

				uv(0.0F, 0.0F).

				color(0, 0, 0, 255).

				endVertex();
		bufferBuilder.vertex(this.x0, this.y1, 0.0D).

				uv(0.0F, 1.0F).

				color(0, 0, 0, 255).

				endVertex();
		bufferBuilder.vertex(this.x1, this.y1, 0.0D).

				uv(1.0F, 1.0F).

				color(0, 0, 0, 255).

				endVertex();
		bufferBuilder.vertex(this.x1, (this.y1 - 4), 0.0D).

				uv(1.0F, 0.0F).

				color(0, 0, 0, 0).

				endVertex();
		bufferBuilder.vertex(this.x0, (this.y1 - 4), 0.0D).

				uv(0.0F, 0.0F).

				color(0, 0, 0, 0).

				endVertex();
		tessellator.end();

		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(this.x0, this.y1, 0.0D).

				color(0, 0, 0, 128).

				endVertex();
		bufferBuilder.vertex(this.x1, this.y1, 0.0D).

				color(0, 0, 0, 128).

				endVertex();
		bufferBuilder.vertex(this.x1, this.y0, 0.0D).

				color(0, 0, 0, 128).

				endVertex();
		bufferBuilder.vertex(this.x0, this.y0, 0.0D).

				color(0, 0, 0, 128).

				endVertex();
		tessellator.end();

		int k = this.getRowLeft();
		int l = this.y0 + 4 - (int) this.getScrollAmount();
		this.

				renderList(matrices, k, l, mouseX, mouseY, delta);
		this.

				renderScrollBar(bufferBuilder, tessellator);

		RenderSystem.enableTexture();
		RenderSystem.shadeModel(7424);
		RenderSystem.enableAlphaTest();
		RenderSystem.disableBlend();
	}

	public void renderScrollBar(BufferBuilder bufferBuilder, Tesselator tessellator) {
		int scrollbarStartX = this.getScrollbarPosition();
		int scrollbarEndX = scrollbarStartX + 6;
		int maxScroll = this.getMaxScroll();
		if (maxScroll > 0) {
			RenderSystem.disableTexture();
			int p = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
			p = Mth.clamp(p, 32, this.y1 - this.y0 - 8);
			int q = (int) this.getScrollAmount() * (this.y1 - this.y0 - p) / maxScroll + this.y0;
			if (q < this.y0) {
				q = this.y0;
			}

			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex(scrollbarStartX, this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX, this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX, this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex(scrollbarStartX, this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex(scrollbarStartX, q + p, 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX, q + p, 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX, q, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex(scrollbarStartX, q, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex(scrollbarStartX, q + p - 1, 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX - 1, q + p - 1, 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX - 1, q, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex(scrollbarStartX, q, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			tessellator.end();
		}
	}

	protected class DescriptionEntry extends AbstractSelectionList.Entry<DescriptionEntry> {
		private final DescriptionListWidget widget;
		protected FormattedCharSequence text;

		public DescriptionEntry(FormattedCharSequence text, DescriptionListWidget widget) {
			this.text = text;
			this.widget = widget;
		}

		@Override
		public void render(PoseStack matrices, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
			if (widget.y0 > y || widget.y1 - textRenderer.lineHeight < y) {
				return;
			}
			textRenderer.drawShadow(matrices, text, x, y, 0xAAAAAA);
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(FormattedCharSequence text, DescriptionListWidget widget) {
			super(text, widget);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				minecraft.setScreen(new WinScreen(false, Runnables.doNothing()));
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

	protected class LinkEntry extends DescriptionEntry {
		private final String link;

		public LinkEntry(FormattedCharSequence text, String link, DescriptionListWidget widget) {
			super(text, widget);
			this.link = link;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				minecraft.setScreen(new ConfirmLinkScreen((open) -> {
					if (open) {
						Util.getPlatform().openUri(link);
					}
					minecraft.setScreen(parent);
				}, link, false));
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

}
