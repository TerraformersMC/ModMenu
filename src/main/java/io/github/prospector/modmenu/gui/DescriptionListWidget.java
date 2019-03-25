package io.github.prospector.modmenu.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.EntryListWidget;

public class DescriptionListWidget extends EntryListWidget<DescriptionListWidget.DescriptionEntry> {

	private ModEntryWidget lastSelected = null;
	private ModListScreen parent;
	private TextRenderer textRenderer;

	public DescriptionListWidget(MinecraftClient client, int width, int height, int top, int bottom, int entryHeight, ModListScreen parent) {
		super(client, width, height, top, bottom, entryHeight);
		this.parent = parent;
		this.textRenderer = client.textRenderer;
	}

	@Override
	public int getEntryWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6 + x;
	}

	@Override
	public void render(int int_1, int int_2, float float_1) {
		if (parent.getModList().selected != lastSelected) {
			lastSelected = parent.getModList().selected;
			clearEntries();
			scrollY = 0d;
			if (lastSelected != null && lastSelected.info.getDescription() != null && !lastSelected.info.getDescription().isEmpty())
				for (String line : textRenderer.wrapStringToWidthAsList(lastSelected.info.getDescription().replaceAll("\n", "\n\n"), getEntryWidth()))
					getInputListeners().add(new DescriptionEntry(line));
		}
		super.render(int_1, int_2, float_1);
	}

	protected class DescriptionEntry extends EntryListWidget.Entry<DescriptionEntry> {
		protected String text;

		public DescriptionEntry(String text) {
			this.text = text;
		}

		@Override
		public void draw(int i, int i1, int i2, int i3, boolean b, float v) {
			MinecraftClient.getInstance().textRenderer.drawWithShadow(text, getX(), getY(), 0xAAAAAA);
		}
	}

}
