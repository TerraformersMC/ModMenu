package com.terraformersmc.modmenu.config.option;

import com.terraformersmc.modmenu.util.TranslationUtil;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BooleanConfigOption implements OptionConvertible {
	private final String key, translationKey;
	private final boolean defaultValue;
	private final Component enabledText;
	private final Component disabledText;

	public BooleanConfigOption(String key, boolean defaultValue, String enabledKey, String disabledKey) {
		ConfigOptionStorage.setBoolean(key, defaultValue);
		this.key = key;
		this.translationKey = TranslationUtil.translationKeyOf("option", key);
		this.defaultValue = defaultValue;
		this.enabledText = Component.translatable(translationKey + "." + enabledKey);
		this.disabledText = Component.translatable(translationKey + "." + disabledKey);
	}

	public BooleanConfigOption(String key, boolean defaultValue) {
		this(key, defaultValue, "true", "false");
	}

	public String getKey() {
		return key;
	}

	public boolean getValue() {
		return ConfigOptionStorage.getBoolean(key);
	}

	public void setValue(boolean value) {
		ConfigOptionStorage.setBoolean(key, value);
	}

	public void toggleValue() {
		ConfigOptionStorage.toggleBoolean(key);
	}

	public boolean getDefaultValue() {
		return defaultValue;
	}

	public Component getButtonText() {
		return CommonComponents.optionNameValue(Component.translatable(translationKey), getValue() ? enabledText : disabledText);
	}

	@Override
	public OptionInstance<Boolean> asOption() {
		if (enabledText != null && disabledText != null) {
			return new OptionInstance<>(
				translationKey,
				OptionInstance.noTooltip(),
				(text, value) -> value ? enabledText : disabledText,
				OptionInstance.BOOLEAN_VALUES,
				getValue(),
				newValue -> ConfigOptionStorage.setBoolean(key, newValue)
			);
		}

		return OptionInstance.createBoolean(
			translationKey,
			getValue(),
			(value) -> ConfigOptionStorage.setBoolean(key, value)
		);
	}
}
