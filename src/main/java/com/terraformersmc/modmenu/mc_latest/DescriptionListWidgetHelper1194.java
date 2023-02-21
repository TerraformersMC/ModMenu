package com.terraformersmc.modmenu.mc_latest;

import com.google.common.util.concurrent.Runnables;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.DescriptionListWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateAvailableBadge;
import com.terraformersmc.modmenu.util.compat.DescriptionListWidgetHelper;
import com.terraformersmc.modmenu.util.compat.MCCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Util;

import java.util.Collections;
import java.util.List;

public class DescriptionListWidgetHelper1194 extends DescriptionListWidgetHelper<DescriptionListWidgetHelper1194.DescriptionEntry> {
	@Override
	public DescriptionListWidget<DescriptionEntry> createDescriptionListWidget(MinecraftClient client, int width, int height, int top, int bottom, int entryHeight, ModsScreen parent) {
		return new DescriptionListWidget<>(client, width, height, top, bottom, entryHeight, parent);
	}

	@Override
	public CreditsScreen createCreditsScreen(boolean endCredits, ModsScreen parent) {
		return new CreditsScreen(endCredits, new LogoDrawer(false), Runnables.doNothing()) {
			@Override
			public void close() {
				client.setScreen(parent);
			}
		};
	}

	@Override
	public DescriptionEntry createDescriptionEntry(OrderedText text, DescriptionListWidget<DescriptionEntry> widget, ModsScreen parent) {
		return new DescriptionEntry(text, widget, parent);
	}

	@Override
	public DescriptionEntry createDescriptionEntry(OrderedText text, DescriptionListWidget<DescriptionEntry> widget, int indent, ModsScreen parent) {
		return new DescriptionEntry(text, widget, indent, parent);
	}

	@Override
	public DescriptionEntry createMojangCreditsEntry(OrderedText text, DescriptionListWidget<DescriptionEntry> widget, ModsScreen parent) {
		return new MojangCreditsEntry(text, widget, parent);
	}

	@Override
	public DescriptionEntry createLinkEntry(OrderedText text, String link, DescriptionListWidget<DescriptionEntry> widget, int indent, ModsScreen parent) {
		return new LinkEntry(text, link, widget, indent, parent);
	}

	@Override
	public DescriptionEntry createLinkEntry(OrderedText text, String link, DescriptionListWidget<DescriptionEntry> widget, ModsScreen parent) {
		return new LinkEntry(text, link, widget, parent);
	}

	protected static class DescriptionEntry extends ElementListWidget.Entry<DescriptionEntry> implements DescriptionListWidget.DescriptionListEntry<DescriptionEntry> {
		private final DescriptionListWidget<DescriptionEntry> widget;
		protected OrderedText text;
		protected int indent;
		public boolean updateTextEntry = false;
		protected final ModsScreen parent;

		public DescriptionEntry(OrderedText text, DescriptionListWidget<DescriptionEntry> widget, int indent, ModsScreen parent) {
			this.text = text;
			this.widget = widget;
			this.indent = indent;
			this.parent = parent;
		}

		public DescriptionEntry(OrderedText text, DescriptionListWidget<DescriptionEntry> widget, ModsScreen parent) {
			this(text, widget, 0, parent);
		}

		public DescriptionEntry setUpdateTextEntry() {
			this.updateTextEntry = true;
			return this;
		}

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
			if (widget.top > y || widget.bottom - MinecraftClient.getInstance().textRenderer.fontHeight < y) {
				return;
			}
			if (updateTextEntry) {
				UpdateAvailableBadge.renderBadge(matrices, x + indent, y);
				x += 11;
			}
			MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, text, x + indent, y, 0xAAAAAA);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}
	}

	protected static class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(OrderedText text, DescriptionListWidget<DescriptionEntry> widget, ModsScreen parent) {
			super(text, widget, parent);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				MinecraftClient.getInstance().setScreen(MCCompat.getInstance().getDescriptionListWidgetHelper().createCreditsScreen(false, parent));
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

	protected static class LinkEntry extends DescriptionEntry {
		private final String link;

		public LinkEntry(OrderedText text, String link, DescriptionListWidget<DescriptionEntry> widget, int indent, ModsScreen parent) {
			super(text, widget, indent, parent);
			this.link = link;
		}

		public LinkEntry(OrderedText text, String link, DescriptionListWidget<DescriptionEntry> widget, ModsScreen parent) {
			this(text, link, widget, 0, parent);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				MinecraftClient.getInstance().setScreen(new ConfirmLinkScreen((open) -> {
					if (open) {
						Util.getOperatingSystem().open(link);
					}
					MinecraftClient.getInstance().setScreen(parent);
				}, link, false));
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}
}
