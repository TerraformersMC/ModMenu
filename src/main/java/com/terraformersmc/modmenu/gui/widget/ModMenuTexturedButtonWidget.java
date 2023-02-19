package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
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
	private final boolean allowUpdateBadge;

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, PressAction onPress) {
		this(x, y, width, height, u, v, texture, 256, 256, onPress);
	}

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, NarratorManager.EMPTY);
	}

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, Text message) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, ButtonWidget.EMPTY, false);
	}

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, Text message, boolean allowUpdateBadge) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, ButtonWidget.EMPTY, allowUpdateBadge);
	}

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, Text message, ButtonWidget.TooltipSupplier tooltipSupplier) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, tooltipSupplier, false);
	}

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, Text message, ButtonWidget.TooltipSupplier tooltipSupplier, boolean allowUpdateBadge) {
		super(x, y, width, height, message, onPress, tooltipSupplier);
		this.uWidth = uWidth;
		this.vHeight = vHeight;
		this.u = u;
		this.v = v;
		this.texture = texture;
		this.allowUpdateBadge = allowUpdateBadge;
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

		drawTexture(matrices, this.x, this.y, this.u, adjustedV, this.width, this.height, this.uWidth, this.vHeight);
		RenderSystem.enableDepthTest();

		if (this.isHovered()) {
			this.renderTooltip(matrices, mouseX, mouseY);
		}

		if (this.allowUpdateBadge && ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.modUpdateAvailable) {
			UpdateAvailableBadge.renderBadge(matrices, this.x + this.width - 5, this.y - 3);
		}
	}

	public boolean isJustHovered() {
		return hovered;
	}

	public boolean isFocusedButNotHovered() {
		return !hovered && isFocused();
	}
}
