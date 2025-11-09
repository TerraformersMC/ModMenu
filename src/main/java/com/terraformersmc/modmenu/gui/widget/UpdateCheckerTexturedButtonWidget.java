package com.terraformersmc.modmenu.gui.widget;


import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UpdateCheckerTexturedButtonWidget extends LegacyTexturedButtonWidget {
	public UpdateCheckerTexturedButtonWidget(
		int x,
		int y,
		int width,
		int height,
		int u,
		int v,
		int hoveredVOffset,
		Identifier texture,
		int textureWidth,
		int textureHeight,
		ButtonWidget.PressAction pressAction,
        net.minecraft.text.Text message
	) {
		super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, message);
	}

	@Override
	public void drawIcon(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		super.drawIcon(drawContext, mouseX, mouseY, delta);
		if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(drawContext, this.getX() + this.width - 5, this.getY() - 3);
		}
	}
}
