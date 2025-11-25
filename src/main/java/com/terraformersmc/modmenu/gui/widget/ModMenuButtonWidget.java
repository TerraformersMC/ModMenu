package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class ModMenuButtonWidget extends ButtonWidget.Text {
	public ModMenuButtonWidget(int x, int y, int width, int height, net.minecraft.text.Text text, Screen screen) {
		super(
			x,
			y,
			width,
			height,
			text,
			button -> MinecraftClient.getInstance().setScreen(new ModsScreen(screen)),
			ButtonWidget.DEFAULT_NARRATION_SUPPLIER
		);
	}

	@Override
    public void drawIcon(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		super.drawIcon(drawContext, mouseX, mouseY, delta);
		if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(
				drawContext,
//				this.width + this.getX() - 16,      // 1.21.8
				this.width + this.getX() - 13,      // Matches Realms Position
				this.height / 2 + this.getY() - 4
			);
		}
	}
}
