package com.terraformersmc.modmenu.config.option;

import com.mojang.serialization.Codec;
import com.terraformersmc.modmenu.util.TranslationUtil;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Locale;

public class EnumConfigOption<E extends Enum<E>> implements OptionConvertable {
	private final String key, translationKey;
	private final Class<E> enumClass;
	private final E defaultValue;

	public EnumConfigOption(String key, E defaultValue) {
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

	public E getDefaultValue() {
		return defaultValue;
	}

	private static <E extends Enum<E>> Text getValueText(EnumConfigOption<E> option, E value) {
		return Text.translatable(option.translationKey + "." + value.name().toLowerCase(Locale.ROOT));
	}

	public Text getButtonText() {
		return ScreenTexts.composeGenericOptionText(Text.translatable(translationKey), getValueText(this, getValue()));
	}

	@Override
	public SimpleOption<E> asOption() {
		return new SimpleOption<>(translationKey, SimpleOption.emptyTooltip(),
				(text, value) -> getValueText(this, value),
				new SimpleOption.PotentialValuesBasedCallbacks<>(Arrays.asList(enumClass.getEnumConstants()),
						Codec.STRING.xmap(
								string -> Arrays.stream(enumClass.getEnumConstants()).filter(e -> e.name().toLowerCase().equals(string)).findAny().orElse(null),
								newValue -> newValue.name().toLowerCase()
						)),
				getValue(), value -> ConfigOptionStorage.setEnum(key, value));
	}
}
