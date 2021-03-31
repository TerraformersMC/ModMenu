package com.terraformersmc.modmenu.api;

import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface ConfigScreenFactory<S extends Screen> extends io.github.prospector.modmenu.api.ConfigScreenFactory<S> {
	S create(Screen parent);
}
