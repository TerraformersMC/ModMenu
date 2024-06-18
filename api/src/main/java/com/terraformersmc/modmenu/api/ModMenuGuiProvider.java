package com.terraformersmc.modmenu.api;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * This is used to provide your own Mods screens much like Mod Menu's GUI module does.
 * Most mods should not use this API.
 * To use it, implement this as a "modmenu-gui" entrypoint to your mod.
 */
public interface ModMenuGuiProvider {

	/**
	 * Used for creating a {@link Screen} instance of the Mod Menu
	 * "Mods" screen
	 *
	 * @param previous The screen before opening
	 * @return A "Mods" Screen
	 */
	Screen createModsScreen(Screen previous);

	/**
	 * Used for creating a {@link Text} just like what would appear
	 * on a Mod Menu Mods button
	 *
	 * @return The text that would be displayed on a Mods button
	 */
	Text createModsButtonText();
}
