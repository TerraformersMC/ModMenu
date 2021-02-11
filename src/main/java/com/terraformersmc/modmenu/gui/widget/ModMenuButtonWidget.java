package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ModMenuButtonWidget extends ButtonWidget {
	public ModMenuButtonWidget(int x, int y, int width, int height, Text text, Screen screen) {
		super(x, y, width, height, text, button -> MinecraftClient.getInstance().openScreen(new ModsScreen(screen)));
	}
}
