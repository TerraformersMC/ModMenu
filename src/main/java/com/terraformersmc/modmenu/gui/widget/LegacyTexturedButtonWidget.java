package com.terraformersmc.modmenu.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LegacyTexturedButtonWidget extends TexturedButtonWidget {
	private final int u;
	private final int v;
	private final int hoveredVOffset;

	private final Identifier texture;

	private final int textureWidth;
	private final int textureHeight;

	public LegacyTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, ButtonWidget.PressAction pressAction, Text message) {
		super(x, y, width, height, null, pressAction, message);

		this.u = u;
		this.v = v;
		this.hoveredVOffset = hoveredVOffset;

		this.texture = texture;

		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		int v = this.v;

		if (!this.isNarratable()) {
			v += this.hoveredVOffset * 2;
		} else if (this.isSelected()) {
			v += this.hoveredVOffset;
		}

		context.drawTexture(this.texture, this.getX(), this.getY(), this.u, v, this.width, this.height, this.textureWidth, this.textureHeight);
	}

	public static Builder legacyTexturedBuilder(Text message, ButtonWidget.PressAction onPress) {
		return new Builder(message, onPress);
	}

	public static class Builder {
		private final Text message;
		private final ButtonWidget.PressAction onPress;

		private int x;
		private int y;

		private int width;
		private int height;

		private int u;
		private int v;
		private int hoveredVOffset;

		private Identifier texture;

		private int textureWidth;
		private int textureHeight;

		public Builder(Text message, PressAction onPress) {
			this.message = message;
			this.onPress = onPress;
		}

		public Builder position(int x, int y) {
			this.x = x;
			this.y = y;

			return this;
		}

		public Builder size(int width, int height) {
			this.width = width;
			this.height = height;

			return this;
		}

		public Builder uv(int u, int v, int hoveredVOffset) {
			this.u = u;
			this.v = v;

			this.hoveredVOffset = hoveredVOffset;

			return this;
		}

		public Builder texture(Identifier texture, int textureWidth, int textureHeight) {
			this.texture = texture;

			this.textureWidth = textureWidth;
			this.textureHeight = textureHeight;

			return this;
		}

		public LegacyTexturedButtonWidget build() {
			return new LegacyTexturedButtonWidget(this.x, this.y, this.width, this.height, this.u, this.v, this.hoveredVOffset, this.texture, this.textureWidth, this.textureHeight, this.onPress, this.message);
		}
	}
}
