package com.terraformersmc.modmenu.mc_latest;

import com.terraformersmc.modmenu.util.compat.CompatInitializer;
import com.terraformersmc.modmenu.util.compat.KeybindsHelper;
import com.terraformersmc.modmenu.util.compat.MCCompat;
import com.terraformersmc.modmenu.util.compat.TooltipHelper;

public class ModMenuMCCompat implements CompatInitializer {
	@Override
	public void onInitialize(MCCompat compat) {
		if (MCCompat.after22w46a) compat.setKeybindsHelper(new KeybindsHelper());
		if (MCCompat.after22w45a) compat.setTooltipHelper(new TooltipHelper());
		if (MCCompat.after22w45a) compat.setWidgetHelper(new WidgetHelper1193());
	}
}
