package com.terraformersmc.modmenu.mixin;

import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClickableWidget.class)
public interface AccessorClickableWidget {
	@Accessor
	TooltipState getTooltip();
}
