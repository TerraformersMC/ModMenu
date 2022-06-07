package com.terraformersmc.modmenu.config.option;

import net.minecraft.text.Text;

import com.terraformersmc.modmenu.util.TranslationUtil;

import java.util.Set;

public class StringSetConfigOption {
	private final String key, translationKey;
	private final Set<String> defaultValue;

	public StringSetConfigOption(String key, Set<String> defaultValue) {
		super();
		ConfigOptionStorage.setStringSet(key, defaultValue);
		this.key = key;
		this.translationKey = TranslationUtil.translationKeyOf("option", key);
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public Set<String> getValue() {
		return ConfigOptionStorage.getStringSet(key);
	}

	public void setValue(Set<String> value) {
		ConfigOptionStorage.setStringSet(key, value);
	}

	public Text getMessage() {
		return Text.translatable(translationKey);
	}

	public Set<String> getDefaultValue() {
		return defaultValue;
	}
}
