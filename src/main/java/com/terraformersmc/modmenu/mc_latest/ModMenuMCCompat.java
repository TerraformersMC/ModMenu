package com.terraformersmc.modmenu.mc_latest;

import com.terraformersmc.modmenu.util.compat.*;

public class ModMenuMCCompat implements CompatInitializer {
	@Override
	public void onInitialize(MCCompat compat) {
		if (MCCompat.after22w46a) compat.setKeybindsHelper(new KeybindsHelper());
		if (MCCompat.after22w45a) compat.setTooltipHelper(new TooltipHelper());
		if (MCCompat.after22w45a) compat.setWidgetHelper(new WidgetHelper1194());
		if (MCCompat.after22w43a) compat.setConfigHelper(new ConfigHelper1194());
		if (MCCompat.after23w03a) compat.setBlaze3DHelper(new Blaze3DHelper());
		if (MCCompat.after23w03a) compat.setDescriptionListWidgetHelper(new DescriptionListWidgetHelper1194());
		if (MCCompat.after23w03a) compat.setButtonHelper(new ButtonHelper1194());
		if (MCCompat.after23w03a) compat.setMcVersionHelper(new MCVersionHelper1194());
		if (MCCompat.after23w03a) compat.setFocusHelper(new FocusHelper1194());
	}
}
