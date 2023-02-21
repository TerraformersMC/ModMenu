package com.terraformersmc.modmenu.mc_latest;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import com.terraformersmc.modmenu.mixin.mc1193plus.IGridWidgetAccessor;
import com.terraformersmc.modmenu.util.compat.ButtonHelper;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Supplier;

public class ButtonHelper1193 extends ButtonHelper {
	@Override
	public ButtonWidget createConfigureButton(
			ModsScreen screen,
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip,
			RenderOverride renderOverride
	) {
		return new ModMenuTexturedButtonWidget(x, y, width, height, u, v, texture, uWidth, vHeight, onPress) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				renderOverride.render(this, matrices, mouseX, mouseY, delta);
				super.render(matrices, mouseX, mouseY, delta);
			}

			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
				RenderSystem.setShaderColor(1, 1, 1, 1f);
				super.renderButton(matrices, mouseX, mouseY, delta);
			}
		};
	}

	@Override
	public ButtonWidget createFiltersButton(
			ModsScreen screen,
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip
	) {
		ButtonWidget widget = new ModMenuTexturedButtonWidget(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, tooltip);
		widget.setTooltip(Tooltip.of(tooltip));
		return widget;
	}

	@Override
	public List<ClickableWidget> getButtons(Screen screen) {
		ClickableWidget widget = Screens.getButtons(screen).get(0);
		if (widget instanceof GridWidget) return ((IGridWidgetAccessor) widget).getChildren();
		return null;
	}

	@Override
	public ButtonWidget createModMenuButtonWidget(int x, int y, int width, int height, Text text, Screen screen) {
		return new ModMenuButtonWidget(x, y, width, height, text, screen);
	}

	@Override
	public ButtonWidget createModMenuTexturedButtonWidget(
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text message, boolean allowUpdateBadge
	) {
		return new ModMenuTexturedButtonWidget(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, allowUpdateBadge);
	}

	@Override
	public ButtonWidget createButtonWidget(
			int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress, RenderOverride renderOverride
	) {
		return new ButtonWidget(x, y, width, height, message, onPress, Supplier::get) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				renderOverride.render(this, matrices, mouseX, mouseY, delta);
				super.render(matrices, mouseX, mouseY, delta);
			}
		};
	}

	@Override
	public ButtonWidget createButtonWidget(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
		return ButtonWidget.builder(message, onPress)
				.position(x, y)
				.size(width, height)
				.narrationSupplier(Supplier::get)
				.build();
	}
}
