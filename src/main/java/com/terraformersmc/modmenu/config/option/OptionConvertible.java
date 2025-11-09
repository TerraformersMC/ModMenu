package com.terraformersmc.modmenu.config.option;

import net.minecraft.client.OptionInstance;

public interface OptionConvertible {
	OptionInstance<?> asOption();
}
