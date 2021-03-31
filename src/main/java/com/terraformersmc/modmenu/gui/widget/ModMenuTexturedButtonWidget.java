package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ModMenuTexturedButtonWidget extends Button {
	private final ResourceLocation texture;
	private final int u;
	private final int v;
	private final int uWidth;
	private final int vHeight;

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, ResourceLocation texture, OnPress onPress) {
		this(x, y, width, height, u, v, texture, 256, 256, onPress);
	}

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, ResourceLocation texture, int uWidth, int vHeight, OnPress onPress) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, NarratorChatListener.NO_TITLE);
	}

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, ResourceLocation texture, int uWidth, int vHeight, OnPress onPress, Component message) {
		this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, NO_TOOLTIP);
	}

	public ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, ResourceLocation texture, int uWidth, int vHeight, OnPress onPress, Component message, OnTooltip tooltipSupplier) {
		super(x, y, width, height, message, onPress, tooltipSupplier);
		this.uWidth = uWidth;
		this.vHeight = vHeight;
		this.u = u;
		this.v = v;
		this.texture = texture;
	}

	protected void setPos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
		Minecraft client = Minecraft.getInstance();
		client.getTextureManager().bind(this.texture);
		RenderSystem.color4f(1, 1, 1, 1f);
		RenderSystem.disableDepthTest();
		int adjustedV = this.v;
		if (!active) {
			adjustedV += this.height * 2;
		} else if (this.isHovered()) {
			adjustedV += this.height;
		}

		blit(matrices, this.x, this.y, this.u, adjustedV, this.width, this.height, this.uWidth, this.vHeight);
		RenderSystem.enableDepthTest();

		if (this.isHovered()) {
			this.renderToolTip(matrices, mouseX, mouseY);
		}
	}

	public boolean isJustHovered() {
		return isHovered;
	}

	public boolean isFocusedButNotHovered() {
		return !isHovered && isFocused();
	}
}
