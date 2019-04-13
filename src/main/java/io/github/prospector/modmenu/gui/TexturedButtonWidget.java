package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;

public class TexturedButtonWidget extends ButtonWidget {
	private final Identifier texture;
	private final int u;
	private final int v;
	private final int textureWidth;
	private final int textureHeight;

	public TexturedButtonWidget(int x, int y, int width, int height, int int_6, int int_7, Identifier texture, PressAction onPress) {
		this(x, y, width, height, int_6, int_7, texture, 256, 256, onPress);
	}

	public TexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier identifier_1, int textureWidth, int tedxtureHeight, PressAction onPress) {
		this(x, y, width, height, u, v, identifier_1, textureWidth, tedxtureHeight, onPress, "");
	}

	public TexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int textureWidth, int textureHeight, PressAction onPress, String string_1) {
		super(x, y, width, height, string_1, onPress);
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.u = u;
		this.v = v;
		this.texture = texture;
	}

	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void renderButton(int mouseX, int mouseY, float delta) {
		MinecraftClient client = MinecraftClient.getInstance();
		client.getTextureManager().bindTexture(this.texture);
		GlStateManager.disableDepthTest();
		int adjustedV = this.v;
		if (!active) {
			adjustedV += this.height * 2;
		} else if (this.isHovered()) {
			adjustedV += this.height;
		}

		blit(this.x, this.y, this.u, adjustedV, this.width, this.height, this.textureWidth, this.textureHeight);
		GlStateManager.enableDepthTest();
	}
}