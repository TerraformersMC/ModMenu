package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.gui.ModsConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ModsConfigButtonWidget extends ButtonWidget {
	public ModsConfigButtonWidget(int x, int y, int width, int height, Text text, Screen screen) {
		super(x, y, width, height, text, button -> MinecraftClient.getInstance().openScreen(new ModsConfigScreen(screen)));
	}
}
