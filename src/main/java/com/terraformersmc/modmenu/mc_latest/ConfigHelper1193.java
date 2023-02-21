package com.terraformersmc.modmenu.mc_latest;

import com.terraformersmc.modmenu.util.compat.ConfigHelper;
import net.minecraft.client.option.SimpleOption;

import java.util.function.Consumer;

public class ConfigHelper1193 extends ConfigHelper {
	@Override
	public <T> SimpleOption<T> createSimpleOption(
			String key, SimpleOption.ValueTextGetter<T> valueTextGetter, SimpleOption.Callbacks<T> callbacks,
			T defaultValue, Consumer<T> changeCallback) {
		return new SimpleOption<>(key, SimpleOption.emptyTooltip(), valueTextGetter, callbacks, defaultValue, changeCallback);
	}
}
