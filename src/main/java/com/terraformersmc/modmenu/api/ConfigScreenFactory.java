package com.terraformersmc.modmenu.api;

import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ConfigScreenFactory<S extends Screen> {
    /**
     * @apiNote Returning {@code null} does not hide the config button.
     */
    @Nullable S create(Screen parent);
}
