package com.terraformersmc.modmenu.mc_1_19_2;

import com.terraformersmc.modmenu.gui.ModMenuOptionsScreen;
import com.terraformersmc.modmenu.util.compat.TooltipHelper;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;

import java.util.List;

public class TooltipHelper1192 extends TooltipHelper {
	@Override
	public void modMenuOptionsScreen$render(ModMenuOptionsScreen screen, MatrixStack matrices, int mouseX, int mouseY) {
		List<OrderedText> list = GameOptionsScreen.getHoveredButtonTooltip((ButtonListWidget) screen.getList(), mouseX, mouseY);
		if (list != null) {
			screen.renderOrderedTooltip(matrices, list, mouseX, mouseY);
		}
	}
}
