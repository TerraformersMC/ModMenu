package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

/*
 * This class is used to detect mods which do not override the default ModMenuApi.getModConfigScreenFactory()
 * while still preserving the guarantee the above API method does not return null.
 */
public class NullScreenFactory<S extends Screen> implements ConfigScreenFactory<S> {
    @Override
    public @Nullable S create(Screen parent) {
        return null;
    }
}
