package com.terraformersmc.modmenu.gui;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ModsConfigScreen extends Screen {
	private final Screen previous;
	private ButtonListWidget list;

	public ModsConfigScreen(Screen previous) {
		super(new TranslatableText("modmenu.config.title"));
		this.previous = previous;
	}

	protected void init() {
		this.list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
		this.list.addAll(getAllModConfigOptions());

		this.addSelectableChild(this.list);
		this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20,
				ScreenTexts.DONE, (button) -> this.client.openScreen(this.previous)));
	}

	private Option[] getAllModConfigOptions() {
		List<Option> options = new LinkedList<>();
		for (Mod mod : ModMenu.MODS.values().stream().sorted(ModMenuConfig.SORTING.getValue().getComparator()).collect(Collectors.toList())) {
			try {
				Screen configScreen = ModMenu.getConfigScreen(mod.getId(), this);
				if (configScreen != null && !mod.getId().equals("minecraft")) {
					options.add(new ModConfigOption(mod, configScreen));
				}
			} catch (NoClassDefFoundError e) {
				ModMenu.LOGGER.warn("The '" + mod.getId() + "' mod config screen is not available because " + e.getLocalizedMessage() + " is missing.");
			} catch (Throwable e) {
				ModMenu.LOGGER.error("Error from mod '" + mod.getId() + "'", e);
			}
		}
		return options.toArray(new Option[0]);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.list.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
		super.render(matrices, mouseX, mouseY, delta);
	}

	public void onClose() {
		this.client.openScreen(this.previous);
	}

	class ModConfigOption extends Option {
		private final Mod mod;
		private final Screen configScreen;

		public ModConfigOption(Mod mod, Screen configScreen) {
			super(mod.getId());
			this.mod = mod;
			this.configScreen = configScreen;
		}

		@Override
		public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
			return new ButtonWidget(x, y, width, 20, Text.of(this.mod.getName()), this::onPress);
		}

		private void onPress(ButtonWidget buttonWidget) {
			client.openScreen(this.configScreen);
		}
	}
}
