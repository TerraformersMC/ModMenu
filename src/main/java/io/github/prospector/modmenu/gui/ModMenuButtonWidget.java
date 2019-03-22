package io.github.prospector.modmenu.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class ModMenuButtonWidget extends ButtonWidget {
	public ModMenuButtonWidget(int x, int y, int width, int height, String text, Screen screen) {
		super(x, y, width, height, text, button -> MinecraftClient.getInstance().openScreen(new ModListScreen(screen)));
	}
}
