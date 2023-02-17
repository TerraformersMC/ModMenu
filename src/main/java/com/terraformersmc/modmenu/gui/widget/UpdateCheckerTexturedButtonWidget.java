package com.terraformersmc.modmenu.gui.widget;


import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UpdateCheckerTexturedButtonWidget extends ModMenuTexturedButtonWidget {
	public UpdateCheckerTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress, Text message) {
		super(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message);
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderButton(matrices, mouseX, mouseY, delta);
		if (ModMenuConfig.UPDATE_BADGE.getValue() && ModMenu.modUpdateAvailable) {
			UpdateAvailableBadge.renderBadge(matrices, this.x + this.width - 5, this.y - 3);
		}
	}
}
