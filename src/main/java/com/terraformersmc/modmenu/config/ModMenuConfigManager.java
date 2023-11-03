package com.terraformersmc.modmenu.config;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.option.BooleanConfigOption;
import com.terraformersmc.modmenu.config.option.ConfigOptionStorage;
import com.terraformersmc.modmenu.config.option.EnumConfigOption;
import com.terraformersmc.modmenu.config.option.StringSetConfigOption;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Collectors;

public class ModMenuConfigManager {
	private static Path path;

	private static void prepareConfigPath() {
		if (path != null) {
			return;
		}
		path = FabricLoader.getInstance().getConfigDir().resolve(ModMenu.MOD_ID + ".json");
	}

	public static void initializeConfig() {
		load();
	}

	@SuppressWarnings("unchecked")
	private static void load() {
		prepareConfigPath();

		try {
			if (!Files.exists(path)) {
				save();
			}
			if (Files.exists(path)) {
				BufferedReader br = Files.newBufferedReader(path);
				JsonObject json = new JsonParser().parse(br).getAsJsonObject();

				for (Field field : ModMenuConfig.class.getDeclaredFields()) {
					if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
						if (StringSetConfigOption.class.isAssignableFrom(field.getType())) {
							JsonArray jsonArray = json.getAsJsonArray(field.getName().toLowerCase(Locale.ROOT));
							if (jsonArray != null) {
								StringSetConfigOption option = (StringSetConfigOption) field.get(null);
								ConfigOptionStorage.setStringSet(option.getKey(), Sets.newHashSet(jsonArray).stream().map(JsonElement::getAsString).collect(Collectors.toSet()));
							}
						} else if (BooleanConfigOption.class.isAssignableFrom(field.getType())) {
							JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive(field.getName().toLowerCase(Locale.ROOT));
							if (jsonPrimitive != null && jsonPrimitive.isBoolean()) {
								BooleanConfigOption option = (BooleanConfigOption) field.get(null);
								ConfigOptionStorage.setBoolean(option.getKey(), jsonPrimitive.getAsBoolean());
							}
						} else if (EnumConfigOption.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType) {
							JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive(field.getName().toLowerCase(Locale.ROOT));
							if (jsonPrimitive != null && jsonPrimitive.isString()) {
								Type generic = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
								if (generic instanceof Class<?>) {
									EnumConfigOption<?> option = (EnumConfigOption<?>) field.get(null);
									Enum<?> found = null;
									for (Enum<?> value : ((Class<Enum<?>>) generic).getEnumConstants()) {
										if (value.name().toLowerCase(Locale.ROOT).equals(jsonPrimitive.getAsString())) {
											found = value;
											break;
										}
									}
									if (found != null) {
										ConfigOptionStorage.setEnumTypeless(option.getKey(), found);
									}
								}
							}
						}
					}
				}
			}
		} catch (IOException | IllegalAccessException e) {
			System.err.println("Couldn't load Mod Menu configuration file; reverting to defaults");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static void save() {
		ModMenu.clearModCountCache();
		prepareConfigPath();

		JsonObject config = new JsonObject();

		try {
			for (Field field : ModMenuConfig.class.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
					if (BooleanConfigOption.class.isAssignableFrom(field.getType())) {
						BooleanConfigOption option = (BooleanConfigOption) field.get(null);
						config.addProperty(field.getName().toLowerCase(Locale.ROOT), ConfigOptionStorage.getBoolean(option.getKey()));
					} else if (StringSetConfigOption.class.isAssignableFrom(field.getType())) {
						StringSetConfigOption option = (StringSetConfigOption) field.get(null);
						JsonArray array = new JsonArray();
						ConfigOptionStorage.getStringSet(option.getKey()).forEach(array::add);
						config.add(field.getName().toLowerCase(Locale.ROOT), array);
					} else if (EnumConfigOption.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType) {
						Type generic = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
						if (generic instanceof Class<?>) {
							EnumConfigOption<?> option = (EnumConfigOption<?>) field.get(null);
							config.addProperty(field.getName().toLowerCase(Locale.ROOT), ConfigOptionStorage.getEnumTypeless(option.getKey(), (Class<Enum<?>>) generic).name().toLowerCase(Locale.ROOT));
						}
					}
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		String jsonString = ModMenu.GSON.toJson(config);

		try (BufferedWriter fileWriter = Files.newBufferedWriter(path)) {
			fileWriter.write(jsonString);
		} catch (IOException e) {
			System.err.println("Couldn't save Mod Menu configuration file");
			e.printStackTrace();
		}
	}
}
