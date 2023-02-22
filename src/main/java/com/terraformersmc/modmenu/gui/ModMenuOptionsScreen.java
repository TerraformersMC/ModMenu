package com.terraformersmc.modmenu.gui;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.util.compat.MCCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ModMenuOptionsScreen extends GameOptionsScreen {

	private Screen previous;
	private OptionListWidget list;

	@SuppressWarnings("resource")
	public ModMenuOptionsScreen(Screen previous) {
		super(previous, MinecraftClient.getInstance().options, Text.translatable("modmenu.options"));
		this.previous = previous;
	}


	protected void init() {
		this.list = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
		this.list.addAll(ModMenuConfig.asOptions());
		this.addSelectableChild(this.list);
		this.addDrawableChild(
				MCCompat.getInstance().getWidgetHelper().createButtonWidget(
						this.width / 2 - 100, this.height - 27,
						200, 20,
						ScreenTexts.DONE, (button) -> {
							ModMenuConfigManager.save();
							this.client.setScreen(this.previous);
						}
				)
		);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.list.render(matrices, mouseX, mouseY, delta);
		drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
		super.render(matrices, mouseX, mouseY, delta);

		MCCompat.getInstance().getTooltipHelper().modMenuOptionsScreen$render(this, matrices, mouseX, mouseY);
	}

	public Object getList() {
		return list;
	}

	public void removed() {
		ModMenuConfigManager.save();
	}
}
