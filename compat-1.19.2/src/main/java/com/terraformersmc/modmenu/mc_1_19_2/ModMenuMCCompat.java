package com.terraformersmc.modmenu.mc_1_19_2;

import com.terraformersmc.modmenu.util.compat.CompatInitializer;
import com.terraformersmc.modmenu.util.compat.MCCompat;

public class ModMenuMCCompat implements CompatInitializer {
	@Override
	public void onInitialize(MCCompat compat) {
		if (!MCCompat.after22w46a) compat.setKeybindsHelper(new KeybindHelper1192());
		if (!MCCompat.after22w45a) compat.setTooltipHelper(new TooltipHelper1192());
		if (!MCCompat.after22w45a) compat.setWidgetHelper(new WidgetHelper1192());
		if (!MCCompat.after22w43a) compat.setConfigHelper(new ConfigHelper1192());
		if (!MCCompat.after22w45a) compat.setButtonHelper(new ButtonHelper1192());
	}
}
