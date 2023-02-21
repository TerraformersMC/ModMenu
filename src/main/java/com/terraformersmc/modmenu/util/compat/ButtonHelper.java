package com.terraformersmc.modmenu.util.compat;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.List;

public abstract class ButtonHelper {
	public abstract List<ClickableWidget> getButtons(Screen screen);
}
