package com.terraformersmc.modmenu.mc_1_19_2;

import com.terraformersmc.modmenu.util.compat.CompatInitializer;
import com.terraformersmc.modmenu.util.compat.MCCompat;
import com.terraformersmc.modmenu.util.compat.MCVersions;

public class ModMenuMCCompat implements CompatInitializer {
	@Override
	public void onInitialize(MCCompat compat) {
		if (MCVersions.MC22W46A.before()) {
			compat.setKeybindsHelper(new KeybindHelper1192());

			if (MCVersions.MC22W45A.before()) {
				compat.setTooltipHelper(new TooltipHelper1192());
				compat.setWidgetHelper(new WidgetHelper1192());
				compat.setButtonHelper(new ButtonHelper1192());

				if (MCVersions.MC22W43A.before()) compat.setConfigHelper(new ConfigHelper1192());
			}
		}
	}
}
