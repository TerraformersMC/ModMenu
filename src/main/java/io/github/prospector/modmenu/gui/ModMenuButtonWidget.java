package io.github.prospector.modmenu.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class ModMenuButtonWidget extends ButtonWidget {
	public Screen screen;

	public ModMenuButtonWidget(int i, int i1, int i2, String s, Screen screen) {
		super(i, i1, i2, s);
		this.screen = screen;
	}

	public ModMenuButtonWidget(int i, int i1, int i2, int i3, int i4, String s, Screen screen) {
		super(i, i1, i2, i3, i4, s);
		this.screen = screen;
	}

	@Override
	public void onPressed(double var1, double var3) {
		MinecraftClient.getInstance().openScreen(new ModListScreen(screen));
	}
}
