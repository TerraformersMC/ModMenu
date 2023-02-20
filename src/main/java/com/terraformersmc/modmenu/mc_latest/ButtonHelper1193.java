package com.terraformersmc.modmenu.mc_latest;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.compat.ButtonHelper;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ButtonHelper1193 extends ButtonHelper {
	@Override
	public ButtonWidget createConfigureButton(
			ModsScreen screen,
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip,
			ModListEntry selected, Map<String, Boolean> modHasConfigScreen
	) {
		ButtonWidget widget = new ModMenuTexturedButtonWidget(x, y, width, height, u, v, texture, uWidth, vHeight, onPress) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				if (selected != null) {
					String modid = selected.getMod().getId();
					active = modHasConfigScreen.get(modid);
				} else {
					active = false;
				}
				visible = active;
				super.render(matrices, mouseX, mouseY, delta);
			}

			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
				RenderSystem.setShaderColor(1, 1, 1, 1f);
				super.renderButton(matrices, mouseX, mouseY, delta);
			}
		};
		widget.setTooltip(Tooltip.of(tooltip));
		return widget;
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
}
