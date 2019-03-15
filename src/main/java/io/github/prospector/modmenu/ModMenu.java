package io.github.prospector.modmenu;

import java.util.Map;

import com.google.common.collect.Maps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Map<String, Runnable> CONFIG_RUNNABLE_MAP = Maps.newHashMap();

	public static boolean noFabric;

	@Override
	public void onInitializeClient() {
		noFabric = !FabricLoader.getInstance().isModLoaded("fabric");
	}

}
