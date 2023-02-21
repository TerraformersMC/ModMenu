package com.terraformersmc.modmenu.util.compat;

import net.minecraft.client.option.SimpleOption;

import java.util.function.Consumer;

public abstract class ConfigHelper {
	public abstract <T> SimpleOption<T> createSimpleOption(
			String key, SimpleOption.ValueTextGetter<T> valueTextGetter, SimpleOption.Callbacks<T> callbacks,
			T defaultValue, Consumer<T> changeCallback
	);
}
