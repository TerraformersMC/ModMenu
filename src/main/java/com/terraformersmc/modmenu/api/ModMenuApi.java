package com.terraformersmc.modmenu.api;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.Consumer;

public interface ModMenuApi {

	/**
	 * Used for creating a {@link Screen} instance of the Mod Menu
	 * "Mods" screen
	 *
	 * @param previous The screen before opening
	 * @return A "Mods" Screen
	 */
	static Screen createModsScreen(Screen previous) {
		return new ModsScreen(previous);
	}

	/**
	 * Used for creating a {@link Text} just like what would appear
	 * on a Mod Menu Mods button
	 *
	 * @return The text that would be displayed on a Mods button
	 */
	static Text createModsButtonText() {
		return ModMenu.createModsButtonText(true);
	}

	/**
	 * Used to construct a new config screen instance when your mod's
	 * configuration button is selected on the mod menu screen. The
	 * screen instance parameter is the active mod menu screen.
	 *
	 * @return A factory for constructing config screen instances.
	 */
	default ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> null;
	}

	/**
	 * Used for mods that have their own update checking logic.
	 * By returning your own {@link UpdateChecker} instance, you can override ModMenus built-in update checking logic.
	 *
	 * @return An {@link UpdateChecker} or <code>null</code> if ModMenu should handle update checking.
	 */
	default UpdateChecker getUpdateChecker() {
		return null;
	}

	/**
	 * Used to provide config screen factories for other mods. This takes second
	 * priority to a mod's own config screen factory provider. For example, if
	 * mod `xyz` supplies a config screen factory, mod `abc` providing a config
	 * screen to `xyz` will be pointless, as the one provided by `xyz` will be
	 * used.
	 * <p>
	 * This method is NOT meant to be used to add a config screen factory to
	 * your own mod.
	 *
	 * @return a map of mod ids to screen factories.
	 */
	default Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return ImmutableMap.of();
	}

	/**
	 * Used to mark mods with a badge indicating that they are
	 * provided by a modpack.
	 * <p>
	 * Builtin mods such as `minecraft` cannot be marked as
	 * provided by a modpack.
	 */
	default void attachModpackBadges(Consumer<String> consumer) {
		return;
	}
}
