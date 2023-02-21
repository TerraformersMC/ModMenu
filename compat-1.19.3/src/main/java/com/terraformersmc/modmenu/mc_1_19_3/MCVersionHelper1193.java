package com.terraformersmc.modmenu.mc_1_19_3;

import com.terraformersmc.modmenu.util.compat.MCVersionHelper;
import net.minecraft.SharedConstants;

public class MCVersionHelper1193 extends MCVersionHelper {
	@Override
	public String getMCVersion() {
		return SharedConstants.getGameVersion().getName();
	}
}
