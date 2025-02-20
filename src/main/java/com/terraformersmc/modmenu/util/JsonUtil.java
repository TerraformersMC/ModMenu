package com.terraformersmc.modmenu.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Optional;

public class JsonUtil {
	private JsonUtil() {
	}

	public static Optional<String> getString(JsonObject parent, String field) {
		if (!parent.has(field)) {
			return Optional.empty();
		}

		var value = parent.get(field);
		if (!value.isJsonPrimitive() || !((JsonPrimitive) value).isString()) {
			return Optional.empty();
		}

		return Optional.of(value.getAsString());
	}

	public static Optional<Boolean> getBoolean(JsonObject parent, String field) {
		if (!parent.has(field)) {
			return Optional.empty();
		}

		var value = parent.get(field);
		if (!value.isJsonPrimitive() || !((JsonPrimitive) value).isBoolean()) {
			return Optional.empty();
		}

		return Optional.of(value.getAsBoolean());
	}
}
