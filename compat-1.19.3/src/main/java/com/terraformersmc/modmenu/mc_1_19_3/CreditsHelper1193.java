package com.terraformersmc.modmenu.mc_1_19_3;

import com.google.common.util.concurrent.Runnables;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.compat.CreditsHelper;
import net.minecraft.client.gui.screen.CreditsScreen;

public class CreditsHelper1193 extends CreditsHelper {
	@Override
	public CreditsScreen createScreen(boolean endCredits, ModsScreen parent) {
		return new CreditsScreen(endCredits, Runnables.doNothing()) {
			@Override
			public void close() {
				client.setScreen(parent);
			}
		};
	}
}
