package io.github.prospector.modmenu;

import net.fabricmc.api.ClientModInitializer;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";

	public static boolean noFabric;

	@Override
	public void onInitializeClient() {
		noFabric = !net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("fabric");
	}

}
