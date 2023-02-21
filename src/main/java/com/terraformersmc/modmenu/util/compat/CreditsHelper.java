package com.terraformersmc.modmenu.util.compat;

import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.gui.screen.CreditsScreen;

public abstract class CreditsHelper {
	public abstract CreditsScreen createScreen(boolean endCredits, ModsScreen parent);
}
