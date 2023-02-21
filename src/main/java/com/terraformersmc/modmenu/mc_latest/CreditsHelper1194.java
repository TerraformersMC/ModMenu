package com.terraformersmc.modmenu.mc_latest;

import com.google.common.util.concurrent.Runnables;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.compat.CreditsHelper;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.CreditsScreen;

public class CreditsHelper1194 extends CreditsHelper {
	@Override
	public CreditsScreen createScreen(boolean endCredits, ModsScreen parent) {
		return new CreditsScreen(endCredits, new LogoDrawer(false), Runnables.doNothing()) {
			@Override
			public void close() {
				client.setScreen(parent);
			}
		};
	}
}
