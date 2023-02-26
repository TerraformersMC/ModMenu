package com.terraformersmc.modmenu.mc_latest;

import com.terraformersmc.modmenu.util.compat.NarratorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.NarratorManager;

public class NarratorHelper1194 extends NarratorHelper {
	@Override
	public NarratorManager getNarratorManager() {
		return MinecraftClient.getInstance().getNarratorManager();
	}
}
