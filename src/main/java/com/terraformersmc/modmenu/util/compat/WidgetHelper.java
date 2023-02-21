package com.terraformersmc.modmenu.util.compat;

import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public abstract class WidgetHelper {
	public abstract ButtonWidget createConfigureButton(
			ModsScreen screen,
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip,
			RenderOverride renderOverride
	);

	public abstract ButtonWidget createFiltersButton(
			ModsScreen screen,
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip
	);

	public abstract ButtonWidget createModMenuButtonWidget(
			int x, int y, int width, int height, Text text, Screen screen
	);

	public abstract ButtonWidget createModMenuTexturedButtonWidget(
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text message, boolean allowUpdateBadge
	);

	public ButtonWidget createModMenuTexturedButtonWidget(
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text message
	) {
		return this.createModMenuTexturedButtonWidget(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, false);
	}

	public abstract ButtonWidget createButtonWidget(
			int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress, RenderOverride renderOverride
	);

	public abstract ButtonWidget createButtonWidget(
			int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress
	);

	public abstract void renderModListWidget(
			MatrixStack matrices, Tessellator tessellator, BufferBuilder buffer,
			int entryTop, int entryHeight, int entryLeft, int selectionRight
	);

	public static interface RenderOverride {
		void render(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY, float delta);
	}
}
