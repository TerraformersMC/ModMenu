package com.terraformersmc.modmenu.util.mod.fabric;

import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.*;

public class CustomValueUtil {
	public static Optional<Boolean> getBoolean(String key, ModMetadata metadata) {
		if (metadata.containsCustomValue(key)) {
			return Optional.of(metadata.getCustomValue(key).getAsBoolean());
		}
		return Optional.empty();
	}

	public static Optional<String> getString(String key, ModMetadata metadata) {
		if (metadata.containsCustomValue(key)) {
			return Optional.of(metadata.getCustomValue(key).getAsString());
		}
		return Optional.empty();
	}

	public static Optional<Set<String>> getStringSet(String key, ModMetadata metadata) {
		if (metadata.containsCustomValue(key)) {
			return getStringSet(key, metadata.getCustomValue(key).getAsObject());
		}
		return Optional.empty();
	}

	public static Optional<Boolean> getBoolean(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			return Optional.of(object.get(key).getAsBoolean());
		}
		return Optional.empty();
	}

	public static Optional<String> getString(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			return Optional.of(object.get(key).getAsString());
		}
		return Optional.empty();
	}

	public static Optional<String[]> getStringArray(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			CustomValue.CvArray cvArray = object.get(key).getAsArray();
			String[] strings = new String[cvArray.size()];
			for (int i = 0; i < cvArray.size(); i++) {
				strings[i] = cvArray.get(i).getAsString();
			}
			return Optional.of(strings);
		}
		return Optional.empty();
	}

	public static Optional<Set<String>> getStringSet(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			Set<String> strings = new HashSet<>();
			for (CustomValue value : object.get(key).getAsArray()) {
				strings.add(value.getAsString());
			}
			return Optional.of(strings);
		}
		return Optional.empty();
	}

	public static Optional<Map<String, String>> getStringMap(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			Map<String, String> strings = new HashMap<>();
			for (Map.Entry<String, CustomValue> entry : object.get(key).getAsObject()) {
				strings.put(entry.getKey(), entry.getValue().getAsString());
			}
			return Optional.of(strings);
		}
		return Optional.empty();
	}
}
