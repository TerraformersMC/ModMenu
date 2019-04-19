package io.github.prospector.modmenu.api;

import io.github.prospector.modmenu.ModMenu;
import net.minecraft.client.gui.Screen;

import java.util.Optional;
import java.util.function.Supplier;

public interface ModMenuApi {

	/* Please use the new API by making a class that implements ModMenuApi and add it as a "modmenu" entry point
	 * This method will probably go away come the 1.15 snapshots
	 * */
	@Deprecated
	static void addConfigOverride(String modid, Runnable action) {
		ModMenu.CONFIG_OVERRIDES_LEGACY.put(modid, action);
	}

	String getModId();

	default Optional<Supplier<Screen>> getConfigScreen(Screen screen) {
		return Optional.empty();
	}
}
