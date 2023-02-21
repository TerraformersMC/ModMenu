package com.terraformersmc.modmenu.mc_latest;

import com.terraformersmc.modmenu.util.compat.MCVersionHelper;
import net.minecraft.SharedConstants;

public class MCVersionHelper1194 extends MCVersionHelper {
	@Override
	public String getMCVersion() {
		return SharedConstants.getGameVersion().getName();
	}
}
