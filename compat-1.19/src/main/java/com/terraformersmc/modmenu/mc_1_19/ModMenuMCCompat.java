package com.terraformersmc.modmenu.mc_1_19;

import com.terraformersmc.modmenu.util.compat.CompatInitializer;
import com.terraformersmc.modmenu.util.compat.MCCompat;
import com.terraformersmc.modmenu.util.compat.MCVersions;

public class ModMenuMCCompat implements CompatInitializer {
	@Override
	public void onInitialize(MCCompat compat) {
		if (MCVersions.MC1_19_1_PRE2.before() || MCVersions.MC1_19_1_RC1.match()) {
			compat.setNarratorHelper(new NarratorHelper119());
		}
	}
}
