package com.terraformersmc.modmenu.util.compat;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public abstract class ButtonHelper {
	public abstract ButtonWidget createConfigureButton(
			ModsScreen screen,
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip,
			ModListEntry selected, Map<String, Boolean> modHasConfigScreen, Map<String, Throwable> modScreenErrors
	);

	public abstract ButtonWidget createFiltersButton(
			ModsScreen screen,
			int x, int y, int width, int height, int u, int v, Identifier texture,
			int uWidth, int vHeight, ButtonWidget.PressAction onPress, Text tooltip
	);

	public abstract List<ClickableWidget> getButtons(Screen screen);
}
