package io.github.prospector.modmenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenu.util.ModMenuModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.HashMap;
import java.util.Map;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

	/* USE io.github.prospector.modmenu.api.ModMenuApi! */
	@Deprecated
	public static final Map<String, Runnable> CONFIG_OVERRIDES = new HashMap<>();
	public static final Map<String, ModMenuModConfig> MOD_MENU_MOD_CONFIGS = new HashMap<>();
	public static final Map<String, ModMenuModConfig> MOD_MENU_MOD_CONFIG_OVERRIDES = new HashMap<>();

	public static boolean noFabric;

	public static void updateCacheApiValue(String modid, boolean value) {
		if (MOD_MENU_MOD_CONFIGS.get(modid) == null) {
			MOD_MENU_MOD_CONFIGS.put(modid, new ModMenuModConfig());
		}
		MOD_MENU_MOD_CONFIGS.put(modid, MOD_MENU_MOD_CONFIGS.get(modid).setApi(value));
	}

	public static void updateCacheClientsideOnlyValue(String modid, boolean value) {
		if (MOD_MENU_MOD_CONFIGS.get(modid) == null) {
			MOD_MENU_MOD_CONFIGS.put(modid, new ModMenuModConfig());
		}
		MOD_MENU_MOD_CONFIGS.put(modid, MOD_MENU_MOD_CONFIGS.get(modid).setClientsideOnly(value));
	}

	@Override
	public void onInitializeClient() {
		noFabric = !FabricLoader.getInstance().isModLoaded("fabric");
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
