package io.github.prospector.modmenu.gui;

import io.github.prospector.modmenu.util.HardcodedUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.EntryListWidget;

public class DescriptionListWidget extends EntryListWidget<DescriptionListWidget.DescriptionEntry> {

	private final ModListScreen parent;
	private final TextRenderer textRenderer;
	private ModListEntry lastSelected = null;

	public DescriptionListWidget(MinecraftClient client, int width, int height, int top, int bottom, int entryHeight, ModListScreen parent) {
		super(client, width, height, top, bottom, entryHeight);
		this.parent = parent;
		this.textRenderer = client.textRenderer;
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
		return this.width - 6 + left;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clearEntries();
			setScrollAmount(-Double.MAX_VALUE);
			String description = lastSelected.getMetadata().getDescription();
			String id = lastSelected.getMetadata().getId();
			if (description.isEmpty() && HardcodedUtil.getHardcodedDescriptions().containsKey(id)) {
				description = HardcodedUtil.getHardcodedDescription(id);
			}
			if (lastSelected != null && description != null && !description.isEmpty()) {
				for (String line : textRenderer.wrapStringToWidthAsList(description.replaceAll("\n", "\n\n"), getRowWidth())) {
					children().add(new DescriptionEntry(line));
				}
			}
		}
		super.render(mouseX, mouseY, delta);
	}

	@Override
	protected void renderHoleBackground(int y1, int y2, int startAlpha, int endAlpha) {
		// Awful hack but it makes the background "seamless"
		ModListScreen.overlayBackground(left, y1, right, y2, 64, 64, 64, startAlpha, endAlpha);
	}

	protected class DescriptionEntry extends EntryListWidget.Entry<DescriptionEntry> {
		protected String text;

		public DescriptionEntry(String text) {
			this.text = text;
		}

		@Override
		public void render(int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
			MinecraftClient.getInstance().textRenderer.drawWithShadow(text, x, y, 0xAAAAAA);
		}
	}

}
