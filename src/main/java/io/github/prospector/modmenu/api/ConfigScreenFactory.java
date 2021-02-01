package io.github.prospector.modmenu.api;

import net.minecraft.client.gui.screen.Screen;

/**
 * Deprecated interface, switch to {@link com.terraformersmc.modmenu.api.ModMenuApi} and {@link com.terraformersmc.modmenu.api.ConfigScreenFactory} instead
 *
 * Will be removed in 1.18 snapshots
 */
@Deprecated
@FunctionalInterface
public interface ConfigScreenFactory<S extends Screen> {
	S create(Screen parent);
}
