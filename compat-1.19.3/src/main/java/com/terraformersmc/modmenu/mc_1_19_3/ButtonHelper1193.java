package com.terraformersmc.modmenu.mc_1_19_3;

import com.terraformersmc.modmenu.mixin.mc1193plus.IGridWidgetAccessor;
import com.terraformersmc.modmenu.util.compat.ButtonHelper;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;

import java.util.List;

public class ButtonHelper1193 extends ButtonHelper {
	@Override
	public List<ClickableWidget> getButtons(Screen screen) {
		ClickableWidget widget = Screens.getButtons(screen).get(0);
		if (widget instanceof GridWidget) return ((IGridWidgetAccessor) widget).getChildren();
		return null;
	}
}
