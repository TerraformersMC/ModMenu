package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ModMenuButtonWidget extends ButtonWidget {
	public ModMenuButtonWidget(int x, int y, int width, int height, Text text, Screen screen) {
		super(x, y, width, height, text, button -> MinecraftClient.getInstance().setScreen(new ModsScreen(screen)), ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
	}

	@Override
	public void renderWidget(DrawContext DrawContext, int mouseX, int mouseY, float delta) {
		super.renderWidget(DrawContext, mouseX, mouseY, delta);
		if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(DrawContext, this.width + this.getX() - 16, this.height / 2 + this.getY() - 4);
		}
	}
}
