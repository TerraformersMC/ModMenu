package com.terraformersmc.modmenu.gui.widget;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DescriptionListWidget extends EntryListWidget<DescriptionListWidget.DescriptionEntry> {

	private final ModsScreen parent;
	private final TextRenderer textRenderer;
	private ModListEntry lastSelected = null;

	public DescriptionListWidget(MinecraftClient client, int width, int height, int top, int bottom, int entryHeight, ModsScreen parent) {
		super(client, width, height, top, bottom, entryHeight);
		this.parent = parent;
		this.textRenderer = client.textRenderer;
	}

	@Override
	public DescriptionEntry getSelectedOrNull() {
		return null;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarPositionX() {
		return this.width - 6 + left;
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		Mod mod = parent.getSelectedEntry().getMod();
		builder.put(NarrationPart.TITLE, mod.getName() + " " + mod.getPrefixedVersion());
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clearEntries();
			setScrollAmount(-Double.MAX_VALUE);
			if (lastSelected != null) {
				Mod mod = lastSelected.getMod();
				String description = mod.getDescription();
				String translatableDescriptionKey = "modmenu.descriptionTranslation." + mod.getId();
				if (I18n.hasTranslation(translatableDescriptionKey)) {
					description = I18n.translate(translatableDescriptionKey);
				}
				if (!description.isEmpty()) {
					for (OrderedText line : textRenderer.wrapLines(Text.literal(description.replaceAll("\n", "\n\n")), getRowWidth() - 5)) {
						children().add(new DescriptionEntry(line, this));
					}
				}

				Map<String, String> links = mod.getLinks();
				String sourceLink = mod.getSource();
				if ((!links.isEmpty() || sourceLink != null) && !ModMenuConfig.HIDE_MOD_LINKS.getValue()) {
					children().add(new DescriptionEntry(OrderedText.EMPTY, this));
					children().add(new DescriptionEntry(Text.translatable("modmenu.links").asOrderedText(), this));

					if (sourceLink != null) {
						children().add(new LinkEntry(Text.literal("  ").append(Text.translatable("modmenu.source").formatted(Formatting.BLUE).formatted(Formatting.UNDERLINE)).asOrderedText(), sourceLink, this));
					}

					links.forEach((key, value) -> {
						children().add(new LinkEntry(Text.literal("  ").append(Text.translatable(key).formatted(Formatting.BLUE).formatted(Formatting.UNDERLINE)).asOrderedText(), value, this));
					});
				}

				Set<String> licenses = mod.getLicense();
				if (!ModMenuConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty()) {
					children().add(new DescriptionEntry(OrderedText.EMPTY, this));
					children().add(new DescriptionEntry(Text.translatable("modmenu.license").asOrderedText(), this));

					for (String license : licenses) {
						children().add(new DescriptionEntry(Text.literal("  " + license).asOrderedText(), this));
					}
				}

				if (!ModMenuConfig.HIDE_MOD_CREDITS.getValue()) {
					if ("minecraft".equals(mod.getId())) {
						children().add(new DescriptionEntry(OrderedText.EMPTY, this));
						children().add(new MojangCreditsEntry(Text.translatable("modmenu.viewCredits").formatted(Formatting.BLUE).formatted(Formatting.UNDERLINE).asOrderedText(), this));
					} else if ("java".equals(mod.getId())) {
						children().add(new DescriptionEntry(OrderedText.EMPTY, this));
					} else {
						List<String> credits = mod.getCredits();
						if (!credits.isEmpty()) {
							children().add(new DescriptionEntry(OrderedText.EMPTY, this));
							children().add(new DescriptionEntry(Text.translatable("modmenu.credits").asOrderedText(), this));
							for (String credit : credits) {
								children().add(new DescriptionEntry(Text.literal("  " + credit).asOrderedText(), this));
							}
						}
					}
				}
			}
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		{
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.vertex(this.left, this.bottom, 0.0D).texture(this.left / 32.0F, (this.bottom + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
			bufferBuilder.vertex(this.right, this.bottom, 0.0D).texture(this.right / 32.0F, (this.bottom + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
			bufferBuilder.vertex(this.right, this.top, 0.0D).texture(this.right / 32.0F, (this.top + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
			bufferBuilder.vertex(this.left, this.top, 0.0D).texture(this.left / 32.0F, (this.top + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
			tessellator.draw();
		}

		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(this.left, (this.top + 4), 0.0D).

				color(0, 0, 0, 0).

				next();
		bufferBuilder.vertex(this.right, (this.top + 4), 0.0D).

				color(0, 0, 0, 0).

				next();
		bufferBuilder.vertex(this.right, this.top, 0.0D).

				color(0, 0, 0, 255).

				next();
		bufferBuilder.vertex(this.left, this.top, 0.0D).

				color(0, 0, 0, 255).

				next();
		bufferBuilder.vertex(this.left, this.bottom, 0.0D).

				color(0, 0, 0, 255).

				next();
		bufferBuilder.vertex(this.right, this.bottom, 0.0D).

				color(0, 0, 0, 255).

				next();
		bufferBuilder.vertex(this.right, (this.bottom - 4), 0.0D).

				color(0, 0, 0, 0).

				next();
		bufferBuilder.vertex(this.left, (this.bottom - 4), 0.0D).

				color(0, 0, 0, 0).

				next();
		tessellator.draw();

		int k = this.getRowLeft();
		int l = this.top + 4 - (int) this.getScrollAmount();
		this.renderList(matrices, k, l, mouseX, mouseY, delta);
		this.renderScrollBar(bufferBuilder, tessellator);

		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public void renderScrollBar(BufferBuilder bufferBuilder, Tessellator tessellator) {
		int scrollbarStartX = this.getScrollbarPositionX();
		int scrollbarEndX = scrollbarStartX + 6;
		int maxScroll = this.getMaxScroll();
		if (maxScroll > 0) {
			RenderSystem.disableTexture();
			int p = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this.getMaxPosition());
			p = MathHelper.clamp(p, 32, this.bottom - this.top - 8);
			int q = (int) this.getScrollAmount() * (this.bottom - this.top - p) / maxScroll + this.top;
			if (q < this.top) {
				q = this.top;
			}

			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
			bufferBuilder.vertex(scrollbarStartX, this.bottom, 0.0D).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(scrollbarEndX, this.bottom, 0.0D).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(scrollbarEndX, this.top, 0.0D).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(scrollbarStartX, this.top, 0.0D).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(scrollbarStartX, q + p, 0.0D).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(scrollbarEndX, q + p, 0.0D).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(scrollbarEndX, q, 0.0D).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(scrollbarStartX, q, 0.0D).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(scrollbarStartX, q + p - 1, 0.0D).color(192, 192, 192, 255).next();
			bufferBuilder.vertex(scrollbarEndX - 1, q + p - 1, 0.0D).color(192, 192, 192, 255).next();
			bufferBuilder.vertex(scrollbarEndX - 1, q, 0.0D).color(192, 192, 192, 255).next();
			bufferBuilder.vertex(scrollbarStartX, q, 0.0D).color(192, 192, 192, 255).next();
			tessellator.draw();
		}
	}

	protected class DescriptionEntry extends EntryListWidget.Entry<DescriptionEntry> {
		private final DescriptionListWidget widget;
		protected OrderedText text;

		public DescriptionEntry(OrderedText text, DescriptionListWidget widget) {
			this.text = text;
			this.widget = widget;
		}

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
			if (widget.top > y || widget.bottom - textRenderer.fontHeight < y) {
				return;
			}
			textRenderer.drawWithShadow(matrices, text, x, y, 0xAAAAAA);
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(OrderedText text, DescriptionListWidget widget) {
			super(text, widget);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				client.setScreen(new MinecraftCredits(false));
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		class MinecraftCredits extends CreditsScreen {
			public MinecraftCredits(boolean endCredits) {
				super(endCredits, Runnables.doNothing());
			}

			@Override
			public void close() {
				client.setScreen(parent);
			}
		}
	}

	protected class LinkEntry extends DescriptionEntry {
		private final String link;

		public LinkEntry(OrderedText text, String link, DescriptionListWidget widget) {
			super(text, widget);
			this.link = link;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				client.setScreen(new ConfirmChatLinkScreen((open) -> {
					if (open) {
						Util.getOperatingSystem().open(link);
					}
					client.setScreen(parent);
				}, link, false));
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

}
