package com.terraformersmc.modmenu.gui;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.text.Text;

public class ModMenuOptionsScreen extends GameOptionsScreen {
	public ModMenuOptionsScreen(Screen previous) {
		super(previous, MinecraftClient.getInstance().options, Text.translatable("modmenu.options"));
	}

	@Override
	protected void addOptions() {
		if (this.body != null) {
			this.body.addAll(ModMenuConfig.asOptions());
		}
	}

	@Override
	public void removed() {
		ModMenuConfigManager.save();
		ModMenu.checkForUpdates();
	}
}
