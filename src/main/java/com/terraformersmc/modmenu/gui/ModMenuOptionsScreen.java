package com.terraformersmc.modmenu.gui;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.text.Text;

public class ModMenuOptionsScreen extends GameOptionsScreen {

	private OptionListWidget list;

	public ModMenuOptionsScreen(Screen previous) {
		super(previous, MinecraftClient.getInstance().options, Text.translatable("modmenu.options"));
	}


	@Override
	protected void init() {
		this.list = this.addDrawableChild(new OptionListWidget(this.client, this.width, this));
		this.list.addAll(ModMenuConfig.asOptions());
		super.init();
	}

	@Override
	protected void addOptions() {
		// NO-OP
	}

	@Override
	protected void initTabNavigation() {
		super.initTabNavigation();
		this.list.position(this.width, this.layout);
	}

	@Override
	public void render(DrawContext DrawContext, int mouseX, int mouseY, float delta) {
		super.render(DrawContext, mouseX, mouseY, delta);
		this.list.render(DrawContext, mouseX, mouseY, delta);
	}

	@Override
	public void removed() {
		ModMenuConfigManager.save();
		ModMenu.checkForUpdates();
	}
}
