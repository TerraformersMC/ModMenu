package com.terraformersmc.modmenu.config.option;

import com.terraformersmc.modmenu.util.TranslationUtil;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class BooleanConfigOption implements OptionConvertable {
	private final String key, translationKey;
	private final boolean defaultValue;
	private final Text enabledText;
	private final Text disabledText;

	public BooleanConfigOption(String key, boolean defaultValue, String enabledKey, String disabledKey) {
		ConfigOptionStorage.setBoolean(key, defaultValue);
		this.key = key;
		this.translationKey = TranslationUtil.translationKeyOf("option", key);
		this.defaultValue = defaultValue;
		this.enabledText = Text.translatable(translationKey + "." + enabledKey);
		this.disabledText = Text.translatable(translationKey + "." + disabledKey);
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

	public Text getButtonText() {
		return ScreenTexts.composeGenericOptionText(Text.translatable(translationKey), getValue() ? enabledText : disabledText);
	}

	@Override
	public SimpleOption<Boolean> asOption() {
		if (enabledText != null && disabledText != null) {
			return new SimpleOption<>(translationKey, SimpleOption.emptyTooltip(),
					(text, value) -> value ? enabledText : disabledText, SimpleOption.BOOLEAN, getValue(),
					newValue -> ConfigOptionStorage.setBoolean(key, newValue));
		}
		return SimpleOption.ofBoolean(translationKey, getValue(), (value) -> ConfigOptionStorage.setBoolean(key, value));
	}
}
