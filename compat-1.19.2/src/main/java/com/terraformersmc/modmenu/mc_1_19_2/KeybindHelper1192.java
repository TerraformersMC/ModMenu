package com.terraformersmc.modmenu.mc_1_19_2;

import com.terraformersmc.modmenu.util.compat.KeybindsHelper;
import net.minecraft.client.MinecraftClient;

public class KeybindHelper1192 extends KeybindsHelper {
	@Override
	public void setRepeatEvents(boolean bol) {
		MinecraftClient.getInstance().keyboard.setRepeatEvents(bol);
	}
}
