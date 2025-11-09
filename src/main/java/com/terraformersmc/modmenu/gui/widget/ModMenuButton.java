package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuButton extends Button.Plain {
	public ModMenuButton(int x, int y, int width, int height, net.minecraft.network.chat.Component text, Screen screen) {
		super(
			x,
			y,
			width,
			height,
			text,
			button -> Minecraft.getInstance().setScreen(new ModsScreen(screen)),
			Button.DEFAULT_NARRATION
		);
	}

	@Override
    public void renderContents(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
		super.renderContents(drawContext, mouseX, mouseY, delta);
		if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(
				drawContext,
				this.width + this.getX() - 16,      // 1.21.8
//				this.width + this.getX() - 13,      // Matches Realms Position
				this.height / 2 + this.getY() - 4
			);
		}
	}
}
