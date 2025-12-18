package com.terraformersmc.modmenu.mixin;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractWidget.class)
public interface AccessorClickableWidget {
	@Accessor
	WidgetTooltipHolder getTooltip();
}
