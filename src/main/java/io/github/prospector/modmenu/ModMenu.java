package io.github.prospector.modmenu;

import com.google.common.collect.Maps;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Map;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";

	/* USE io.github.prospector.modmenu.api.ModMenuApi! */
	@Deprecated
	public static final Map<String, Runnable> CONFIG_OVERRIDES = Maps.newHashMap();

	public static boolean noFabric;

	@Override
	public void onInitializeClient() {
		noFabric = !FabricLoader.getInstance().isModLoaded("fabric");
	}

}
