package com.terraformersmc.modmenu.mc_1_19_3;

import com.terraformersmc.modmenu.util.compat.CompatInitializer;
import com.terraformersmc.modmenu.util.compat.MCCompat;
import com.terraformersmc.modmenu.util.compat.MCVersions;

public class ModMenuMCCompat implements CompatInitializer {
	@Override
	public void onInitialize(MCCompat compat) {
		if (MCVersions.MC23W05A.before()) {
			if (MCVersions.MC22W45A.laterIncluded()) compat.setWidgetHelper(new WidgetHelper1193());

			if (MCVersions.MC23W03A.before()) {
				compat.setBlaze3DHelper(new Blaze3DHelper1193());
				compat.setDescriptionListWidgetHelper(new DescriptionListWidgetHelper1193());
				compat.setMcVersionHelper(new MCVersionHelper1193());
				compat.setFocusHelper(new FocusHelper1193());

				if (MCVersions.MC22W45A.laterIncluded()) compat.setButtonHelper(new ButtonHelper1193());
			}
		}
	}
}
