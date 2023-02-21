package com.terraformersmc.modmenu.mc_1_19_2;

import com.terraformersmc.modmenu.util.compat.ButtonHelper;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.List;

public class ButtonHelper1192 extends ButtonHelper {
	@Override
	public List<ClickableWidget> getButtons(Screen screen) {
		return Screens.getButtons(screen);
	}
}
