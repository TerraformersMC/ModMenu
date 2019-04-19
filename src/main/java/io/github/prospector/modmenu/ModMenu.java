package io.github.prospector.modmenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.HashMap;
import java.util.Map;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

	public static final Map<String, Runnable> CONFIG_OVERRIDES_LEGACY = new HashMap<>();
	public static final Map<String, ModMenuApi> API_MAP = new HashMap<>();
	public static final Map<String, Boolean> MOD_API = new HashMap<>();
	public static final Map<String, Boolean> MOD_CLIENTSIDE = new HashMap<>();

	public static boolean noFabric;

	public static void updateCacheApiValue(String modid, boolean value) {
		MOD_API.put(modid, value);
	}

	public static void updateCacheClientsideOnlyValue(String modid, boolean value) {
		MOD_CLIENTSIDE.put(modid, value);
	}

	@Override
	public void onInitializeClient() {
		noFabric = !FabricLoader.getInstance().isModLoaded("fabric");
		FabricLoader.getInstance().getEntrypoints("modmenu", ModMenuApi.class).forEach(modMenuApi -> API_MAP.put(modMenuApi.getModId(), modMenuApi));
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			ModMetadata metadata = mod.getMetadata();
			try {
				if (metadata.containsCustomElement("modmenu:api")) {
					ModMenu.updateCacheApiValue(metadata.getId(), metadata.getCustomElement("modmenu:api").getAsBoolean());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (metadata.containsCustomElement("modmenu:clientsideOnly")) {
					ModMenu.updateCacheClientsideOnlyValue(metadata.getId(), metadata.getCustomElement("modmenu:clientsideOnly").getAsBoolean());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
