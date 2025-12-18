package com.terraformersmc.modmenu.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class LegacyTexturedButtonWidget extends ImageButton {
	private final int u;
	private final int v;
	private final int hoveredVOffset;

	private final Identifier texture;

	private final int textureWidth;
	private final int textureHeight;

	public LegacyTexturedButtonWidget(
		int x,
		int y,
		int width,
		int height,
		int u,
		int v,
		int hoveredVOffset,
		Identifier texture,
		int textureWidth,
		int textureHeight,
		Button.OnPress pressAction,
		net.minecraft.network.chat.Component message
	) {
		super(x, y, width, height, null, pressAction, message);

		this.u = u;
		this.v = v;
		this.hoveredVOffset = hoveredVOffset;

		this.texture = texture;

		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	@Override
    public void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		int v = this.v;
		if (!this.isActive()) {
			v += this.hoveredVOffset * 2;
		} else if (this.isHoveredOrFocused()) {
			v += this.hoveredVOffset;
		}

		context.blit(
			RenderPipelines.GUI_TEXTURED,
			this.texture,
			this.getX(),
			this.getY(),
			this.u,
			v,
			this.width,
			this.height,
			this.textureWidth,
			this.textureHeight
		);
	}

    public static com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget.Builder legacyTexturedBuilder(net.minecraft.network.chat.Component message, Button.OnPress onPress) {
		return new com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget.Builder(message, onPress);
	}

	public static class Builder {
		private final net.minecraft.network.chat.Component message;
		private final Button.OnPress onPress;

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

		public Builder(net.minecraft.network.chat.Component message, OnPress onPress) {
			this.message = message;
			this.onPress = onPress;
		}

		public com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget.Builder position(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget.Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget.Builder uv(int u, int v, int hoveredVOffset) {
			this.u = u;
			this.v = v;
			this.hoveredVOffset = hoveredVOffset;
			return this;
		}

		public com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget.Builder texture(Identifier texture, int textureWidth, int textureHeight) {
			this.texture = texture;
			this.textureWidth = textureWidth;
			this.textureHeight = textureHeight;
			return this;
		}

		public LegacyTexturedButtonWidget build() {
			return new LegacyTexturedButtonWidget(
				this.x,
				this.y,
				this.width,
				this.height,
				this.u,
				this.v,
				this.hoveredVOffset,
				this.texture,
				this.textureWidth,
				this.textureHeight,
				this.onPress,
				this.message
			);
		}
	}
}
