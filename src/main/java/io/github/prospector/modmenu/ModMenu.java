package io.github.prospector.modmenu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";

	public static boolean noFabric;

	@Override
	public void onInitializeClient() {
		noFabric = !FabricLoader.getInstance().isModLoaded("fabric");
	}

}
