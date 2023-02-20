package com.terraformersmc.modmenu.mc_1_19_2;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.compat.ButtonHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ButtonHelper1192 extends ButtonHelper {
	@Override
	public ButtonWidget createConfigureButton(
			ModsScreen screen,
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip,
			ModListEntry selected, Map<String, Boolean> modHasConfigScreen
	) {
		return new ModMenuTexturedButtonWidget1192(x, y, width, height, u, v, texture, uWidth, vHeight, onPress,
				tooltip, (button, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget1192 widget1192 = (ModMenuTexturedButtonWidget1192) button;
			if (widget1192.isJustHovered()) {
				screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
			} else if (widget1192.isFocusedButNotHovered()) {
				screen.renderTooltip(matrices, tooltip, button.x, button.y);
			}
		}) {
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
				RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
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
		return new ModMenuTexturedButtonWidget1192(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, tooltip,
				(button, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget1192 widget1192 = (ModMenuTexturedButtonWidget1192) button;
			if (widget1192.isJustHovered()) {
				screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
			} else if (widget1192.isFocusedButNotHovered()) {
				screen.renderTooltip(matrices, tooltip, button.x, button.y);
			}
		});
	}
}
