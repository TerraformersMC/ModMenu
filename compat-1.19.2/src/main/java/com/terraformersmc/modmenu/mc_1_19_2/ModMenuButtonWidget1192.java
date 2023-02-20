package com.terraformersmc.modmenu.mc_1_19_2;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.UpdateAvailableBadge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ModMenuButtonWidget1192 extends ButtonWidget {
	public ModMenuButtonWidget1192(int x, int y, int width, int height, Text text, Screen screen) {
		super(x, y, width, height, text, button -> MinecraftClient.getInstance().setScreen(new ModsScreen(screen)), ButtonWidget.EMPTY);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.modUpdateAvailable) {
			UpdateAvailableBadge.renderBadge(matrices, this.width + this.x - 16, this.height / 2 + this.y - 4);
		}
	}
}
