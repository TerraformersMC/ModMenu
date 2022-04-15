package com.terraformersmc.modmenu.config.option;

import net.minecraft.client.option.SimpleOption;

public interface OptionConvertable {
	SimpleOption<?> asOption();
}
