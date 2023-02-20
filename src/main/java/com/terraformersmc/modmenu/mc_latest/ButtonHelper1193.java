package com.terraformersmc.modmenu.mc_latest;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
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
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class ButtonHelper1193 extends ButtonHelper {
	@Override
	public ButtonWidget createConfigureButton(
			ModsScreen screen,
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip,
			ModListEntry selected, Map<String, Boolean> modHasConfigScreen, Map<String, Throwable> modScreenErrors
	) {
		return new ModMenuTexturedButtonWidget(x, y, width, height, u, v, texture, uWidth, vHeight, onPress) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				String modId = selected.getMod().getId();
				if (selected != null) {
					active = modHasConfigScreen.get(modId);
				} else {
					active = false;
					visible = false;
				}
				visible = selected != null && modHasConfigScreen.get(modId) || modScreenErrors.containsKey(modId);
				if (modScreenErrors.containsKey(modId)) {
					Throwable e = modScreenErrors.get(modId);
					this.setTooltip(Tooltip.of(Text.translatable("modmenu.configure.error", modId, modId).copy().append("\n\n").append(e.toString()).formatted(Formatting.RED)));
				} else {
					this.setTooltip(Tooltip.of(tooltip));
				}
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
}
