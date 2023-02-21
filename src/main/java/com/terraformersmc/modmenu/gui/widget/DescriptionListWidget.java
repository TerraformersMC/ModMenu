package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.compat.DescriptionListWidgetHelper;
import com.terraformersmc.modmenu.util.compat.MCCompat;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DescriptionListWidget<T extends EntryListWidget.Entry<T> & DescriptionListWidget.DescriptionListEntry<T>> extends EntryListWidget<T> {

	private final ModsScreen parent;
	private final TextRenderer textRenderer;
	private ModListEntry lastSelected = null;
	private DescriptionListWidgetHelper<T> helper = (DescriptionListWidgetHelper<T>) MCCompat.getInstance().getDescriptionListWidgetHelper();

	public DescriptionListWidget(MinecraftClient client, int width, int height, int top, int bottom, int entryHeight, ModsScreen parent) {
		super(client, width, height, top, bottom, entryHeight);
		this.parent = parent;
		this.textRenderer = client.textRenderer;
	}

	@Override
	public T getSelectedOrNull() {
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
		builder.put(NarrationPart.TITLE, mod.getTranslatedName() + " " + mod.getPrefixedVersion());
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
				String description = mod.getTranslatedDescription();
				if (!description.isEmpty()) {
					for (OrderedText line : textRenderer.wrapLines(Text.literal(description.replaceAll("\n", "\n\n")), getRowWidth() - 5)) {
						children().add(helper.createDescriptionEntry(line, this, parent));
					}
				}

				if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue().contains(mod.getId())) {
					if (mod.getModrinthData() != null) {
						children().add(helper.createDescriptionEntry(OrderedText.EMPTY, this, parent));
						children().add(helper.createDescriptionEntry(Text.translatable("modmenu.hasUpdate").asOrderedText(), this, parent).setUpdateTextEntry());
						children().add(helper.createLinkEntry(
								Text.translatable("modmenu.updateText", mod.getModrinthData().versionNumber(), Text.translatable("modmenu.modrinth"))
										.formatted(Formatting.BLUE)
										.formatted(Formatting.UNDERLINE)
										.asOrderedText(), "https://modrinth.com/project/%s/version/%s".formatted(mod.getModrinthData().projectId(), mod.getModrinthData().versionId()), this, 8, parent));
					}
					if (mod.getChildHasUpdate()) {
						children().add(helper.createDescriptionEntry(OrderedText.EMPTY, this, parent));
						children().add(helper.createDescriptionEntry(Text.translatable("modmenu.childHasUpdate").asOrderedText(), this, parent).setUpdateTextEntry());
					}
				}

				Map<String, String> links = mod.getLinks();
				String sourceLink = mod.getSource();
				if ((!links.isEmpty() || sourceLink != null) && !ModMenuConfig.HIDE_MOD_LINKS.getValue()) {
					children().add(helper.createDescriptionEntry(OrderedText.EMPTY, this, parent));
					children().add(helper.createDescriptionEntry(Text.translatable("modmenu.links").asOrderedText(), this, parent));

					if (sourceLink != null) {
						children().add(helper.createLinkEntry(Text.translatable("modmenu.source").formatted(Formatting.BLUE).formatted(Formatting.UNDERLINE).asOrderedText(), sourceLink, this, 8, parent));
					}

					links.forEach((key, value) -> {
						children().add(helper.createLinkEntry(Text.translatable(key).formatted(Formatting.BLUE).formatted(Formatting.UNDERLINE).asOrderedText(), value, this, 8, parent));
					});
				}

				Set<String> licenses = mod.getLicense();
				if (!ModMenuConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty()) {
					children().add(helper.createDescriptionEntry(OrderedText.EMPTY, this, parent));
					children().add(helper.createDescriptionEntry(Text.translatable("modmenu.license").asOrderedText(), this, parent));

					for (String license : licenses) {
						children().add(helper.createDescriptionEntry(Text.literal(license).asOrderedText(), this, 8, parent));
					}
				}

				if (!ModMenuConfig.HIDE_MOD_CREDITS.getValue()) {
					if ("minecraft".equals(mod.getId())) {
						children().add(helper.createDescriptionEntry(OrderedText.EMPTY, this, parent));
						children().add(helper.createMojangCreditsEntry(Text.translatable("modmenu.viewCredits").formatted(Formatting.BLUE).formatted(Formatting.UNDERLINE).asOrderedText(), this, parent));
					} else if ("java".equals(mod.getId())) {
						children().add(helper.createDescriptionEntry(OrderedText.EMPTY, this, parent));
					} else {
						List<String> credits = mod.getCredits();
						if (!credits.isEmpty()) {
							children().add(helper.createDescriptionEntry(OrderedText.EMPTY, this, parent));
							children().add(helper.createDescriptionEntry(Text.translatable("modmenu.credits").asOrderedText(), this, parent));
							for (String credit : credits) {
								int indent = 8;
								for (OrderedText line : textRenderer.wrapLines(Text.literal(credit), getRowWidth() - 5 - 16)) {
									children().add(helper.createDescriptionEntry(line, this, indent, parent));
									indent = 16;
								}
							}
						}
					}
				}
			}
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		{
			RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
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
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);

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

		this.renderList(matrices, mouseX, mouseY, delta);
		this.renderScrollBar(bufferBuilder, tessellator);

		MCCompat.getInstance().getBlaze3DHelper().enableTexture();
		RenderSystem.disableBlend();
	}

	public void renderScrollBar(BufferBuilder bufferBuilder, Tessellator tessellator) {
		int scrollbarStartX = this.getScrollbarPositionX();
		int scrollbarEndX = scrollbarStartX + 6;
		int maxScroll = this.getMaxScroll();
		if (maxScroll > 0) {
			MCCompat.getInstance().getBlaze3DHelper().disableTexture();
			int p = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this.getMaxPosition());
			p = MathHelper.clamp(p, 32, this.bottom - this.top - 8);
			int q = (int) this.getScrollAmount() * (this.bottom - this.top - p) / maxScroll + this.top;
			if (q < this.top) {
				q = this.top;
			}

			RenderSystem.setShader(GameRenderer::getPositionColorProgram);
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

	public static interface DescriptionListEntry<T> {
		T setUpdateTextEntry();
	}
}
