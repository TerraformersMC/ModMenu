package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModMenuTexturedButtonWidget extends ButtonWidget {
	private final Identifier texture;
	private final int u;
	private final int v;
	private final int uWidth;
	private final int vHeight;

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, PressAction onPress) {
		this(x, y, width, height, u, v, texture, 256, 256, onPress);
	}

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, NarratorManager.EMPTY);
	}

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, Text message) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
	}

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, Text message, NarrationSupplier narationSupplier) {
		super(x, y, width, height, message, onPress, narationSupplier);
		this.uWidth = uWidth;
		this.vHeight = vHeight;
		this.u = u;
		this.v = v;
		this.texture = texture;
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		RenderSystem.setShaderColor(1, 1, 1, 1f);
		RenderSystem.setShaderTexture(0, this.texture);
		RenderSystem.disableDepthTest();
		int adjustedV = this.v;
		if (!active) {
			adjustedV += this.height * 2;
		} else if (this.isHovered()) {
			adjustedV += this.height;
		}

		drawTexture(matrices, this.getX(), this.getY(), this.u, adjustedV, this.width, this.height, this.uWidth, this.vHeight);
		RenderSystem.enableDepthTest();
	}
}
