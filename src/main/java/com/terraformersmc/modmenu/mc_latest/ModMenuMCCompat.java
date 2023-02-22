package com.terraformersmc.modmenu.mc_latest;

import com.terraformersmc.modmenu.util.compat.*;

public class ModMenuMCCompat implements CompatInitializer {
	@Override
	public void onInitialize(MCCompat compat) {
		if (MCVersions.MC22W43A.laterIncluded()) {
			compat.setConfigHelper(new ConfigHelper1194());

			if (MCVersions.MC22W45A.laterIncluded()) {
				compat.setTooltipHelper(new TooltipHelper());

				if (MCVersions.MC22W46A.laterIncluded()) {
					compat.setKeybindsHelper(new KeybindsHelper());

					if (MCVersions.MC23W03A.laterIncluded()) {
						compat.setBlaze3DHelper(new Blaze3DHelper());
						compat.setDescriptionListWidgetHelper(new DescriptionListWidgetHelper1194());
						compat.setButtonHelper(new ButtonHelper1194());
						compat.setMcVersionHelper(new MCVersionHelper1194());
						compat.setFocusHelper(new FocusHelper1194());

						if (MCVersions.MC23W05A.laterIncluded()) {
							compat.setWidgetHelper(new WidgetHelper1194());
						}
					}
				}
			}
		}
	}
}
