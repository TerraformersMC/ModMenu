package com.terraformersmc.modmenu.mc_1_19_2;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.compat.WidgetHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class WidgetHelper1192 extends WidgetHelper {
	@Override
	public ButtonWidget createConfigureButton(
			ModsScreen screen,
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip,
			RenderOverride renderOverride
	) {
		return new ModMenuTexturedButtonWidget1192(x, y, width, height, u, v, texture, uWidth, vHeight, onPress,
				tooltip, (button, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget1192 widget1192 = (ModMenuTexturedButtonWidget1192) button;
			if (widget1192.isJustHovered()) {
				screen.renderTooltip(matrices, widget1192.getDynamicTooltip(), mouseX, mouseY);
			} else if (widget1192.isFocusedButNotHovered()) {
				screen.renderTooltip(matrices, widget1192.getDynamicTooltip(), button.x, button.y);
			}
		}) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				renderOverride.render(this, matrices, mouseX, mouseY, delta);
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

	@Override
	public ButtonWidget createModMenuButtonWidget(int x, int y, int width, int height, Text text, Screen screen) {
		return new ModMenuButtonWidget1192(x, y, width, height, text, screen);
	}

	@Override
	public ButtonWidget createModMenuTexturedButtonWidget(
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text message, boolean allowUpdateBadge
	) {
		return new ModMenuTexturedButtonWidget1192(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, allowUpdateBadge);
	}

	@Override
	public ButtonWidget createButtonWidget(
			int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress, RenderOverride renderOverride
	) {
		return new ButtonWidget(x, y, width, height, message, onPress) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				renderOverride.render(this, matrices, mouseX, mouseY, delta);
				super.render(matrices, mouseX, mouseY, delta);
			}
		};
	}

	@Override
	public ButtonWidget createButtonWidget(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
		return new ButtonWidget(x, y, width, height, message, onPress);
	}

	@Override
	public void renderModListWidget(
			MatrixStack matrices, Tessellator tessellator, BufferBuilder buffer,
			int entryTop, int entryHeight, int entryLeft, int selectionRight
	) {
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
		buffer.vertex(matrix, entryLeft, entryTop + entryHeight + 2, 0.0F).next();
		buffer.vertex(matrix, selectionRight, entryTop + entryHeight + 2, 0.0F).next();
		buffer.vertex(matrix, selectionRight, entryTop - 2, 0.0F).next();
		buffer.vertex(matrix, entryLeft, entryTop - 2, 0.0F).next();
		tessellator.draw();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
		buffer.vertex(matrix, entryLeft + 1, entryTop + entryHeight + 1, 0.0F).next();
		buffer.vertex(matrix, selectionRight - 1, entryTop + entryHeight + 1, 0.0F).next();
		buffer.vertex(matrix, selectionRight - 1, entryTop - 1, 0.0F).next();
		buffer.vertex(matrix, entryLeft + 1, entryTop - 1, 0.0F).next();
		tessellator.draw();
	}
}
