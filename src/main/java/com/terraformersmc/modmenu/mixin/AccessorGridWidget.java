package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;

@Mixin(GridLayout.class)
public interface AccessorGridWidget {
	@Accessor
	List<LayoutElement> getChildren();
}
