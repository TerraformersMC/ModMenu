package io.github.prospector.modmenu.api;

import io.github.prospector.modmenu.ModMenu;
import net.minecraft.client.gui.Screen;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ModMenuApi {
	/**
	 * Replaced with {@link ModMenuApi#getConfigScreen(Screen)}, with
	 * the ModMenuApi implemented onto a class that is added as an
	 * entry point to your fabric mod metadata.
	 *
	 * @deprecated Will be removed in 1.15 snapshots.
	 */
	@Deprecated
	static void addConfigOverride(String modid, Runnable action) {
		ModMenu.addLegacyConfigScreenTask(modid, action);
	}

	/**
	 * Used to determine the owner of this API implementation.
	 * Will be deprecated and removed once Fabric has support
	 * for providing ownership information about entry points.
	 */
	String getModId();

	/**
	 * Replaced with {@link ModMenuApi#getConfigScreenFactory()}, which
	 * now allows ModMenu to open the screen for you, rather than depending
	 * on you to open it, and gets rid of the messy Optional->Supplier wrapping.
	 *
	 * @deprecated Will be removed in 1.15 snapshots.
	 */
	@Deprecated
	default Optional<Supplier<Screen>> getConfigScreen(Screen screen) {
		return Optional.empty();
	}

	/**
	 * Used to construct a new config screen instance when your mod's
	 * configuration button is selected on the mod menu screen. The
	 * screen instance parameter is the active mod menu screen.
	 *
	 * @return A factory function for constructing config screen instances.
	 */
	default Function<Screen, Screen> getConfigScreenFactory() {
		return screen -> getConfigScreen(screen).map(Supplier::get).orElse(null);
	}
}
