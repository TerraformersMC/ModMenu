package com.terraformersmc.modmenu.mc_1_19_2;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.UpdateAvailableBadge;
import com.terraformersmc.modmenu.util.compat.ButtonCompat;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModMenuTexturedButtonWidget1192 extends ButtonWidget {
	private final Identifier texture;
	private final int u;
	private final int v;
	private final int uWidth;
	private final int vHeight;
	private final boolean allowUpdateBadge;
	private Text dynamicTooltip;

	public ModMenuTexturedButtonWidget1192(int x, int y, int width, int height, int u, int v, Identifier texture, PressAction onPress) {
		this(x, y, width, height, u, v, texture, 256, 256, onPress);
	}

	public ModMenuTexturedButtonWidget1192(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, NarratorManager.EMPTY);
	}

	public ModMenuTexturedButtonWidget1192(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, Text message) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, ButtonWidget.EMPTY, false);
	}

	public ModMenuTexturedButtonWidget1192(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, Text message, TooltipSupplier tooltipSupplier) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, tooltipSupplier, false);
	}

	public ModMenuTexturedButtonWidget1192(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, Text message, boolean allowUpdateBadge) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, ButtonWidget.EMPTY, allowUpdateBadge);
	}

	public ModMenuTexturedButtonWidget1192(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, Text message, TooltipSupplier tooltipSupplier, boolean allowUpdateBadge) {
		super(x, y, width, height, message, onPress, tooltipSupplier);
		this.uWidth = uWidth;
		this.vHeight = vHeight;
		this.u = u;
		this.v = v;
		this.texture = texture;
		this.allowUpdateBadge = allowUpdateBadge;
		this.dynamicTooltip = message;
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

		drawTexture(matrices, ((ButtonCompat)this).getButtonX(), ((ButtonCompat)this).getButtonY(), this.u, adjustedV, this.width, this.height, this.uWidth, this.vHeight);
		RenderSystem.enableDepthTest();

		if (this.allowUpdateBadge && ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.modUpdateAvailable) {
			UpdateAvailableBadge.renderBadge(matrices, ((ButtonCompat)this).getButtonX() + this.width - 5, ((ButtonCompat)this).getButtonY() - 3);
		}

		if (this.isHovered()) {
			this.renderTooltip(matrices, mouseX, mouseY);
		}
	}

	public boolean isJustHovered() {
		return hovered;
	}

	public boolean isFocusedButNotHovered() {
		return !hovered && isFocused();
	}

	@Override
	public void setDynamicTooltip(Text dynamicTooltip) {
		this.dynamicTooltip = dynamicTooltip;
	}

	public Text getDynamicTooltip() {
		return dynamicTooltip;
	}
}
