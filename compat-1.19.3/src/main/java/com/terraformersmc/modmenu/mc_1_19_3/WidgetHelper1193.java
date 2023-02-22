package com.terraformersmc.modmenu.mc_1_19_3;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.util.compat.WidgetHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class WidgetHelper1193 extends WidgetHelper {
	@Override
	public ButtonWidget createConfigureButton(
			ModsScreen screen,
			int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip,
			RenderOverride renderOverride
	) {
		return new ModMenuTexturedButtonWidget1193(x, y, width, height, u, v, texture, uWidth, vHeight, onPress) {
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
			int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip
	) {
		ButtonWidget widget = new ModMenuTexturedButtonWidget1193(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, tooltip);
		widget.setTooltip(Tooltip.of(tooltip));
		return widget;
	}

	@Override
	public ButtonWidget createModMenuButtonWidget(int x, int y, int width, int height, Text text, Screen screen) {
		return new ModMenuButtonWidget(x, y, width, height, text, screen);
	}

	@Override
	public ButtonWidget createModMenuTexturedButtonWidget(
			int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text message, boolean allowUpdateBadge
	) {
		return new ModMenuTexturedButtonWidget1193(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, allowUpdateBadge);
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
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
		buffer.vertex(matrix, entryLeft + 1, entryTop + entryHeight + 1, 0.0F).next();
		buffer.vertex(matrix, selectionRight - 1, entryTop + entryHeight + 1, 0.0F).next();
		buffer.vertex(matrix, selectionRight - 1, entryTop - 1, 0.0F).next();
		buffer.vertex(matrix, entryLeft + 1, entryTop - 1, 0.0F).next();
		tessellator.draw();
	}
}
