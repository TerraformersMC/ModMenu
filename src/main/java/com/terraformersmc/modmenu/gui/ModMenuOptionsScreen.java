package com.terraformersmc.modmenu.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class ModMenuOptionsScreen extends OptionsSubScreen {

	private Screen previous;
	private OptionsList list;

	public ModMenuOptionsScreen(Screen previous) {
		super(previous, Minecraft.getInstance().options, new TranslatableComponent("modmenu.options"));
		this.previous = previous;
	}


	protected void init() {
		this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		this.list.addSmall(ModMenuConfig.asOptions());
		this.children.add(this.list);
		this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, (button) -> {
			ModMenuConfigManager.save();
			this.minecraft.setScreen(this.previous);
		}));
	}

	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.list.render(matrices, mouseX, mouseY, delta);
		drawCenteredString(matrices, this.font, this.title, this.width / 2, 5, 0xffffff);
		super.render(matrices, mouseX, mouseY, delta);
		List<FormattedCharSequence> list = tooltipAt(this.list, mouseX, mouseY);
		if (list != null) {
			this.renderTooltip(matrices, list, mouseX, mouseY);
		}

	}

	public void removed() {
		ModMenuConfigManager.save();
	}
}
