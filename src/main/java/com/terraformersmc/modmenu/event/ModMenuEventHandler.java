package com.terraformersmc.modmenu.event;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ModMenuEventHandler {
	private static final ResourceLocation FABRIC_ICON_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/mods_button.png");

	public static void register() {
		ScreenEvents.AFTER_INIT.register(ModMenuEventHandler::afterScreenInit);
	}

	public static void afterScreenInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
		if (screen instanceof TitleScreen) {
			afterTitleScreenInit(screen);
		} else if (screen instanceof PauseScreen) {
			afterGameMenuScreenInit(screen);
		}
	}

	private static void afterTitleScreenInit(Screen screen) {
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue()) {
			final List<AbstractWidget> buttons = Screens.getButtons(screen);
			int modsButtonIndex = -1;
			final int spacing = 24;
			final int buttonsY = screen.height / 4 + 48;
			for (int i = 0; i < buttons.size(); i++) {
				AbstractWidget button = buttons.get(i);
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.CLASSIC) {
					shiftButtons(button, modsButtonIndex == -1, spacing);
				}
				if (buttonHasText(button, "menu.online")) {
					if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.REPLACE_REALMS) {
						buttons.set(i, new ModMenuButtonWidget(button.x, button.y, button.getWidth(), button.getHeight(), ModMenuApi.createModsButtonText(), screen));
					} else {
						if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.SHRINK) {
							button.setWidth(98);
						}
						modsButtonIndex = i + 1;
					}
				}
			}
			if (modsButtonIndex != -1) {
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.CLASSIC) {
					buttons.add(modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 - 100, buttonsY + spacing * 3 - (spacing / 2), 200, 20, ModMenuApi.createModsButtonText(), screen));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.SHRINK) {
					buttons.add(modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 + 2, buttonsY + spacing * 2, 98, 20, ModMenuApi.createModsButtonText(), screen));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.ICON) {
					buttons.add(modsButtonIndex, new ModMenuTexturedButtonWidget(screen.width / 2 + 104, buttonsY + spacing * 2, 20, 20, 0, 0, FABRIC_ICON_BUTTON_LOCATION, 32, 64, button -> Minecraft.getInstance().setScreen(new ModsScreen(screen)), ModMenuApi.createModsButtonText()));
				}
			}
		}
	}

	private static void afterGameMenuScreenInit(Screen screen) {
		if (ModMenuConfig.MODIFY_GAME_MENU.getValue()) {
			final List<AbstractWidget> buttons = Screens.getButtons(screen);
			int modsButtonIndex = -1;
			final int spacing = 24;
			final int buttonsY = screen.height / 4 + 8;
			ModMenuConfig.ModsButtonStyle style = ModMenuConfig.MODS_BUTTON_STYLE.getValue().forGameMenu();
			for (int i = 0; i < buttons.size(); i++) {
				AbstractWidget button = buttons.get(i);
				if (style == ModMenuConfig.ModsButtonStyle.CLASSIC) {
					shiftButtons(button, modsButtonIndex == -1, spacing);
				}
				if (buttonHasText(button, "menu.reportBugs")) {
					modsButtonIndex = i + 1;
					if (style == ModMenuConfig.ModsButtonStyle.SHRINK) {
						buttons.set(i, new ModMenuButtonWidget(button.x, button.y, button.getWidth(), button.getHeight(), ModMenuApi.createModsButtonText(), screen));
					} else {
						modsButtonIndex = i + 1;
					}
				}
			}
			if (modsButtonIndex != -1) {
				if (style == ModMenuConfig.ModsButtonStyle.CLASSIC) {
					buttons.add(modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 - 102, buttonsY + spacing * 3 - (spacing / 2), 204, 20, ModMenuApi.createModsButtonText(), screen));
				} else if (style == ModMenuConfig.ModsButtonStyle.ICON) {
					buttons.add(modsButtonIndex, new ModMenuTexturedButtonWidget(screen.width / 2 + 4 + 100 + 2, screen.height / 4 + 72 + -16, 20, 20, 0, 0, FABRIC_ICON_BUTTON_LOCATION, 32, 64, button -> Minecraft.getInstance().setScreen(new ModsScreen(screen)), ModMenuApi.createModsButtonText()));
				}
			}
		}
	}

	private static boolean buttonHasText(AbstractWidget button, String translationKey) {
		Component text = button.getMessage();
		return text instanceof TranslatableComponent && ((TranslatableComponent) text).getKey().equals(translationKey);
	}

	private static void shiftButtons(AbstractWidget button, boolean shiftUp, int spacing) {
		if (shiftUp) {
			button.y -= spacing / 2;
		} else {
			button.y += spacing - (spacing / 2);
		}
	}
}
