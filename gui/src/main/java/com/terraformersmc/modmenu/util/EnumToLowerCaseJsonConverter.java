package com.terraformersmc.modmenu.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class EnumToLowerCaseJsonConverter implements JsonSerializer<Enum<?>>, JsonDeserializer<Enum<?>> {
	private static final Map<String, Class<? extends Enum<?>>> TYPE_CACHE = new HashMap<>();

	@Override
	public JsonElement serialize(final Enum<?> src, final Type typeOfSrc, final JsonSerializationContext context) {
		if (src == null) {
			return JsonNull.INSTANCE;
		}
		return new JsonPrimitive(src.name().toLowerCase());
	}

	@Override
	public Enum<?> deserialize(
		final JsonElement json,
		final Type type,
		final JsonDeserializationContext context
	) throws JsonParseException {
		if (json == null || json.isJsonNull()) {
			return null;
		}

		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) {
			throw new JsonParseException("Expecting a String JsonPrimitive, getting " + json);
		}

		try {
			final String enumClassName = type.getTypeName();
			Class<? extends Enum<?>> enumClass = TYPE_CACHE.get(enumClassName);
			if (enumClass == null) {
				enumClass = (Class<? extends Enum<?>>) Class.forName(enumClassName);
				TYPE_CACHE.put(enumClassName, enumClass);
			}

			return Enum.valueOf((Class) enumClass, json.getAsString().toUpperCase());
		} catch (final ClassNotFoundException e) {
			throw new JsonParseException(e);
		}
	}
}
