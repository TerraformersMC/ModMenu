package com.terraformersmc.modmenu.mc_latest;

import com.terraformersmc.modmenu.mixin.mc1193plus.IGridWidgetAccessor;
import com.terraformersmc.modmenu.util.compat.ButtonHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;

import java.util.List;

public class ButtonHelper1194 extends ButtonHelper {
	@Override
	public List<ClickableWidget> getButtons(Screen screen) {
		for (Element element : screen.children()) {
			if (element instanceof GridWidget widget) {
				return ((IGridWidgetAccessor) widget).getChildren();
			}
		}

		return null;
	}
}
