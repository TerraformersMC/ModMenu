package com.terraformersmc.modmenu.gui;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class ModMenuOptionsScreen extends OptionsSubScreen {
	public ModMenuOptionsScreen(Screen previous) {
		super(previous, Minecraft.getInstance().options, Component.translatable("modmenu.options"));
	}

	@Override
	protected void addOptions() {
		if (this.list != null) {
			this.list.addSmall(ModMenuConfig.asOptions());
		}
	}

	@Override
	public void removed() {
		ModMenuConfigManager.save();
		ModMenu.checkForUpdates();
	}
}
