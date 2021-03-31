package com.terraformersmc.modmenu.config.option;

import com.terraformersmc.modmenu.util.TranslationUtil;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Locale;

public class EnumConfigOption<E extends Enum<E>> extends CycleOption {
	private final String key, translationKey;
	private final Class<E> enumClass;
	private final E defaultValue;

	public EnumConfigOption(String key, E defaultValue) {
		super(key, (ignored, amount) -> ConfigOptionStorage.cycleEnum(key, defaultValue.getDeclaringClass()), null);
		ConfigOptionStorage.setEnum(key, defaultValue);
		this.key = key;
		this.translationKey = TranslationUtil.translationKeyOf("option", key);
		this.enumClass = defaultValue.getDeclaringClass();
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public E getValue() {
		return ConfigOptionStorage.getEnum(key, enumClass);
	}

	public void setValue(E value) {
		ConfigOptionStorage.setEnum(key, value);
	}

	public void cycleValue() {
		ConfigOptionStorage.cycleEnum(key, enumClass);
	}

	public void cycleValue(int amount) {
		ConfigOptionStorage.cycleEnum(key, enumClass, amount);
	}

	@Override
	public Component getMessage(Options options) {
		return new TranslatableComponent(translationKey, new TranslatableComponent(translationKey + "." + ConfigOptionStorage.getEnum(key, enumClass).name().toLowerCase(Locale.ROOT)));
	}

	public E getDefaultValue() {
		return defaultValue;
	}
}
