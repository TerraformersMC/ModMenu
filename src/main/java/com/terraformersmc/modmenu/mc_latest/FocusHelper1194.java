package com.terraformersmc.modmenu.mc_latest;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.compat.FocusHelper;
import net.minecraft.client.gui.widget.ClickableWidget;

public class FocusHelper1194 extends FocusHelper {
	@Override
	public void focusOnClickableWidget(ClickableWidget widget, ModsScreen screen) {
		widget.setFocused(true);
	}
}
