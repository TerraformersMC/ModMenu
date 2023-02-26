package com.terraformersmc.modmenu.util.compat;

import net.minecraft.client.util.math.MatrixStack;

public interface ListWidgetCompat {
	default void renderListCompat(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
		throw new RuntimeException("[ModMenu] This is not supposed to happen!");
	}
}
