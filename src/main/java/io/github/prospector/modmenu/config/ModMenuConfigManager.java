package io.github.prospector.modmenu.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public class ModMenuConfigManager {
	private static File file;
	private static ModMenuConfig config;

	private static void prepareBiomeConfigFile() {
		if (file != null) {
			return;
		}
		file = new File(FabricLoader.getInstance().getConfigDirectory(), ModMenu.MOD_ID + ".json");
	}

	public static ModMenuConfig initializeConfig() {
		if (config != null) {
			return config;
		}

		config = new ModMenuConfig();
		load();

		return config;
	}

	private static void load() {
		prepareBiomeConfigFile();

		try {
			if (file.exists()) {
				Gson gson = new Gson();
				BufferedReader br = new BufferedReader(new FileReader(file));

				config = gson.fromJson(br, ModMenuConfig.class);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't load Mod Menu configuration file; reverting to defaults");
			e.printStackTrace();
		}
	}

	public static void save() {
		prepareBiomeConfigFile();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonString = gson.toJson(config);

		try (FileWriter fileWriter = new FileWriter(file)) {
			fileWriter.write(jsonString);
		} catch (IOException e) {
			System.err.println("Couldn't save Mod Menu configuration file");
			e.printStackTrace();
		}
	}

	public static ModMenuConfig getConfig() {
		return config;
	}
}
