package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;

public class TexturedButtonWidget extends ButtonWidget {
	private final Identifier texture;
	private final int u;
	private final int v;
	private final int uWidth;
	private final int vHeight;

	public TexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, PressAction onPress) {
		this(x, y, width, height, u, v, texture, 256, 256, onPress);
	}

	public TexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, "");
	}

	public TexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, String message) {
		super(x, y, width, height, message, onPress);
		this.uWidth = uWidth;
		this.vHeight = vHeight;
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

		blit(this.x, this.y, this.u, adjustedV, this.width, this.height, this.uWidth, this.vHeight);
		GlStateManager.enableDepthTest();
	}
}