package com.terraformersmc.modmenu.mc_1_19_3;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.compat.FocusHelper;
import net.minecraft.client.gui.widget.ClickableWidget;

public class FocusHelper1193 extends FocusHelper {
	@Override
	public void focusOnClickableWidget(ClickableWidget widget, ModsScreen screen) {
		screen.setInitialFocus(widget);
	}
}
