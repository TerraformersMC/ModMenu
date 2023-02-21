package com.terraformersmc.modmenu.util.compat;

import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;

public abstract class FocusHelper {
	public abstract void focusOnClickableWidget(ClickableWidget widget, ModsScreen screen);
}
