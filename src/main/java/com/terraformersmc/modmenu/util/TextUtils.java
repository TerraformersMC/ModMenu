package com.terraformersmc.modmenu.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class TextUtils {
	public static TranslatableText translatable(String key) {
		return new TranslatableText(key);
	}

	public static TranslatableText translatable(String key, Object... args) {
		return new TranslatableText(key, args);
	}

	public static LiteralText literal(String text) {
		return new LiteralText(text);
	}
}
